package lpbcast;

public class Unsubscription {
	private int nodeId;
	private int age;
	
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
