package lpbcast;

import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;

public class MessagesNetwork {
	private HashMap<Integer, Node> nodes;
	
	public MessagesNetwork() {
	}
	
	public void setNodes(HashMap<Integer, Node> nodes) {
		this.nodes = nodes;
	}
	
	// Send a gossip message to a destination node
	@SuppressWarnings("unchecked")
	public void sendGossip(Message gossip, Integer sourceNodeId, Integer destinationNodeId) {
		Node source = nodes.get(sourceNodeId);
		Node destination = nodes.get(destinationNodeId);
		
		Context<Object> context = ContextUtils.getContext(source);
		Network<Object> network = (Network<Object>)context.getProjection("messages network");
		RepastEdge<Object> edge = network.addEdge(source, destination);
		
		destination.receiveMessage(gossip);
		
		network.removeEdge(edge);
	}
	
	// Request an event to a node that might be different from the originator
	public Event requestEvent(String eventId, Integer sourceNodeId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventId(eventId);
	}
	
	// Request an event to a node that is the originator 
	public Event requestEventToOriginator(String eventId, Integer sourceNodeId, Integer destinationNodeId) {
		return nodes.get(destinationNodeId).findEventIdOriginator(eventId);
	}
}
