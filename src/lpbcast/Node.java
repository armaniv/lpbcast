package lpbcast;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;

public class Node {

	private static final int MAX_L = 15; 			// the maximum view sizes
	private static final int MAX_M = 30; 			// the maximum buffers size
	private static final int FANOUT = 3; 			// the num of processes to which deliver a message (every T)
	private static final double P_EVENT = 0.05;		// prob. that a node generate a new event
	private static final double P_CRASH = 0.002;	// prob. that a node crash

	private Grid<Object> grid; 						// the context's grid
	private int id; 								// the node's identifier
	private ArrayList<Node> view; 					// the node's view
	private ArrayList<Event> events; 				// the node's events list
	private ArrayList<String> eventIds; 			// the node's digest events list
	private ArrayList<Node> sub; 					// the node's subscriptions list
	private ArrayList<Node> unSub; 					// the node's un-subscriptions list
	private ArrayList<Element> retrieveBuf; 		// the message to retrieve list
	private int round;								// the node's round
	private Boolean crashed; 						// signal that the node is failed

	
	public Node(Grid<Object> grid, int id) {
		this.grid = grid;
		this.id = id;
		this.view = new ArrayList<>();
		this.events = new ArrayList<>();
		this.eventIds = new ArrayList<>();
		this.sub = new ArrayList<>();
		this.unSub = new ArrayList<>();
		this.retrieveBuf = new ArrayList<>();
		this.round = 0;
		this.crashed = false;
	}
	

	@ScheduledMethod(start = 1)
	public void initialize() {
		// initially a node knows only its neighbor
		MooreQuery<Node> query = new MooreQuery(grid, this);

		for (Object o : query.query()) {
			if (o instanceof Node) {
				this.view.add((Node) o);
				this.sub.add((Node) o);
			}
		}
	}
	
	@ScheduledMethod(start = 2, interval = 3)
	// crash are rare
	public void SimulateCrash() {
		if (RandomHelper.nextDoubleFromTo(0, 1) < P_CRASH) {
			this.crashed = true;
		}
		
		// ???? a node recovers from crash ?????
	}
	

	@ScheduledMethod(start = 2, interval = 1)
	public void gossipEmission() {
		round++; // ??????? Is this the right place ????????

		// add self to sub
		if (!this.sub.contains(this)) {
			this.sub.add(this);
		}

		// create a new gossip message
		Message gossip = new Message(this, this.events, this.eventIds, this.sub, this.unSub);

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
			Event event = new Event(this);
			this.events.add(event);
		}
	}
	

	public void receiveMessage(Message gossip) {
		// ???? Simulate transmission delay ?????
		
		if (!this.crashed) {
			
			// ---- phase 1
			this.view.removeAll(gossip.getUnSub());
			this.sub.removeAll(gossip.getUnSub());
			
			for(Node uns : gossip.getUnSub())
			{
				if(!this.unSub.contains(uns)) {
					this.unSub.add(uns);
				}
			}
			
			while (this.unSub.size() > MAX_M)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.unSub.size() - 1);
				this.unSub.remove(rnd);
			}
			
			
			// ---- phase 2
			for(Node n_sub : gossip.getSub())
			{
				if (n_sub != this){
					
					if(!this.view.contains(n_sub)) {
						this.view.add(n_sub);
						
						if(!this.sub.contains(n_sub)){
							this.sub.add(n_sub);
						}
					}
				}
			}
			
			while (this.view.size() > MAX_L)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.view.size() - 1);
				Node node_removed = this.view.remove(rnd);
				
				if(!this.sub.contains(node_removed)){
					this.sub.add(node_removed);
				}
			}
		
			while (this.sub.size() > MAX_M)
			{
				int rnd = RandomHelper.nextIntFromTo(0, this.sub.size() - 1);
				this.sub.remove(rnd);
			}
			
			
			// ---- phase 3
			for(Event e : gossip.getEvents())
			{
				if(!this.eventIds.contains(e.getDigest()))
				{
					this.events.add(e);
					this.eventIds.add(e.getDigest());
				}
			}
			
			for(String dig : gossip.getEventIds())
			{
				if(!this.eventIds.contains(dig)) {
					Element elem = new Element(dig, this.round, gossip.getSender());
					
					if(!this.retrieveBuf.contains(elem)){
						this.retrieveBuf.add(elem);
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
	
	
	public String getEventIdsSize() {
		return "n:" + this.id + ", size:"+ this.eventIds.size() + ", crash: " + this.crashed;
	}
	
	

}
