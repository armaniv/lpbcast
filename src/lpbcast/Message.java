package lpbcast;

import java.util.ArrayList;

public class Message {

	private Node sender; 						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private ArrayList<String> eventIds; 		// the message's digest events list
	private ArrayList<Integer> subs; 				// the message's subscriptions list
	private ArrayList<Unsubscription> unSubs; 				// the message's un-subscriptions list

	public Message(Node sender, ArrayList<Event> events, ArrayList<String> eventIds, ArrayList<Integer> subs, 
			ArrayList<Unsubscription> unSubs) {
		this.sender = sender;
		this.events = events;
		this.eventIds = eventIds;
		this.subs = subs;
		this.unSubs = unSubs;
	}

	public Node getSender() {
		return sender;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public ArrayList<String> getEventIds() {
		return eventIds;
	}


	public ArrayList<Integer> getSubs() {
		return subs;
	}

	public ArrayList<Unsubscription> getUnSubs() {
		return unSubs;
	}

}
