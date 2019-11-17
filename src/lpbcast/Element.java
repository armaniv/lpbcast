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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	


}
