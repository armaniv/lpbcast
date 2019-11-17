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
	
	public Integer getEventId() {
		String[] parts = this.id.split("_");
		return Integer.parseInt(parts[1]);
	}
	
	public Integer getGeneratorNodeId() {
		String[] parts = this.id.split("_");
		return Integer.parseInt(parts[0]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gossip_sender == null) ? 0 : gossip_sender.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + round;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element other = (Element) obj;
		if (gossip_sender == null) {
			if (other.gossip_sender != null)
				return false;
		} else if (!gossip_sender.equals(other.gossip_sender))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (round != other.round)
			return false;
		return true;
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
