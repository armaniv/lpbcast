package lpbcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AnalyzedMessage {
	public String id;
	public Integer creationRound;
	public HashMap<Integer, HashSet<Integer>> receiversPerRound;

	public AnalyzedMessage(String id, Integer creationRound) {
		this.id = id;
		this.creationRound = creationRound;
		this.receiversPerRound = new HashMap<Integer, HashSet<Integer>>();
	}

	public void addReceiverAtRound(Integer receiver, Integer round) {
		if (this.receiversPerRound.containsKey(round)) {
			HashSet<Integer> receivers = this.receiversPerRound.get(round);
			receivers.add(receiver);
		} else {
			HashSet<Integer> receivers = new HashSet<Integer>();
			receivers.add(receiver);
			this.receiversPerRound.put(round, receivers);
		}
	}

	public static AnalyzedMessage find(ArrayList<AnalyzedMessage> list, String msgId) {
		for (AnalyzedMessage m : list) {
			if (m.id.equals(msgId)) {
				return m;
			}
		}
		return null;
	}
}
