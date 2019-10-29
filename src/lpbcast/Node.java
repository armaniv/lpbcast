package lpbcast;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Node {

	private static final int MAX_L = 15; 			// the maximum view sizes
	private static final int MAX_M = 30; 			// the maximum buffers size
	private static final int FANOUT = 3; 			// the num of processes to which deliver a message (every T)
	private static final double P_EVENT = 0.05;		// prob. that a node generate a new event
	private static final double P_CRASH = 0.001;	// prob. that a node crash
	private static final int INITIAL_NEIGHBORS = 5; // size of initial connections of a node
	private static final int K = 5;					// rounds to wait before start fetching 
													// an event that was not received from the sender
	private static final int R = 5;					// rounds to wait before start fetching 
													// an event that was not received from random participants
	private Grid<Object> grid; 						// the context's grid
	private int id; 								// the node's identifier
	private ArrayList<Node> view; 					// the node's view
	private ArrayList<Event> events; 				// the node's events list
	private ArrayList<String> eventIds; 				// the node's digest events list
	private ArrayList<Node> subs; 					// the node's subscriptions list
	private ArrayList<Node> unSubs; 					// the node's un-subscriptions list
	private ArrayList<Element> retrieveBuf; 		// the message to retrieve list
	private int round;								// the node's round
	private Boolean crashed; 						// signal that the node is failed
	private int eventIdCounter = 0;

	
	public Node(Grid<Object> grid, int id) {
		this.grid = grid;
		this.id = id;
		this.view = new ArrayList<>();
		this.events = new ArrayList<>();
		this.eventIds = new ArrayList<>();
		this.subs = new ArrayList<>();
		this.unSubs = new ArrayList<>();
		this.retrieveBuf = new ArrayList<>();
		this.round = 0;
		this.crashed = false;
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
	
	@ScheduledMethod(start = 2, interval = 3)
	// crash are rare
	public void SimulateCrash() {
		if (RandomHelper.nextDoubleFromTo(0, 1) < P_CRASH) {
			this.crashed = true;
			
			// recover from crash in 3 thicks
			
			class RecoverAction implements IAction {
				public void execute() {
					//System.out.println(id + " RECOVERED");
					recover();
				}	        
		    };
			
			// schedule retrievement
		    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + 3);
			schedule.schedule(scheduleParameters, new RecoverAction());
			//System.out.println(id + " RECOVER SCHEDULED");
		}
	}
	

	@ScheduledMethod(start = 2, interval = 1)
	public void gossipEmission() {
		round++; // ??????? Is this the right place ????????

		// add self to sub
		if (!this.subs.contains(this)) {
			this.subs.add(this);
		}

		// create a new gossip message
		Message gossip = new Message(this, this.events, this.eventIds, this.subs, this.unSubs);

		int view_size = this.view.size();
		Set<Node> selected_nodes = new LinkedHashSet<>(); // support list

		for (int i = 0; i < FANOUT && i < view_size; i++) {
			int rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
			
			if (selected_nodes.add(this.view.get(rnd))){
				this.view.get(rnd).receiveMessage(gossip);
			} else {
				while (!selected_nodes.add(this.view.get(rnd))) {
					rnd = RandomHelper.nextIntFromTo(0, view_size - 1);
					this.view.get(rnd).receiveMessage(gossip); // ??? if a node crashed ??? (we can check bool and ignore)
				}
			}
		}

		// clear lists
		selected_nodes.clear();
		this.events.clear();

		// with a certain probability generate a new event
		if (RandomHelper.nextDoubleFromTo(0, 1) < P_EVENT) {
			Event event = new Event(this, eventIdCounter);
			this.events.add(event);
			this.eventIds.add(event.getId());
		}
	}
	

	public void receiveMessage(Message gossip) {
		// ???? Simulate transmission delay ?????
		
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
								tryRetrieveEventFromSender(element);
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
	
	public void tryRetrieveEventFromSender(Element element) {
		System.out.println("FROM SENDER");
		if (!this.eventIds.contains(element.getId())){
			// ask event.id from sender
			Node sender = element.getGossip_sender();
			Event e = sender.tryFindEventId(element.getId());
			if (e == null) {
				// if we don't receive an answer from the sender
				// schedule fetch from a random process
			class RetrieveAction implements IAction {
				private Element element;
				public RetrieveAction(Element element) {
					this.element = element;
				}
				public void execute() {
					tryRetrieveEventFromRandomProcess(element);
				}	        
		    };
			
			// schedule retrievement
		    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + R);
			schedule.schedule(scheduleParameters, new RetrieveAction(element));
			}
		}else{
			retrieveBuf.remove(element);
		}
	}
	
	public void tryRetrieveEventFromRandomProcess(Element element) {
		System.out.println("FROM RANDOM");
		int rnd = RandomHelper.nextIntFromTo(0, view.size() - 1);
		Node randomProcess = view.get(rnd);
		String eId = element.getId();
		if (randomProcess.tryFindEventId(eId) == null) {
			// ask event directy to the source
			String[] parts = eId.split("-");
			int source = Integer.parseInt(parts[1]);
			
		}
	}
	
	public Event tryFindEventId(String eventId) {
		for (Event e : this.events){
			if (e.getId() == eventId) {
				return e;
			}
		};
		return null;
	}
	
	public String getEventIdsSize() {
		return "n:" + this.id + ", size:"+ this.eventIds.size() + ", crash: " + this.crashed;
	}
	
	public boolean hasEvents() {
		return this.eventIds.size() > 0;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void recover() {
		this.crashed = false;
	}
	
	public boolean isCrashed() {
		return this.crashed == true;
	}

}
