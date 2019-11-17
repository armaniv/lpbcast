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
	
	public void setFrequency(Integer freq) {
		this.frequency = freq;
	}

	public void incrementFrequency() {
		this.frequency++;
	}

	public String toString() {
		return this.nodeId + ":" + this.frequency;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof Membership)) return false;
	    Membership o = (Membership) obj;
	    return o.getNodeId() == this.nodeId;
	}
}
