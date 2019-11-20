package lpbcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Message {

	private Integer sender;						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private ArrayList<String> eventIds; 		// the message's digest events list
	private ArrayList<Membership> subs; 		// the message's subscriptions list
	private ArrayList<Unsubscription> unSubs; 	// the message's un-subscriptions list

	public Message(Integer sender, ArrayList<Event> events, ArrayList<String> eventIds, ArrayList<Membership> subs,
			ArrayList<Unsubscription> unSubs) {
		this.sender = Integer.valueOf(sender);
		copyEventIds(eventIds);
		copyEvents(events);
		copySubs(subs);
		copyUnsubs(unSubs);
	}

	public Integer getSender() {
		return sender;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public ArrayList<String> getEventIds() {
		return eventIds;
	}

	public ArrayList<Membership> getSubs() {
		return subs;
	}

	public ArrayList<Unsubscription> getUnSubs() {
		return unSubs;
	}
	
	private void copyEvents(ArrayList<Event> events){
		ArrayList<Event> msgEvents = new ArrayList<Event>();
		for (Event e : events) {
			msgEvents.add(new Event(e.getCreatorId(), e.getEventId()));
		}
		this.events = msgEvents;
	}
	
	private void copyEventIds(ArrayList<String> eventIds) {
		ArrayList<String> msgEventIds = new ArrayList<String>();
		for (String s : eventIds) {
			msgEventIds.add(new String(s));
		}
		this.eventIds = msgEventIds;
	}
	
	private void copySubs(ArrayList<Membership> subs) {
		ArrayList<Membership> msgSubs = new ArrayList<Membership>();
		for (Membership m : subs) {
			msgSubs.add(new Membership(m.getNodeId(), m.getFrequency()));
		}
		this.subs = msgSubs;
	}
	
	private void copyUnsubs(ArrayList<Unsubscription> unsubs) {
		ArrayList<Unsubscription> msgUnsubs = new ArrayList<Unsubscription>();
		for (Unsubscription m : unsubs) {
			msgUnsubs.add(new Unsubscription(m.getNodeId(), m.getAge()));
		}
		this.unSubs = msgUnsubs;
	}
}