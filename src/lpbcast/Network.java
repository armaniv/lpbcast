package lpbcast;

import java.util.HashMap;

public class Network {
	private HashMap<Integer, Node> nodes;
	
	public Network() {}
	
	public void setNodes(HashMap<Integer, Node> nodes) {
		this.nodes = nodes;
	}
	
	public void sendGossip(Message gossip, Integer destinationNodeId) {
		nodes.get(destinationNodeId).receiveMessage(gossip);
	}
	
	public Event requestEvent(String eventId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventId(eventId);
	}
}
