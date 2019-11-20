package lpbcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lpbcast.SchedulableActions.*;
import lpbcast.AnalyzedMessage;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class AppNode {

	private int node_count; 				// the number of nodes in the context
	private HashMap<Integer, Node> nodes; 	// the nodes in the context
	private int n_messages; 				// the number of messages that we want in the simulation
	private int msg_per_round;				// the number of messages to be generated per round
	private int churn_rate; 				// the churn rate that we want in the simulation
	private int unsub_rate; 				// the unsub rate that we want in the simulation
	
	// contains the messages that are broadcasted in the current round
	// the key is the id of the message and the element contains a set
	// of the ids of the nodes that have received that message
	private HashMap<String, HashSet<Integer>> messages;

	// --- variables for statistics
	private ArrayList<AnalyzedMessage> analyzed_messages;
	private int analyzed_n_messages = 50;
	private int analyze_start_consider_messages_at_round = 100;
	private HashMap<String, Integer> from_ratio;

	public AppNode(int node_count, int n_messages, int msg_per_round, int churn_rate, int unsub_rate) {
		this.node_count = node_count;
		this.nodes = new HashMap<Integer, Node>();
		this.messages = new HashMap<String, HashSet<Integer>>();
		// this.analyzedInfectedPerRound = new HashMap<Integer, HashSet<Integer>>();
		this.analyzed_messages = new ArrayList<AnalyzedMessage>();
		this.n_messages = n_messages;
		this.churn_rate = churn_rate;
		this.unsub_rate = unsub_rate;
		this.msg_per_round = msg_per_round;
		from_ratio = new HashMap<String, Integer>();

		from_ratio.put("gossip", 0);
		from_ratio.put("rnd", 0);
		from_ratio.put("source", 0);
		from_ratio.put("sender", 0);
		from_ratio.put("self", 0);
	}

	public void addNode(Node node) {
		this.nodes.put(node.getId(), node);
	}

	/**
	 * Periodic function which tells to a random node to generate an event. For
	 * visualization purpose manage also the node's variable newEventThisRound
	 */
	@ScheduledMethod(start = 3, interval = 2, priority = 3)
	public void generateBroadcast() {
		// generate a new message
		if (n_messages > 0) {
			for (int i = 0; i < msg_per_round && n_messages > 0; i++) {
				int rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);

				while (this.nodes.get(rnd).getNodeState() != NodeState.SUB) {
					rnd = RandomHelper.nextIntFromTo(0, this.node_count - 1);
				}

				Node receiver = this.nodes.get(rnd);
				String eventId = receiver.broadcast();
				HashSet<Integer> receivers = new HashSet<Integer>();
				this.messages.put(eventId, receivers);
				receiver.deliver(eventId, "self");
				n_messages--;
				int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

				if (tick >= this.analyze_start_consider_messages_at_round && this.analyzed_n_messages > 0) {
					AnalyzedMessage message = new AnalyzedMessage(eventId, receiver.getCurrentRound());
					this.analyzed_messages.add(message);
					this.analyzed_n_messages--;
				}
			}
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

	public void signalEventReception(String eventId, int receiver, int nodeRound, String from) {
		if (this.messages.containsKey(eventId)) {
			HashSet<Integer> receivers = this.messages.get(eventId);
			receivers.add(receiver);

			if (receivers.size() == this.node_count) {
				this.messages.remove(eventId);
				String[] parts = eventId.split("_");
				int eventGeneratorNodeId = Integer.parseInt(parts[0]);
				this.nodes.get(eventGeneratorNodeId).deleteNew(eventId);

			} else {
				this.messages.put(eventId, receivers);
			}
		}

		Integer from_where = this.from_ratio.get(from);
		this.from_ratio.put(from, from_where + 1);

		AnalyzedMessage m = AnalyzedMessage.find(analyzed_messages, eventId);
		if (m != null) {
			int creationRound = m.creationRound;
			int passedRounds = Math.abs(nodeRound - creationRound);
			m.addReceiverAtRound(receiver, passedRounds);
		}
	}

	@ScheduledMethod(start = 400, interval = 0)
	public void computeExpectedInfectedProcesses() {
		HashMap<Integer, ArrayList<Integer>> receiversPerRound = new HashMap<Integer, ArrayList<Integer>>();

		for (AnalyzedMessage m : this.analyzed_messages) {
			for (Integer round : m.receiversPerRound.keySet()) {
				if (!receiversPerRound.containsKey(round)) {
					ArrayList<Integer> statistics = new ArrayList<Integer>(2);
					statistics.add(0, m.receiversPerRound.get(round).size());
					statistics.add(1, 1);
					receiversPerRound.put(round, statistics);
				} else {
					ArrayList<Integer> statistics = receiversPerRound.get(round);
					statistics.set(0, statistics.get(0) + m.receiversPerRound.get(round).size());
					statistics.set(1, statistics.get(1) + 1);
					receiversPerRound.put(round, statistics);
				}
			}
		}

		System.out.println();
		System.out.println("Expected #InfectedProcesses per Round:");
		double prev_avg = 0;
		for (Integer round : receiversPerRound.keySet()) {
			if (prev_avg >= this.node_count)
				break;
			double sum = receiversPerRound.get(round).get(0);
			double counters = receiversPerRound.get(round).get(1);
			double avg = (double) (sum / counters);
			prev_avg = prev_avg + Double.valueOf(avg);
			System.out.println("round: " + round + " avg: " + prev_avg);
		}

		System.out.println();
		System.out.println("Deliver Type Ratio:");
		System.out.println(this.from_ratio.toString());
	}

	public String receivedBy(String eId) {
		return this.messages.get(eId).toString();
	}
}