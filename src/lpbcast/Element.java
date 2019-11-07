package lpbcast;

public class Element {

	private String id; 				// the identifier of an event
	private int round; 				// the round in which this Element is generated
	private Node gossip_sender; 	// the node that send the event with id @id

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
