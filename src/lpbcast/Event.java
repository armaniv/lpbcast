package lpbcast;

public class Event {

	private Node creator; 			// the Event creator
	private String id; 			// the event's digest (randomly computed)

	public Event(Node creator, int eventCounter) {
		this.creator = creator;
		this.id = creator.getId() + "_" + eventCounter;
	}

	public String getId() {
		return this.id;
	}
}
