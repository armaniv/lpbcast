package lpbcast;

import java.util.HashMap;

public class Network {
	private HashMap<Integer, Node> nodes;
	
	public Network() {}
	
	public void setNodes(HashMap<Integer, Node> nodes) {
		this.nodes = nodes;
	}
	
	// Send a gossip message to a destination node
	public void sendGossip(Message gossip, Integer destinationNodeId) {
		nodes.get(destinationNodeId).receiveMessage(gossip);
	}
	
	// Request an event to a node that might be different from the originator
	public Event requestEvent(String eventId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventId(eventId);
	}
	
	// Request an event to a node that is the originator 
	public Event requestEventToOriginator(String eventId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventIdOriginator(eventId);
	}
}
