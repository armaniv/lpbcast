package lpbcast;

import java.util.HashMap;

import repast.simphony.random.RandomHelper;

public class Router {
	private HashMap<Integer, Node> nodes; // the nodes list
	private int msg_loss_rate;

	public Router(int msg_loss_rate) {
		this.msg_loss_rate = msg_loss_rate;
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
			nodes.get(destinationNodeId).receive(gossip);
		}
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
