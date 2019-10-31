package lpbcast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Node {

	// --- node's 'configuration' parameter
	private Network network; 				// object that deals with localization and transfer of messages
	private Grid<Object> grid; 				// the context's grid
	private Boolean crashed; 				// signal that the node is failed
	private int max_l; 						// the maximum view sizes
	private int max_m; 						// the maximum buffers size
	private int fanout; 					// the num of processes to which deliver a message (every T)
	private int initial_neighbors; 			// size of initial connections (neighbors) of a node
	private int round_k; 					// rounds to wait before asking to the sender for unseen events 
	private int round_r; 					// rounds to wait before asking to a random node for unseen events 

	// --- node's variables
	private int id; 						// the node's identifier
	private ArrayList<Integer> view; 			// the node's view
	private ArrayList<Event> events; 		// the node's events list
	private ArrayList<Event> myEvents; 		// the events generates by this node
	private ArrayList<String> eventIds; 	// the node's digest events list
	private ArrayList<Integer> subs; 			// the node's subscriptions list
	private ArrayList<Integer> unSubs; 		// the node's un-subscriptions list
	private ArrayList<Element> retrieveBuf; // the message to retrieve list
	private int round; 						// the node's round
	private int eventIdCounter; 			// count how many events a node created
	

	public Node(Grid<Object> grid, int id, Network network, int max_l, int max_m, int fanout, int initial_neighbors,
			int round_k, int round_r) {
		this.network = network;
		this.grid = grid;
		this.crashed = false;
		this.max_l = max_l;
		this.max_m = max_m;
		this.fanout = fanout;
		this.initial_neighbors = initial_neighbors;
		this.round_k = round_k;
		this.round_r = round_r;

		this.id = id;
		this.view = new ArrayList<>();
		this.events = new ArrayList<>();
		this.myEvents = new ArrayList<>();
		this.eventIds = new ArrayList<>();
		this.subs = new ArrayList<>();
		this.unSubs = new ArrayList<>();
		this.retrieveBuf = new ArrayList<>();
		this.round = 0;
		this.eventIdCounter = 0;
	}

	@ScheduledMethod(start = 1)
	public void initialize() {
		// initially a node knows only its neighbor
		// neighbors are some nodes that are somewhere around this node
		ArrayList<Integer> neighbors = new ArrayList<Integer>();
		int neigborhood_extent = 1;
		while (neighbors.size() < this.initial_neighbors) {
			GridPoint pt = grid.getLocation(this);
			GridCellNgh<Node> nghCreator = new GridCellNgh<Node>(grid, pt, Node.class, neigborhood_extent,
					neigborhood_extent);
			List<GridCell<Node>> gridCells = nghCreator.getNeighborhood(false);

			for (GridCell<Node> cell : gridCells) {
				Object o = grid.getObjectAt(cell.getPoint().getX(), cell.getPoint().getY());
				if (o instanceof Node && neighbors.size() < this.initial_neighbors) {
					Node node = (Node) o;
					if (!neighbors.contains(node)) {
						neighbors.add(node.getId());
					}
				}
			}
			neigborhood_extent++;
		}
		view.addAll(neighbors);
		subs.addAll(neighbors);
	}

	@ScheduledMethod(start = 2, interval = 1)
	public void gossipEmission() {

		round++;

		// add self to sub
		if (!this.subs.contains(this)) {
			this.subs.add(this.id);
		}

		// create a new gossip message
		Message gossip = new Message(this, this.events, this.eventIds, this.subs, this.unSubs);

		int view_size = this.view.size();

		// send the gossip message to random selected nodes
		ThreadLocalRandom.current().ints(0, view_size).distinct().limit(Math.min(this.fanout, view_size))
				.forEach(random -> {
					this.network.sendGossip(gossip, view.get(random));
				});

		this.events.clear();

		// with a certain probability generate a new event
		// TODO: if supernode says me to create an event, do that
		if (round == 1 && id == 1) {
			Event event = new Event(this.id, eventIdCounter);
			this.myEvents.add(event);
			this.events.add(event);
			this.eventIds.add(event.getId());
			eventIdCounter++;
		}
	}

	public void receiveMessage(Message gossip) {

		if (!this.crashed) {

			// ---- phase 1
			this.view.removeAll(gossip.getUnSub());
			this.subs.removeAll(gossip.getUnSub());

			for (Integer uns : gossip.getUnSub()) {
				if (!this.unSubs.contains(uns)) {
					this.unSubs.add(uns);
				}
			}

			while (this.unSubs.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.unSubs.size() - 1);
				this.unSubs.remove(rnd);
			}

			// ---- phase 2
			for (Integer n_sub : gossip.getSub()) {
				if (n_sub != this.id) {

					if (!this.view.contains(n_sub)) {
						this.view.add(n_sub);

						if (!this.subs.contains(n_sub)) {
							this.subs.add(n_sub);
						}
					}
				}
			}

			while (this.view.size() > this.max_l) {
				int rnd = RandomHelper.nextIntFromTo(0, this.view.size() - 1);
				Integer node_removed = this.view.remove(rnd);

				if (!this.subs.contains(node_removed)) {
					this.subs.add(node_removed);
				}
			}

			while (this.subs.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.subs.size() - 1);
				this.subs.remove(rnd);
			}

			// ---- phase 3
			for (Event e : gossip.getEvents()) {
				if (!this.eventIds.contains(e.getId())) {
					this.events.add(e);
					this.eventIds.add(e.getId());
				}
			}

			for (String dig : gossip.getEventIds()) {
				if (!this.eventIds.contains(dig)) {
					Element elem = new Element(dig, this.round, gossip.getSender());

					if (!this.retrieveBuf.contains(elem)) {
						this.retrieveBuf.add(elem);

						// schedule the retrievement of these events
						class RetrieveAction implements IAction {
							private Element element;

							public RetrieveAction(Element element) {
								this.element = element;
							}

							public void execute() {
								requestEventFromSender(element);
							}
						}
						;

						// schedule retrievement
						ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
						ScheduleParameters scheduleParameters = ScheduleParameters
								.createOneTime(schedule.getTickCount() + this.round_k);
						schedule.schedule(scheduleParameters, new RetrieveAction(elem));
					}
				}
			}

			while (this.eventIds.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.eventIds.size() - 1);
				this.eventIds.remove(rnd);
			}

			while (this.events.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.events.size() - 1);
				this.events.remove(rnd);
			}
		}
	}

	public void requestEventFromSender(Element element) {
		if (!this.eventIds.contains(element.getId())) {
			// ask event.id from sender
			Event e = network.requestEvent(element.getId(), element.getGossipSender().getId());
			if (e == null) {
				// if we don't receive an answer from the sender
				// schedule fetch from a random process
				class RetrieveAction implements IAction {
					private Element element;

					public RetrieveAction(Element element) {
						this.element = element;
					}

					public void execute() {
						requestEventFromRandom(element);
					}
				}
				;

				// schedule retrievement
				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + this.round_r);
				schedule.schedule(scheduleParameters, new RetrieveAction(element));
			} else {
				this.events.add(e);
				this.eventIds.add(e.getId());
				this.retrieveBuf.remove(element);
			}
		} else {
			this.retrieveBuf.remove(element);
		}
	}

	public void requestEventFromRandom(Element element) {
		int rnd = RandomHelper.nextIntFromTo(0, view.size() - 1);
		String eventId = element.getId();
		Event event = network.requestEvent(eventId, view.get(rnd));

		if (event == null) {
			// ask event directly to the source
			String[] parts = eventId.split("_");
			int source = Integer.parseInt(parts[0]);
			event = network.requestEventToOriginator(eventId, source);
			if (event != null) {
				this.events.add(event);
				// LPB-DELIVER(event)
				this.eventIds.add(event.getId());
				this.retrieveBuf.remove(element);
			}
		} else {
			this.events.add(event);
			this.eventIds.add(event.getId());
			this.retrieveBuf.remove(element);
		}
	}

	public Event findEventId(String eventId) {
		for (Event e : this.events) {
			if (e.getId().equals(eventId)) {
				return e;
			}
		}
		return null;
	}

	public Event findEventIdOriginator(String eventId) {
		for (Event e : this.myEvents) {
			if (e.getId().equals(eventId)) {
				return e;
			}
		}
		return null;
	}

	public int getEventIdsSize() {
		return this.eventIds.size();
	}

	public int getId() {
		return this.id;
	}

	public void recover() {
		this.crashed = false;
	}

	public boolean simulateCrashed() {
		return this.crashed == true;
	}

}
