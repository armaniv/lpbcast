package lpbcast;

public class Unsubscription {
	private int nodeId; 		// the node identifier
	private int age; 			// the round at which the unsub is generated

	public Unsubscription(int nodeId, int age) {
		this.nodeId = nodeId;
		this.age = age;
	}

	public int getNodeId() {
		return nodeId;
	}

	public int getAge() {
		return age;
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof Unsubscription)) return false;
	    Unsubscription o = (Unsubscription) obj;
	    return (o.getNodeId() == this.nodeId && o.getAge() == this.age);
	}
}
