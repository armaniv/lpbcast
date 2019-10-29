package lpbcast;

import java.util.UUID;

public class Element {

	private UUID id;
	private int round;
	private Node gossip_sender;
	
	
	public Element(UUID id, int round, Node gossip_sender) {
		this.id = id;
		this.round = round;
		this.gossip_sender = gossip_sender;
	}


	public UUID getId() {
		return id;
	}


	public int getRound() {
		return round;
	}


	public Node getGossip_sender() {
		return gossip_sender;
	}
	
}
