package lpbcast;

import java.util.HashMap;
import java.util.HashSet;

import lpbcast.SchedulableActions.*;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

public class AppNode {

	private int node_count; 			// the number of nodes in the context
	private HashMap<Integer, Node> nodes; 		// the nodes in the context
	private int n_messages; 			// the number of messages that we want in the simulation
	private int msg_per_round;			// the number of messages to be generated per round
	private int churn_rate; 			// the churn rate that we want in the simulation
	private int unsub_rate; 			// the unsub rate that we want in the simulation
	
	// contains the messages that are broadcasted in the current round
	// the key is the id of the message and the element contains a set 
	// of the ids of the nodes that have received that message
	private HashMap<String, HashSet<Integer>> messages;
	
	// contains the messages used to analyze the expected number of 
	// infected processes for a given round with different Fanout values
	private HashMap<String, HashSet<Integer>> analyzedMessages;
	private int analyzedRoundCount=0;
	
	public AppNode(int node_count, int n_messages, int msg_per_round, int churn_rate, int unsub_rate) {
		this.node_count = node_count;
		this.nodes = new HashMap<Integer, Node>();
		this.messages = new HashMap<String, HashSet<Integer>>();
		this.analyzedMessages = new HashMap<String, HashSet<Integer>>();
		this.n_messages = n_messages;
		this.churn_rate = churn_rate;
		this.unsub_rate = unsub_rate;
		this.msg_per_round = msg_per_round;
	}

	public void addNode(Node node) {
		this.nodes.put(node.getId(), node);
	}

	/**
	 * Periodic function which tells to a random node to generate an event. For
	 * visualization purpose manage also the node's variable newEventThisRound
	 */
	@ScheduledMethod(start = 2, interval = 1, priority = 2)
	public void generateBroadcast() {
		// generate a new message
		if (n_messages > 0) {
			for (int i=0; i<msg_per_round && n_messages>0; i++) {
				int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);

				while (this.nodes.get(rnd).getNodeState() != NodeState.SUB) {
					rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
				}

				String eventId = this.nodes.get(rnd).broadcast();			
				HashSet<Integer> receivers = new HashSet<Integer>();
				receivers.add(nodes.get(rnd).getId());
				this.messages.put(eventId, receivers);	
				n_messages--;
				
				if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() >= 100) {
					this.analyzedMessages.put(eventId, receivers);
				}
			}
		}
		
		if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() >= 100) {
			this.analyzedRoundCount++;
		}
	}

	/**
	 * Periodic function in charge of simulating the churn rate.
	 */
	@ScheduledMethod(start = 5, interval = 3)
	public void generateFailure() {
		int n_failure = (this.node_count * this.churn_rate) / 100;
		int tmp = n_failure;

		while (tmp > 0) {
			int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
			if (this.nodes.get(rnd).getNodeState() == NodeState.SUB) {
				this.nodes.get(rnd).crash();
				tmp--;

				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + 3);
				schedule.schedule(scheduleParameters, new RecoverAndSubscribe(this.nodes.get(rnd)));
			}
		}
	}

	/**
	 * Periodic function in charge of simulating the unsubscription rate.
	 */
	@ScheduledMethod(start = 3, interval = 7)
	public void generateUnsubscription() {
		int n_unsubs = (this.node_count * this.unsub_rate) / 100;
		int tmp = n_unsubs;

		while (tmp > 0) {
			int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
			if (this.nodes.get(rnd).getNodeState() == NodeState.SUB) {
				this.nodes.get(rnd).unSubscribe();
				tmp--;

				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParameters = ScheduleParameters.createOneTime(schedule.getTickCount() + 7);
				schedule.schedule(scheduleParameters, new RecoverAndSubscribe(this.nodes.get(rnd)));
			}
		}
	}
	
	public void signalEventReception(Event event, int receiver) {
		System.out.println(receiver + " DELIVERED " + event.getId());
		String eventId = event.getId();
		
		if (this.messages.containsKey(eventId)) {
			HashSet<Integer> receivers = this.messages.get(eventId);
			receivers.add(receiver);
			
			System.out.println("msges reception: " + messages.toString());
			
			if (receivers.size() == this.node_count) {
				this.messages.remove(eventId);
				String[] parts = eventId.split("_");
				int eventGeneratorNodeId = Integer.parseInt(parts[0]);
				this.nodes.get(eventGeneratorNodeId).deleteNew(event);
			}else {
				this.messages.put(eventId, receivers);
			}
			if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() >= 100) {
				this.analyzedMessages.put(eventId, receivers);
			}
		}
	}
	
	public double analyzeInfectedProcesses() {
		int infectedProcesses = 0;
		for (String eventId : this.analyzedMessages.keySet()) {
			infectedProcesses = infectedProcesses + this.analyzedMessages.get(eventId).size();
		}
		return infectedProcesses/(double)this.analyzedMessages.size();
	}
	
	public int analyzeRoundCount() {
		return this.analyzedRoundCount;
	}
	
	@ScheduledMethod(start = 2, interval = 1)
	public void f() {
		
	}
	
	
}
