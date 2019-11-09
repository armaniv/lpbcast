package lpbcast;

import java.util.ArrayList;

public class Message {

	private Integer sender;						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private ArrayList<String> eventIds; 		// the message's digest events list
	private ArrayList<Membership> subs; 		// the message's subscriptions list
	private ArrayList<Unsubscription> unSubs; 	// the message's un-subscriptions list
	private static int id = 0;

	public Message(Integer sender, ArrayList<Event> events, ArrayList<String> eventIds, ArrayList<Membership> subs,
			ArrayList<Unsubscription> unSubs) {
		this.sender = sender;
		this.events = events;
		this.eventIds = eventIds;
		this.subs = subs;
		this.unSubs = unSubs;
		this.id++;
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

	
	public String toString() {
		String ato = "";
		for (Event e : this.events) {
			ato = e.getId() + " ";
		}
			
		return ato;
	}
}
