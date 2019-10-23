package lpbcast;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;

public class Node {

	private static final int  MAX_L = 15; 		// the maximum view sizes	
	private static final int  MAX_M = 30;		// the maximum buffers size
	private static final int  FANOUT = 3;		// the num of processes to which deliver a message (every T)
	
	private Grid<Object> grid; 					// the context's grid
	private int id;								// the node's identifier
	private Set<Node> view;				// the node's view
	private Set<Event> events;			// the node's events list
	private Set<String> eventIds;			// the node's digest events list
	private Set<Node> sub;				// the node's subscriptions list
	private Set<Node> unSub;				// the node's un-subscriptions list
	private Boolean crashed;					// signal that the node is failed
	

	public Node(Grid<Object> grid, int id) {
		this.grid = grid;
		this.id = id;
		this.view = new LinkedHashSet<>();
		this.events = new LinkedHashSet<>();
		this.eventIds = new LinkedHashSet<>();
		this.sub = new LinkedHashSet<>();
		this.unSub = new LinkedHashSet<>();
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
	
	
	public void receiveMessage(Message gossip) {
		// Simulate transmission delay
		 int duration = RandomHelper.nextIntFromTo(0, 40);
	     long startTime = System.currentTimeMillis();
	     long elapsedTime = 0L;
	     while (elapsedTime < duration) {
	    	 elapsedTime = (new Date()).getTime() - startTime;
	     }
	     
	     //TODO receive message procedure
	     
	}

}
