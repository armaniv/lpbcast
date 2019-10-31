package lpbcast;

import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;

public class Router {
	private HashMap<Integer, Node> nodes;
	
	public Router() {
	}
	
	public void setNodes(HashMap<Integer, Node> nodes) {
		this.nodes = nodes;
	}
	
	// Send a gossip message to a destination node
	public void sendGossip(Message gossip, Integer sourceNodeId, Integer destinationNodeId) {
		nodes.get(destinationNodeId).receiveMessage(gossip);
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
