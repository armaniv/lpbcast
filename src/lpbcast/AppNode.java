package lpbcast;

import java.util.HashMap;
import java.util.HashSet;

import lpbcast.SchedulableActions.*;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class AppNode {

	private int node_count; 			// the number of nodes in the context
	private HashMap<Integer, Node> nodes; 		// the nodes in the context
	private int n_messages; 			// the number of messages that we want in the simulation
	private int churn_rate; 			// the churn rate that we want in the simulation
	private int unsub_rate; 			// the unsub rate that we want in the simulation
	private int previus_sender;
	
	// contains the messages that are broadcasted in the current round
	// the key is the id of the message and the element contains a set 
	// of the ids of the nodes that have received that message
	private HashMap<String, HashSet<Integer>> messages;	
	
	public AppNode(int node_count, int n_messages, int churn_rate, int unsub_rate) {
		this.node_count = node_count;
		this.nodes = new HashMap<Integer, Node>();
		this.messages = new HashMap<String, HashSet<Integer>>();
		this.n_messages = n_messages;
		this.churn_rate = churn_rate;
		this.unsub_rate = unsub_rate;
		this.previus_sender = -1;
	}

	public void addNode(Node node) {
		this.nodes.put(node.getId(), node);
	}

	/**
	 * Periodic function which tells to a random node to generate an event. For
	 * visualization purpose manage also the node's variable newEventThisRound
	 */
	@ScheduledMethod(start = 2, interval = 1)
	public void generateBroadcast() {
		// reset newEventThisRound of the previous sender
//		if (this.previus_sender != -1 && n_messages >= 0) {
//			this.nodes.get(previus_sender).setNewEventThisRoundet(false);
//		}
		// generate a new message
		if (n_messages > 0) {
			int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);

			while (this.nodes.get(rnd).getNodeState() != NodeState.SUB) {
				rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
			}

			this.previus_sender = rnd;
			this.nodes.get(rnd).boolbroadcast = true;
			// creatorId + "_" + eventCounter;//
			String eventId = this.nodes.get(rnd).getId() + "_" + this.nodes.get(rnd).eventIdCounter;
			
			HashSet<Integer> receivers = new HashSet<Integer>();
			receivers.add(nodes.get(rnd).getId());
			this.messages.put(eventId, receivers);
			n_messages--;
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
	
	public void signalEventReception(String eventId, int receiver) {
		if (this.messages.containsKey(eventId)) {
			HashSet<Integer> receivers = this.messages.remove(eventId);
			receivers.add(receiver);
			if (receivers.size() == this.node_count) {
				String[] parts = eventId.split("_");
				int eventGeneratorNodeId = Integer.parseInt(parts[0]);
				this.nodes.get(eventGeneratorNodeId).setNewEventThisRoundet(false);
			}else {
				this.messages.put(eventId, receivers);
			}
		}
	}
	
	@ScheduledMethod(start = 150)
	public void asd() {
		for (String key : this.messages.keySet()) {
			HashSet<Integer> receivers = this.messages.get(key);
			if (receivers.size() != this.node_count) {
				System.out.println(key + " " + receivers.size());
			}
		}
	}
	
}
