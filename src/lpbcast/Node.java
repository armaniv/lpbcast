package lpbcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import lpbcast.SchedulableActions.*;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Node {

	// --- node's 'configuration' parameter
	private Router router; 					// object that deals with localization and transfer of messages
	private Grid<Object> grid; 				// the context's grid
	private NodeState nodeState; 			// the node's state (enum)
	private int max_l; 						// the maximum view sizes
	private int max_m; 						// the maximum buffers size
	private int fanout; 					// the num of processes to which deliver a message (every T)
	private int initial_neighbors; 			// size of initial connections (neighbors) of a node
	private int round_k; 					// rounds to wait before asking to the sender for unseen events 
	private int round_r; 					// rounds to wait before asking to a random node for unseen events 
	private boolean age_purging;			// if true enable the event purging optimization
	private int long_ago;					// parameter of event purging optimization and unsub
	private Context<Object> context;
	private Network<Object> network;
	private boolean newEventThisRound;
	
	// --- node's variables
	private int id; 							// the node's identifier
	private ArrayList<Integer> view; 			// the node's view
	private ArrayList<Event> events; 			// the node's events list
	private ArrayList<Event> myEvents; 			// the events generates by this node
	private ArrayList<String> eventIds; 		// the node's digest events list
	private ArrayList<Integer> subs; 			// the node's subscriptions list
	private ArrayList<Unsubscription> unSubs; 	// the node's un-subscriptions list
	private ArrayList<Element> retrieveBuf; 	// the message to retrieve list
	private int round; 							// the node's round
	private int eventIdCounter; 				// count how many events a node created
	

	public Node(int id, Grid<Object> grid, Router router, int max_l, int max_m, int fanout, int initial_neighbors,
			int round_k, int round_r, boolean age_purging) {
		this.router = router;
		this.grid = grid;
		this.nodeState = NodeState.SUB;
		this.max_l = max_l;
		this.max_m = max_m;
		this.fanout = fanout;
		this.initial_neighbors = initial_neighbors;
		this.round_k = round_k;
		this.round_r = round_r;
		this.age_purging = age_purging;
		this.long_ago = 7;
		
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
			GridCellNgh<Node> nghCreator = 
					new GridCellNgh<Node>(grid, pt, Node.class, neigborhood_extent, neigborhood_extent);
			List<GridCell<Node>> gridCells = 
					nghCreator.getNeighborhood(false);

			for (GridCell<Node> cell : gridCells) {
				Object o = grid.getObjectAt(cell.getPoint().getX(), cell.getPoint().getY());
				if (o instanceof Node && neighbors.size() < this.initial_neighbors) {
					Node node = (Node) o;
					if (!neighbors.contains(node.getId())) {
						neighbors.add(node.getId());
					}
				}
			}
			neigborhood_extent++;
		}
		view.addAll(neighbors);
		subs.addAll(neighbors);
	}

	@SuppressWarnings("unchecked")
	@ScheduledMethod(start = 2, interval = 1)
	public void gossipEmission() {
		round++;
		
		if(this.nodeState != NodeState.CRASHED || this.nodeState != NodeState.UNSUB) {	
			ArrayList<Event> events = new ArrayList<Event>();
			for(Event e : this.events) {
				e.incrementAge();
				events.add(e);
			}
			this.events = events;
	
			// add self to sub
			if (!this.subs.contains(this.getId())) {
				this.subs.add(this.id);
			}
	
			// create a new gossip message
			Message gossip = new Message(this, this.events, this.eventIds, this.subs, this.unSubs);
	
			int view_size = this.view.size();
	
			context = ContextUtils.getContext(this);
			network = (Network<Object>)context.getProjection("network");
	
			LinkedHashSet<Integer> selected_nodes = new LinkedHashSet<>(); // support list
			for (int i = 0; i < fanout && i < view_size; i++) {
				int rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
				Integer destinationId = this.view.get(rnd);				
				if (selected_nodes.add(destinationId)){

					for(RepastEdge<Object> edge : network.getOutEdges(this)) {
						network.removeEdge(edge);
					}
					Node destination = this.router.locateNode(destinationId);
					network.addEdge(this, destination);
					router.sendGossip(gossip, this.id, view.get(rnd));
					
				} else {
					while (!selected_nodes.add(this.view.get(rnd))) {
						rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
						destinationId = this.view.get(rnd);
						
						for(RepastEdge<Object> edge : network.getOutEdges(this)) {
							network.removeEdge(edge);
						}
						Node destination = this.router.locateNode(destinationId);
						network.addEdge(this, destination);
						router.sendGossip(gossip, this.id, view.get(rnd));
					}
				}
			}
			
			this.events.clear();
		}
	}

	
	public void broadcast() {
		Event event = new Event(this.id, eventIdCounter);
		this.myEvents.add(event);
		this.events.add(event);
		this.eventIds.add(event.getId());
		eventIdCounter++;
		if (this.age_purging) {
			removeOldestNotifications();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void removeOldestNotifications() {
		
		ArrayList<Event> tmp = new ArrayList<>();
		
		// out of date purging
		while (this.events.size() > this.max_m) {
			for (Event e1 : this.events) {
				for (Event e2 : this.events) {
					if (e1.getCreatorId() == e2.getCreatorId() && e1.getEventId() - e2.getEventId() > this.long_ago) {
						tmp.add(e1);
					}
				}
			}
			this.events.removeAll(tmp);
			tmp.clear();
		}
		
		// by age purging
		while (this.events.size() > this.max_m) {
			int maxAge = Collections.max(this.events).getAge();
			for (Event e : this.events) {
				if (e.getAge() >= maxAge) {
					tmp.add(e);
				}
			}
			this.events.removeAll(tmp);
			tmp.clear();
		}		
	}
	
	public void receive(Message gossip) {
		if(this.nodeState != NodeState.CRASHED && this.nodeState != NodeState.UNSUB) {

			// remove obsolete unsubs
			ArrayList<Unsubscription> old_unsb = new ArrayList<>();
			for(Unsubscription unsub : gossip.getUnSubs())
			{
				if(this.round  > (unsub.getAge() + this.long_ago)) {
					old_unsb.add(unsub);
				}
			}
			gossip.getUnSubs().removeAll(old_unsb);
			this.unSubs.removeAll(old_unsb);
			
			// ---- phase 1
			for(Unsubscription unsub : gossip.getUnSubs())
			{
				this.view.removeIf(v -> v == unsub.getNodeId());
				this.subs.removeIf(s -> s == unsub.getNodeId());
			}

			for (Unsubscription uns : gossip.getUnSubs()) {
				if (!this.unSubs.contains(uns)) {
					this.unSubs.add(uns);
				}
			}

			while (this.unSubs.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.unSubs.size() - 1);
				this.unSubs.remove(rnd);
			}

			// ---- phase 2
			for (Integer n_sub : gossip.getSubs()) {
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
					// LPB-DELIVER(e)
					this.eventIds.add(e.getId());
				}
			}
			
			
			// if event purging optimization is set to true
			if (this.age_purging) {
				
				// -------------- ???? Change dim while iterating ?????			
				
				for (Event e1 : gossip.getEvents()) {
					for (Event e2 : this.events) {
						if (e1.getEventId() == e2.getEventId() && e2.getAge() < e1.getAge()) {
							e2.updateAge(e1.getAge());
							this.events.remove(e1);
							this.events.add(e2);
						}
					}
				}
				
				// ----------------- ????? SELECT PROCESS  ?????
				removeOldestNotifications();
			}	


			for (String dig : gossip.getEventIds()) {
				if (!this.eventIds.contains(dig)) {
					Element elem = new Element(dig, this.round, gossip.getSender());

					if (!this.retrieveBuf.contains(elem)) {
						this.retrieveBuf.add(elem);

						// schedule retrievement
						ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
						ScheduleParameters scheduleParameters = ScheduleParameters
								.createOneTime(schedule.getTickCount() + this.round_k);
						schedule.schedule(scheduleParameters, new RetrieveFromSender(elem, this));
					}
				}
			}

			while (this.eventIds.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.eventIds.size() - 1);
				this.eventIds.remove(rnd);
			}

			
			// if event purging optimization is set to false
			if (!this.age_purging) {
				while (this.events.size() > this.max_m) {
					int rnd = RandomHelper.nextIntFromTo(0, this.events.size() - 1);
					this.events.remove(rnd);
				}
			}

		}
	}

	public void requestEventFromSender(Element element) {
		if (!this.eventIds.contains(element.getId())) {
			// ask event.id from sender
			Event e = router.requestEvent(element.getId(), this.id, element.getGossipSender().getId());
			if (e == null) {
				// if we don't receive an answer from the sender
				// schedule fetch from a random process
				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + this.round_r);
				schedule.schedule(scheduleParameters, new RetrieveFromRandom(element, this));
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
		Event event = router.requestEvent(eventId, this.id, view.get(rnd));

		if (event == null) {
			// ask event directly to the source
			String[] parts = eventId.split("_");
			int eventCreator = Integer.parseInt(parts[0]);
			event = router.requestEventToOriginator(eventId, this.id, eventCreator);
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

	// ----------- TODO: maybe discuss about it
	public void subscribe() {
		this.nodeState = NodeState.SUB;
		// simply call gossip emission (subs will contain only self,
		// events, eventIds, unSubs and retrieveBuf are all empty)
		gossipEmission();
	}
	
	public void unSubscribe() {
		this.nodeState = NodeState.UNSUB;
		// simulate loosing interest 
		this.events.clear();
		this.eventIds.clear();
		this.subs.clear();
		this.unSubs.clear();
		this.retrieveBuf.clear();
		unsubEmission();
	}
	
	public void unsubEmission() {
		Unsubscription unsub = new Unsubscription(this.id, this.round);
		this.unSubs.add(unsub);
		
		Message gossip = new Message(this, this.events, this.eventIds, this.subs, this.unSubs);
		int view_size = this.view.size();

		LinkedHashSet<Integer> selected_nodes = new LinkedHashSet<>(); // support list
		for (int i = 0; i < fanout && i < view_size; i++) {
			int rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
			Integer destinationId = this.view.get(rnd);				
			if (selected_nodes.add(destinationId)){
				router.sendGossip(gossip, this.id, view.get(rnd));
				
			} else {
				while (!selected_nodes.add(this.view.get(rnd))) {
					rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
					router.sendGossip(gossip, this.id, view.get(rnd));
				}
			}
		}
	}
	
	public void setCrashed() {
		this.nodeState = NodeState.CRASHED;
		// loose information about participants and events
		this.events.clear();
		this.eventIds.clear();
		this.subs.clear();
		this.unSubs.clear();
		this.retrieveBuf.clear();
	}
	
	public void setNewEventThisRoundet(boolean value) {
		this.newEventThisRound = value;
	}
	
	public boolean getNewEventThisRound() {
		return this.newEventThisRound;
	}
	
	public NodeState getNodeState() {
		return this.nodeState;
	}
}
