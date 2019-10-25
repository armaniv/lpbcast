package lpbcast;

import java.util.ArrayList;

public class Message {

	private Node sender; 						// the Message sender
	private ArrayList<Event> events; 			// the message's events list
	private ArrayList<String> eventIds; 		// the message's digest events list
	private ArrayList<Node> sub; 				// the message's subscriptions list
	private ArrayList<Node> unSub; 				// the message's un-subscriptions list

	public Message(Node sender, ArrayList<Event> events, ArrayList<String> eventIds, ArrayList<Node> sub, 
			ArrayList<Node> unSub) {
		this.sender = sender;
		this.events = events;
		this.eventIds = eventIds;
		this.sub = sub;
		this.unSub = unSub;
	}

}
