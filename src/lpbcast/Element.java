package lpbcast;

public class Element {

	private String id;
	private int round;
	private Node gossip_sender;
	
	
	public Element(String id, int round, Node gossip_sender) {
		this.id = id;
		this.round = round;
		this.gossip_sender = gossip_sender;
	}


	public String getId() {
		return id;
	}


	public int getRound() {
		return round;
	}


	public Node getGossipSender() {
		return gossip_sender;
	}
	
}
