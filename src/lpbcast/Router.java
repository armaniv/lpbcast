package lpbcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import lpbcast.SchedulableActions.ReceiveGossip;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.PriorityType;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.random.RandomHelper;

public class Router {
	private HashMap<Integer, Node> nodes; 	// the nodes list
	private int msg_loss_rate;				// the loss rate
	private ArrayList<Integer>msgPropagationDelays;

	public Router(int msg_loss_rate) {
		this.msg_loss_rate = msg_loss_rate;
		this.msgPropagationDelays = new ArrayList<Integer>();
		int[] msgPropagationDelays = {1};
		for (int r : msgPropagationDelays) {
			this.msgPropagationDelays.add(r);
		}
	}

	public void setNodes(HashMap<Integer, Node> nodes) {
		this.nodes = nodes;
	}

	// Send a gossip message to a destination node
	public void sendGossip(Message gossip, Integer sourceNodeId, Integer destinationNodeId) {
		double rnd = RandomHelper.nextDoubleFromTo(0, 1);
		double prob = this.msg_loss_rate / (double) 100;

		if (prob > 0 && rnd < prob) {
			// message is lost
		} else {
			Collections.shuffle(this.msgPropagationDelays);
			
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + 1, PriorityType.RANDOM);
			schedule.schedule(scheduleParameters,
					new ReceiveGossip(sourceNodeId, destinationNodeId, gossip, this));
		}
	}
	
	public void send(Message gossip, Integer sourceNodeId, Integer destinationNodeId) {
		nodes.get(destinationNodeId).receive(gossip);
	}

	// Request an event to a node that might be different from the originator
	public Event requestEvent(String eventId, Integer sourceNodeId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventId(eventId);
	}

	// Request an event to a node that is the originator
	public Event requestEventToOriginator(String eventId, Integer sourceNodeId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventIdOriginator(eventId);
	}

	public Node locateNode(Integer id) {
		return this.nodes.get(id);
	}
}
