package lpbcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
		private AppNode appNode;				// the node responsible of the simulation
												// it manages, crashes, broadcast generation, etc
		private int max_l; 						// the maximum view sizes
		private int max_m; 						// the maximum buffers size
		private int fanout; 					// the num of processes to which deliver a message (every T)
		private int initial_neighbors; 			// size of initial connections (neighbors) of a node
		private int round_k; 					// rounds to wait before asking to the sender for unseen events 
		private int round_r; 					// rounds to wait before asking to a random node for unseen events 
		private boolean age_purging;			// if true enables the event purging optimization
		private boolean membership_purging;		// if true enables the membership purging optimization
		private double membership_K;			// 0 < K <= 1 is the weight of the avg used in SELECT_PROCESS()
		private int long_ago;					// parameter of event purging optimization and unsub
		private Context<Object> context;		
		private Network<Object> network;		
		
		// --- node's variables
		private int id; 							// the node's identifier
		private ArrayList<Membership> view; 		// the node's view
		private ArrayList<Event> events; 			// the node's events list
		private ArrayList<Event> myEvents; 			// all the events generates by this node (needed for retransmission)
		
		private HashMap<Event, Boolean> myNewEvents;		// if hash value is false, 
															// it means that these mine events have not been gossiped yet
															// if the map is not empty, it means that it contains my events 
															// which were not received by all other nodes
		
		private ArrayList<String> eventIds; 		// the node's digest events list
		private ArrayList<Membership> subs; 		// the node's subscriptions list
		private ArrayList<Unsubscription> unSubs; 	// the node's un-subscriptions list
		private ArrayList<Element> retrieveBuf; 	// the message to retrieve list
		private int round; 							// the node's round
		private int eventIdCounter; 				// count how many events a node created

	public Node(int id, Grid<Object> grid, Router router, int max_l, int max_m, int fanout, int initial_neighbors,
			int round_k, int round_r, boolean age_purging, boolean membership_purging) {
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
		this.membership_purging = membership_purging;
		this.membership_K = 1;
		this.long_ago = 7;

		this.id = id;
		this.view = new ArrayList<>();
		this.events = new ArrayList<>();
		this.myEvents = new ArrayList<>();
		
		this.myNewEvents = new HashMap<>();
		
		this.eventIds = new ArrayList<>();
		this.subs = new ArrayList<>();
		this.unSubs = new ArrayList<>();
		this.retrieveBuf = new ArrayList<>();
		this.round = 0;
		this.eventIdCounter = 0;
	}

	/**
	 * Configures and initializes the node with mandatory informations to start
	 * participating the algorithm
	 */
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
					if (!neighbors.contains(node.getId())) {
						neighbors.add(node.getId());
					}
				}
			}
			neigborhood_extent++;
		}
		for (Integer neighbor : neighbors) {
			view.add(new Membership(neighbor, 0));
			subs.add(new Membership(neighbor, 0));
		}
		
	}

	/**
	 * Periodic function, which emits this node local information about current
	 * state of the algorithm
	 */
	@SuppressWarnings("unchecked")
	@ScheduledMethod(start = 2, interval = 1, priority = 1)
	public void gossipEmission() {
		round++;

		if (this.nodeState != NodeState.CRASHED || this.nodeState != NodeState.UNSUB) {
			
			ArrayList<Event> events = new ArrayList<Event>();
			for (Event e : this.events) {
				e.incrementAge();
				events.add(e);
			}
			
			// gossip my new events
			for (Event e : this.myNewEvents.keySet()) {
				if (!this.myNewEvents.get(e)) {
					events.add(e);
					this.eventIds.add(e.getId());
					// set new event as Gossiped
					this.myNewEvents.put(e, true);
				}
			}
			
			
			this.events = events;

			// add self to sub
			Membership me = new Membership(this.getId(), 0);
			if (!this.subs.contains(me)) {
				this.subs.add(me);
			}

			// create a new gossip message
			Message gossip = new Message(this.id, this.events, this.eventIds, this.subs, this.unSubs);

			context = ContextUtils.getContext(this);
			network = (Network<Object>) context.getProjection("network");
			
			LinkedHashSet<Integer> selected = new LinkedHashSet<Integer>();
			int i=0;
			while (i<Math.min(fanout,  this.view.size())) {
				int rnd = RandomHelper.nextIntFromTo(0,  this.view.size() -1);
				if (!selected.contains(rnd)) {
					Integer destinationId = this.view.get(rnd).getNodeId();
					
					for (RepastEdge<Object> edge : network.getOutEdges(this)) {
						network.removeEdge(edge);
					}
					Node destination = this.router.locateNode(destinationId);
					network.addEdge(this, destination);
					
					router.sendGossip(gossip, this.id, this.view.get(rnd).getNodeId());
					i++;
				}
				
			}

			this.events.clear();
		}
	}

	/**
	 * Emits a broadcast message (to be delivered to all nodes participating to this
	 * node topic)
	 */
	public String broadcast() {
		Event event = new Event(this.id, this.eventIdCounter);
		this.myEvents.add(event);
		// tell to itself that this event is new and was not gossiped yet
		this.myNewEvents.put(event, false);
		eventIdCounter++;
		if (this.age_purging) {
			removeOldestNotifications();
		}
		return event.getId();
	}

	/**
	 * Processes gossip messages and updates its internal state based on it.
	 * 
	 * @param gossip The gossip message received
	 */
	public void receive(Message gossip) {
		if (this.nodeState != NodeState.CRASHED && this.nodeState != NodeState.UNSUB) {

			// remove obsolete unsubs
			ArrayList<Unsubscription> oldUnSubs = new ArrayList<>();
			for (Unsubscription unsub : gossip.getUnSubs()) {
				if (this.round > (unsub.getAge() + this.long_ago)) {
					oldUnSubs.add(unsub);
				}
			}
			gossip.getUnSubs().removeAll(oldUnSubs);
			this.unSubs.removeAll(oldUnSubs);

			// ---- phase 1
			for (Unsubscription unsub : gossip.getUnSubs()) {
				this.view.removeIf(v -> v.getNodeId() == unsub.getNodeId());
				this.subs.removeIf(s -> s.getNodeId() == unsub.getNodeId());
			}

			for (Unsubscription uns : gossip.getUnSubs()) {
				if (!this.unSubs.contains(uns)) {
					this.unSubs.add(uns);
				}
			}

			// randomly shortens unSubs buffer
			while (this.unSubs.size() > this.max_m) {
				int rnd = RandomHelper.nextIntFromTo(0, this.unSubs.size() - 1);
				this.unSubs.remove(rnd);
			}

			// ---- phase 2

			// update the view and the subscriptions buffers
			// with new subscriptions from the gossip message
			for (Membership n_sub : gossip.getSubs()) {
				if (n_sub.getNodeId() != this.id) {

					if (!this.view.contains(n_sub)) {
						this.view.add(n_sub);

						if (!this.subs.contains(n_sub)) {
							this.subs.add(n_sub);
						}
					}
				}
			}

			// if Frequency Based Membership Purging optimization is ON
			if (this.membership_purging) {
				ArrayList<Membership> gossipSubs = gossip.getSubs();
				for (int j = 0; j < gossipSubs.size(); j++) {
					Membership gossipSub = gossipSubs.get(j);
					int old_freq = gossipSub.getFrequency();

					// if the membership received is in node's view increment
					// the frequency of the membership contained in view
					if (this.view.contains(gossipSub)) {
						int i = this.view.indexOf(gossipSub);
						Membership myMembership = this.view.get(i);
						myMembership.incrementFrequency();
						this.view.remove(i);
						this.view.add(myMembership);
					} else {
						// otherwise add the membership in the
						// view and increment its frequency
						gossipSub.incrementFrequency();
						this.view.add(gossipSub);
					}
					
					// if the received membership is in the node's subscriptions
					// increment its frequency in the subs buffer
					if (this.subs.contains(gossipSub)) {
						int i = this.subs.indexOf(gossipSub);
						Membership myMembership = this.subs.get(i);
						myMembership.incrementFrequency();
						this.subs.remove(i);
						this.subs.add(myMembership);
					} else {
						// otherwise we just add it in the subs and update the frequency
						if (gossipSub.getFrequency() == old_freq) {
							gossipSub.incrementFrequency();
						}
						this.subs.add(gossipSub);
					}
				}

				// when the size of view or subs is above the threshold truncate
				// the buffers by selecting an element using SELECT_PROCESS()
				while (this.view.size() > this.max_l) {
					Membership target = selectProcess(this.view);
					this.view.remove(target);
					if (!this.subs.contains(target)) {
						this.subs.add(target);
					}
				}

				while (this.subs.size() > this.max_m) {
					Membership target = selectProcess(this.subs);
					ArrayList<Membership> view = new ArrayList<Membership>(this.view);
					view.remove(target);
					this.subs = view;
				}
				
			} else {

				// adapt view and subs sizes below the threshold
				// by randomly removing elements from them
				while (this.view.size() > this.max_l) {
					
					int rnd = RandomHelper.nextIntFromTo(0, this.view.size() - 1);
					Membership node_removed = this.view.remove(rnd);

					if (!this.subs.contains(node_removed)) {
						this.subs.add(node_removed);
					}
				}

				while (this.subs.size() > this.max_m) {
					int rnd = RandomHelper.nextIntFromTo(0, this.subs.size() - 1);
					this.subs.remove(rnd);
				}
			}

			// ---- phase 3			
			ArrayList<Event> gossipEvents = gossip.getEvents();
			for (int i=0; i<gossipEvents.size(); i++) {
				Event e = gossipEvents.get(i);
				
				if (!this.eventIds.contains(e.getId())) {
					this.events.add(e);
					// deliver event to the application
					this.deliver(e);
					this.eventIds.add(e.getId());
				}
			}

			// if event purging optimization is set to true
			// we update the ages of the events based on the
			// events we received through the gossip message
			if (this.age_purging) {
				
				ArrayList<Event> toRemove = new ArrayList<Event>();
				ArrayList<Event> toAdd = new ArrayList<Event>();
				for (Event e1 : gossip.getEvents()) {
					for (Event e2 : this.events) {
						if (e1.getEventId() == e2.getEventId() && e2.getAge() < e1.getAge()) {
							e2.updateAge(e1.getAge());
							toRemove.add(e1);
							toAdd.add(e2);
						}
					}
				}
				this.events.removeAll(toRemove);
				this.events.addAll(toAdd);

				// and we remove the oldest events based on age
				removeOldestNotifications();
				
			} else {

				// otherwise we just remove events randomly until
				// the buffer has the maximum size
				while (this.events.size() > this.max_m) {
					int rnd = RandomHelper.nextIntFromTo(0, this.events.size() - 1);
					this.events.remove(rnd);
				}
			}

			// if there are events that other nodes have seen
			// but this node did not, schedule a retrieve actionBoolean
			// where the sender of the message containing that id is contacted
			for (String dig : gossip.getEventIds()) {
				if (!this.eventIds.contains(dig)) {
					Element elem = new Element(dig, this.round, gossip.getSender());

					if (!this.retrieveBuf.contains(elem)) {
						this.retrieveBuf.add(elem);

						// schedule retrieve
						ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
						ScheduleParameters scheduleParameters = ScheduleParameters
								.createOneTime(schedule.getTickCount() + this.round_k);
						schedule.schedule(scheduleParameters, new RetrieveFromSender(elem, this));
					}
				}
			}

			// truncates eventIds removing oldest elements
			while (this.eventIds.size() > this.max_m) {
				this.eventIds.remove(0);
			}

		}
	}

	/**
	 * Purges messages (only when Age Based Purging is ON) when either the event is
	 * out of date or is the oldest
	 */
	@SuppressWarnings("unchecked")
	private void removeOldestNotifications() {

		ArrayList<Event> tmp = new ArrayList<>();
		boolean outOfDate = true;

		// out of date purging
		while (this.events.size() > this.max_m && outOfDate) {
			outOfDate = false;
			for (Event e1 : this.events) {
				for (Event e2 : this.events) {
					if (e1.getCreatorId() == e2.getCreatorId() && e1.getEventId() - e2.getEventId() > this.long_ago) {
						tmp.add(e1);
						outOfDate = true;
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

	/**
	 * Randomly selects an element (process in this context) which has frequency
	 * greater than the average frequency multiplied by this.membership_K
	 * 
	 * @param list - is the list from which to select
	 * @return the selected element (process in this context)
	 */
	public Membership selectProcess(ArrayList<Membership> list) {
		boolean found = false;
		Membership target = null;
		int avg = 0;
		for (Membership m : list) {
			avg = avg + m.getFrequency();
		}
		avg = avg / list.size();
		while (!found) {
			int rnd = RandomHelper.nextIntFromTo(0, list.size() - 1);
			target = list.get(rnd);
			if (target.getFrequency() > (this.membership_K * avg)) {
				found = true;
			} else {
				target.incrementFrequency();
				list.remove(rnd);
				list.add(target);
			}
		}
		return target;
	}

	/**
	 * Requests the input element from the node who has forwarded it to this node
	 * and if the node gives a negative answer it schedules a RetrieveFromRandom
	 * action in (schedule.getTickCount() + this.round_r) ticks
	 * 
	 * @param element - represents an Events but contains only the eventId and
	 *                nodeId to connect with
	 */
	public void requestEventFromSender(Element element) {
		// if still had not received the event
		// ask the sender for it
		if (!this.eventIds.contains(element.getId())) {
			Event e = router.requestEvent(element.getId(), this.id, element.getGossipSender());
			if (e == null) {
				// if we don't receive an answer from the sender
				// schedule fetch from a random process
				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParameters = ScheduleParameters
						.createOneTime(schedule.getTickCount() + this.round_r);
				schedule.schedule(scheduleParameters, new RetrieveFromRandom(element, this));
			} else {
				this.events.add(e);
				this.deliver(e);
				this.eventIds.add(e.getId());
				this.retrieveBuf.remove(element);
			}
		}
	}

	/**
	 * Requests the input element from a random node of the system and if a negative
	 * answer is received requests the event from the source which for sure has it
	 * 
	 * @param element - represents an Events but contains only the eventId and
	 *                nodeId to connect with
	 */
	public void requestEventFromRandom(Element element) {
		// if still had not received the event
		// ask the a random node for it
		if (!this.eventIds.contains(element.getId())) {
			int rnd = RandomHelper.nextIntFromTo(0, view.size() - 1);
			String eventId = element.getId();
			Event event = router.requestEvent(eventId, this.id, view.get(rnd).getNodeId());
			// if the random process does not have the event or it is crashed
			if (event == null) {
				// ask event directly to the source
				String[] parts = eventId.split("_");
				int eventCreator = Integer.parseInt(parts[0]);
				event = router.requestEventToOriginator(eventId, this.id, eventCreator);
				if (event != null) {
					this.events.add(event);
					this.deliver(event);
					this.eventIds.add(event.getId());
					this.retrieveBuf.remove(element);
				}
			} else {
				this.events.add(event);
				this.deliver(event);
				this.eventIds.add(event.getId());
				this.retrieveBuf.remove(element);
			}
		}
	}

	/**
	 * Tell to a node to subscribe to the topic, this is done gossiping its
	 * subscription
	 */
	public void subscribe() {
		this.nodeState = NodeState.SUB;
		// simply call gossip emission (subs will contain only self,
		// events, eventIds, unSubs and retrieveBuf are all empty)
		gossipEmission();
	}

	/**
	 * Tell to a node to unsubscribe to the topic, this is done gossiping its
	 * unsubscription
	 */
	public void unSubscribe() {
		this.nodeState = NodeState.UNSUB;

		this.events.clear();
		this.eventIds.clear();
		this.subs.clear();
		this.unSubs.clear();
		this.retrieveBuf.clear();
		unsubEmission();
	}

	/**
	 * Emits a gossip message that contains only information about the unsubscription
	 * of the node
	 */
	public void unsubEmission() {
		Unsubscription unsub = new Unsubscription(this.id, this.round);
		this.unSubs.add(unsub);

		Message gossip = new Message(this.id, this.events, this.eventIds, this.subs, this.unSubs);
		int view_size = this.view.size();

		LinkedHashSet<Integer> selected_nodes = new LinkedHashSet<>(); // support list
		for (int i = 0; i < fanout && i < view_size; i++) {
			int rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
			Integer destinationId = this.view.get(rnd).getNodeId();
			if (selected_nodes.add(destinationId)) {
				router.sendGossip(gossip, this.id, view.get(rnd).getNodeId());

			} else {
				while (!selected_nodes.add(this.view.get(rnd).getNodeId())) {
					rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
					router.sendGossip(gossip, this.id, view.get(rnd).getNodeId());
				}
			}
		}
	}

	/**
	 * Tell to a node to simulate a crash. A node loose information about events and
	 * participants
	 */
	public void crash() {
		this.nodeState = NodeState.CRASHED;

		this.events.clear();
		this.eventIds.clear();
		this.subs.clear();
		this.unSubs.clear();
		this.retrieveBuf.clear();
	}

	/**
	 * Search for an Event in the events that this node has and return that if
	 * present
	 * 
	 * @param eventId - the identifier of the event to search for
	 */
	public Event findEventId(String eventId) {
		for (Event e : this.events) {
			if (e.getId().equals(eventId)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Search for an Event in the events that this node generated and return that if
	 * present
	 * 
	 * @param eventId - the identifier of the event to search for
	 */
	public Event findEventIdOriginator(String eventId) {
		for (Event e : this.myEvents) {
			if (e.getId().equals(eventId)) {
				return e;
			}
		}
		return null;
	}
	
	public void deliver(Event e) {
		this.appNode.signalEventReception(e, this.id);
	}

	public NodeState getNodeState() {
		return this.nodeState;
	}

	public int getId() {
		return this.id;
	}
	
	public void setAppNode(AppNode appNode) {
		this.appNode = appNode;
	}
	
	public void deleteNew(Event e) {
		this.myNewEvents.remove(e);
	}
	
	/**
	 * Tells if the events generated by this node, 
	 * were received by all the other nodes or not
	 */
	public boolean hasNewEvents() {
		return this.myNewEvents.size() > 0;
	}
}
