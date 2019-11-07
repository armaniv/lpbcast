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

}
