package lpbcast;

import java.util.ArrayList;

public class Message {

	private Integer sender;						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private EventIds eventIds; 		// the message's digest events list
	private ArrayList<Membership> subs; 		// the message's subscriptions list
	private ArrayList<Unsubscription> unSubs; 	// the message's un-subscriptions list

	public Message(Integer sender, ArrayList<Event> events, EventIds eventIds, ArrayList<Membership> subs,
			ArrayList<Unsubscription> unSubs) {
		this.sender = Integer.valueOf(sender);
		this.eventIds = new EventIds(eventIds.getMap());
		this.events = new ArrayList<Event>(events);
		this.subs = new ArrayList<Membership>(subs);
		this.unSubs = new ArrayList<Unsubscription>(unSubs);
	}

	public Integer getSender() {
		return sender;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public EventIds getEventIds() {
		return eventIds;
	}

	public ArrayList<Membership> getSubs() {
		return subs;
	}

	public ArrayList<Unsubscription> getUnSubs() {
		return unSubs;
	}
}
