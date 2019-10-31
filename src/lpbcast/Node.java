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

	private static final int MAX_L = 15; 			// the maximum view sizes
	private static final int MAX_M = 30; 			// the maximum buffers size
	private static final int FANOUT = 4; 			// the num of processes to which deliver a message (every T)
	private static final double P_EVENT = 0.05;		// prob. that a node generate a new event
	private static final double P_CRASH = 0.001;	// prob. that a node crash
	private static final int INITIAL_NEIGHBORS = 5; // size of initial connections of a node
	private static final int K = 2;					// rounds to wait before start fetching 
													// an event that was not received from the sender
	private static final int R = 2;					// rounds to wait before start fetching 
													// an event that was not received from random participants
	private Network network;						// object that deals with localization and transfer of messages
	private Grid<Object> grid; 						// the context's grid
	private int id; 								// the node's identifier
	private ArrayList<Node> view; 					// the node's view
	private ArrayList<Event> events; 				// the node's events list
	private ArrayList<Event> myEvents;				// the events generates by this node 
	private ArrayList<String> eventIds; 			// the node's digest events list
	private ArrayList<Node> subs; 					// the node's subscriptions list
	private ArrayList<Node> unSubs; 				// the node's un-subscriptions list
	private ArrayList<Element> retrieveBuf; 		// the message to retrieve list
	private int round;								// the node's round
	private Boolean crashed; 						// signal that the node is failed
	private int eventIdCounter;

	
	public Node(Grid<Object> grid, int id, Network network) {
		this.network = network;
		this.grid = grid;
		this.id = id;
		this.view = new ArrayList<>();
		this.events = new ArrayList<>();
		this.myEvents = new ArrayList<>();
		this.eventIds = new ArrayList<>();
		this.subs = new ArrayList<>();
		this.unSubs = new ArrayList<>();
		this.retrieveBuf = new ArrayList<>();
		this.round = 0;
		this.crashed = false;
		this.eventIdCounter = 0;		
	}
	

	@ScheduledMethod(start = 1)
	public void initialize() {
		// initially a node knows only its neighbor		
		// neighbors are some nodes that are somewhere around this node
		ArrayList<Node> neighbors = new ArrayList<Node>();
		int neigborhood_extent = 1;
		while (neighbors.size() < INITIAL_NEIGHBORS) {
			GridPoint pt = grid.getLocation(this);
			GridCellNgh<Node> nghCreator = new GridCellNgh<Node>(grid, pt, Node.class, neigborhood_extent, neigborhood_extent);
			List<GridCell<Node>> gridCells = nghCreator.getNeighborhood(false);
			
			for (GridCell<Node> cell : gridCells) {
				Object o = grid.getObjectAt(cell.getPoint().getX(), cell.getPoint().getY());
				if (o instanceof Node && neighbors.size() < INITIAL_NEIGHBORS) {
					Node node = (Node)o;
					if (!neighbors.contains(node)) {
						neighbors.add(node);
					}
				}
			}
			neigborhood_extent++;
		}
		view.addAll(neighbors);
		subs.addAll(neighbors);		
	}
	
	/*@ScheduledMethod(start = 2, interval = 3)
	// crash are rare
	public void SimulateCrash() {
		if (RandomHelper.nextDoubleFromTo(0, 1) < P_CRASH) {
			this.crashed = true;
			
			// recover from crash in 3 thicks
			class RecoverAction implements IAction {
				public void execute() {
					recover();
				}	        
		    };
			
			// schedule retrievement
		    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + 3);
			schedule.schedule(scheduleParameters, new RecoverAction());
		}
	}*/
	

	@ScheduledMethod(start = 2, interval = 1)
	public void gossipEmission() {
		
		round++; 

		// add self to sub
		if (!this.subs.contains(this)) {
			this.subs.add(this);
		}

		// create a new gossip message
		Message gossip = new Message(this, this.events, this.eventIds, this.subs, this.unSubs);

		int view_size = this.view.size();
		
		// send the gossip message to random selected nodes
		ThreadLocalRandom.current()
			.ints(0, view_size).distinct().limit(Math.min(FANOUT, view_size))
			.forEach(random -> {
				this.network.sendGossip(gossip, view.get(random).getId());
			});

		this.events.clear();

		// with a certain probability generate a new event
		if (/*RandomHelper.nextDoubleFromTo(0, 1) < P_EVENT*/ round==1 && id ==1) {
			Event event = new Event(this, eventIdCounter);
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
			
			for(Node uns : gossip.getUnSub())
			{
				if(!this.unSubs.contains(uns)) {
					this.unSubs.add(uns);
				}
			}
			
			while (this.unSubs.size() > MAX_M)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.unSubs.size() - 1);
				this.unSubs.remove(rnd);
			}
			
			
			// ---- phase 2
			for(Node n_sub : gossip.getSub())
			{
				if (n_sub != this){
					
					if(!this.view.contains(n_sub)) {
						this.view.add(n_sub);
						
						if(!this.subs.contains(n_sub)){
							this.subs.add(n_sub);
						}
					}
				}
			}
			
			while (this.view.size() > MAX_L)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.view.size() - 1);
				Node node_removed = this.view.remove(rnd);
				
				if(!this.subs.contains(node_removed)){
					this.subs.add(node_removed);
				}
			}
		
			while (this.subs.size() > MAX_M)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.subs.size() - 1);
				this.subs.remove(rnd);
			}
			
			
			// ---- phase 3
			for(Event e : gossip.getEvents())
			{
				if(!this.eventIds.contains(e.getId()))
				{
					this.events.add(e);
					this.eventIds.add(e.getId());
				}
			}
			
			for(String dig : gossip.getEventIds())
			{
				if(!this.eventIds.contains(dig)) {
					Element elem = new Element(dig, this.round, gossip.getSender());
					
					if(!this.retrieveBuf.contains(elem)){
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
					    };
						
						// schedule retrievement
					    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
						ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + K);
						schedule.schedule(scheduleParameters, new RetrieveAction(elem));
					}
				}
			}
			
			while (this.eventIds.size() > MAX_M)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.eventIds.size() - 1);
				this.eventIds.remove(rnd);
			}
			
			while (this.events.size() > MAX_M)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.events.size() - 1);
				this.events.remove(rnd);
			}		
		}
	}
	
	public void requestEventFromSender(Element element) {
		if (!this.eventIds.contains(element.getId())){
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
				};
			
				// schedule retrievement
			    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + R);
				schedule.schedule(scheduleParameters, new RetrieveAction(element));
			}else{
				this.events.add(e);
				this.eventIds.add(e.getId());
				this.retrieveBuf.remove(element);
				}
		} else{
			this.retrieveBuf.remove(element);
		}
	}
	
	public void requestEventFromRandom(Element element) {
		int rnd = RandomHelper.nextIntFromTo(0, view.size() - 1);
		String eventId = element.getId();
		Event event = network.requestEvent(eventId, view.get(rnd).getId());
		
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
		} else{
			this.events.add(event);
			this.eventIds.add(event.getId());
			this.retrieveBuf.remove(element);
			}
	}
	
	public Event findEventId(String eventId) {
		for (Event e : this.events){
			if (e.getId().equals(eventId)) {
				return e;
			}
		}
		return null;
	}
	
	public Event findEventIdOriginator(String eventId) {
		for (Event e : this.myEvents){
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
