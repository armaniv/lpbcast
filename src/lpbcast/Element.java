package lpbcast;

public class Element {

	private String id; 				// the identifier of an event
	private int round; 				// the round in which this Element is generated
	private int gossip_sender; 	// the node that send the event with id @id

	public Element(String id, int round, int gossip_sender) {
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

	public int getGossipSender() {
		return gossip_sender;
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof Element)) return false;
	    Element o = (Element) obj;
	    return (o.getId().equals(this.id) && o.getRound() == this.round && o.getGossipSender() == this.gossip_sender);
	}
}
