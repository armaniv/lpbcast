package lpbcast;

public class Element {

	private String id; 				// the identifier of an event
	private int round; 				// the round in which this Element is generated
	private Integer gossip_sender; 	// the node that send the event with id @id

	public Element(String id, int round, Integer gossip_sender) {
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

	public Integer getGossipSender() {
		return gossip_sender;
	}

}
