package lpbcast;

import java.util.ArrayList;
import java.util.UUID;

public class Message {

	private Node sender; 						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private ArrayList<UUID> eventIds; 		// the message's digest events list
	private ArrayList<Node> sub; 				// the message's subscriptions list
	private ArrayList<Node> unSub; 				// the message's un-subscriptions list

	public Message(Node sender, ArrayList<Event> events, ArrayList<UUID> eventIds, ArrayList<Node> sub, 
			ArrayList<Node> unSub) {
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

	public ArrayList<UUID> getEventIds() {
		return eventIds;
	}


	public ArrayList<Node> getSub() {
		return sub;
	}

	public ArrayList<Node> getUnSub() {
		return unSub;
	}

}
