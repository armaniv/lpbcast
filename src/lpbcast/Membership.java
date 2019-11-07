package lpbcast;

public class Membership {
	private Integer nodeId; 		// the node identifier
	private Integer frequency;

	public Membership(Integer nodeId, Integer frequency) {
		this.nodeId = nodeId;
		this.frequency = frequency;
	}

	public Integer getNodeId() {
		return this.nodeId;
	}

	public Integer getFrequency() {
		return this.frequency;
	}

	public void incrementFrequency() {
		this.frequency++;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Membership) {
			Membership p = (Membership) o;
			return this.nodeId.equals(p.getNodeId());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.nodeId;
	}
}
