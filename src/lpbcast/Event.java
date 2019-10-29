package lpbcast;

import java.util.UUID;

public class Event {

	private Node creator; 			// the Event creator
	private UUID id; 			// the event's digest (randomly computed)

	public Event(Node creator) {
		this.creator = creator;
		this.id = UUID.randomUUID();
	}

	public UUID getId() {
		return this.id;
	}
}
