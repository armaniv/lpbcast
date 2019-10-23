package lpbcast;

import java.util.Set;

public class Message {

	private Node sender; 					// the Message sender
	private Set<Event> events; 				// the message's events list
	private Set<String> eventIds; 			// the message's digest events list
	private Set<Node> sub; 					// the message's subscriptions list
	private Set<Node> unSub; 				// the message's un-subscriptions list

	public Message(Node sender, Set<Event> events, Set<String> eventIds, Set<Node> sub, Set<Node> unSub) {
		this.sender = sender;
		this.events = events;
		this.eventIds = eventIds;
		this.sub = sub;
		this.unSub = unSub;
	}

}
