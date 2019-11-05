package lpbcast;

import java.util.ArrayList;

import lpbcast.SchedulableActions.*;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class ApplicationNode {
	
	private int node_count;				// the number of nodes in the context
	private ArrayList<Node> nodes;		// the nodes in the context
	private int n_messages;				// the number of messages that we want in the simulation
	private int churn_rate;				// the churn_rate that we want in the simulation
	private int n_failure;	
	private int previus_sender;

	public ApplicationNode(int node_count, int n_messages, int churn_rate) {
		this.node_count = node_count;
		this.nodes = new ArrayList<>();
		this.n_messages = n_messages;
		this.churn_rate = churn_rate;
		this.n_failure = (this.node_count * this.churn_rate) / 100;	
		this.previus_sender = -1;
	}

	public void addNode(Node node){
		this.nodes.add(node);
	}
	
	
	@ScheduledMethod(start = 2, interval = 1)
	public void GenerateBroadcast() {
		if (this.previus_sender != -1 && n_messages >= 0) {
			this.nodes.get(previus_sender).setNewEventThisRoundet(false);
		}
		if(n_messages > 0){
			int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
			this.previus_sender = rnd;
			this.nodes.get(rnd).broadcast();
			this.nodes.get(rnd).setNewEventThisRoundet(true);
			n_messages--;
		}
	}
	
	
	@ScheduledMethod(start = 5, interval = 3)
	public void GenerateFailure() {
		int tmp = this.n_failure;
		
		while (tmp > 0){
			int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
			this.nodes.get(rnd).setCrashed();
			tmp--;
			
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			ScheduleParameters scheduleParameters = ScheduleParameters
					.createOneTime(schedule.getTickCount() + 3);
			schedule.schedule(scheduleParameters, new RecoverFromFailure(this.nodes.get(rnd)));
		}	
	}
	
}
