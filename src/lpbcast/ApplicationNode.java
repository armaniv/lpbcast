package lpbcast;

import java.util.ArrayList;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class ApplicationNode {
	
	private ArrayList<Node> nodes;
	private int n_messages;
	private int churn_rate;

	public ApplicationNode(int n_messages, int churn_rate) {
		this.nodes = new ArrayList<>();
		this.n_messages = n_messages;
		this.churn_rate = churn_rate;
	}

	public void addNode(Node node){
		this.nodes.add(node);
	}
	
	
	@ScheduledMethod(start = 2, interval = 1)
	public void GenerateBroadcast() {
		if(n_messages > 0){
			int rnd = RandomHelper.nextIntFromTo(0, this.nodes.size() - 1);
			this.nodes.get(rnd).broadcast();
			n_messages--;
		}
		
	}
	
}
