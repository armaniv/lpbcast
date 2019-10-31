package lpbcast;

import java.util.ArrayList;

public class Message {

	private Node sender; 						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private ArrayList<String> eventIds; 		// the message's digest events list
	private ArrayList<Integer> sub; 				// the message's subscriptions list
	private ArrayList<Integer> unSub; 				// the message's un-subscriptions list

	public Message(Node sender, ArrayList<Event> events, ArrayList<String> eventIds, ArrayList<Integer> sub, 
			ArrayList<Integer> unSub) {
		this.sender = sender;
		this.events = events;
		this.eventIds = eventIds;
		this.sub = sub;
		this.unSub = unSub;
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


	public ArrayList<Integer> getSub() {
		return sub;
	}

	public ArrayList<Integer> getUnSub() {
		return unSub;
	}

}
