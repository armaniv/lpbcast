package lpbcast;

import java.util.ArrayList;
import java.util.HashMap;

public class EventIds {
	private HashMap<Integer, ArrayList<Integer>> eventIds;

	public EventIds() {
		this.eventIds = new HashMap<Integer, ArrayList<Integer>>();
	}

	public EventIds(HashMap<Integer, ArrayList<Integer>> eventIds) {
		this.eventIds = new HashMap<Integer, ArrayList<Integer>>();
		this.eventIds.putAll(eventIds);
	}

	public boolean contains(Integer nodeId, Integer eventId) {
		ArrayList<Integer> events = this.eventIds.get(nodeId);
		if (events == null) {
			return false;
		}
		Integer lastInSeq = events.get(0);
		if (eventId <= lastInSeq) {
			return true;
		} else if (events.size() == 1) {
			return false;
		}
		Integer size = events.size();
		Integer biggestId = events.get(size - 1);
		if (eventId > biggestId) {
			return false;
		}
		for (int i = size - 1; i >= 1; i--) {
			Integer outOfSeq = events.get(i);
			if (eventId.equals(outOfSeq)) {
				return true;
			}
		}
		return false;
	}

	public void add(Integer nodeId, Integer eventId) {
		ArrayList<Integer> events = this.eventIds.get(nodeId);
		if (events == null) {
			events = new ArrayList<Integer>();
			events.add(eventId);
			this.eventIds.put(nodeId, events);
			return;
		} else if (events.size() == 1) {
			events.add(eventId);
		} else {
			int size = events.size();
			for (int i = size - 1; i >= 0; i--) {
				Integer event = events.get(i);
				if (eventId > event) {
					events.add(i + 1, eventId);
					break;
				}
			}
		}
		Integer lastInSeq = events.get(0);
		int i = 1;
		while (i == 1) {
			Integer event = events.get(i);
			if (event.equals(lastInSeq + 1)) {
				lastInSeq = event;
				events.remove(0);
				if (events.size() == 1) {
					i++;
				}
			} else {
				i++;
			}
		}
		this.eventIds.put(nodeId, events);
	}

	public HashMap<Integer, ArrayList<Integer>> getMap() {
		return this.eventIds;
	}

	public void clear() {
		this.eventIds.clear();
	}

	public void log() {
		System.out.println(this.eventIds.toString());
	}
}
