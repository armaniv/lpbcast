package lpbcast;

public class Unsubscription {
	private Integer nodeId; 	// the node identifier
	private Integer age; 		// the round at which the unsub is generated

	public Unsubscription(Integer nodeId, Integer age) {
		this.nodeId = nodeId;
		this.age = age;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public Integer getAge() {
		return age;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Unsubscription))
			return false;
		Unsubscription o = (Unsubscription) obj;
		return (o.getNodeId().equals(this.nodeId) && o.getAge().equals(this.age));
	}
}
