package lpbcast;

public class Event {

	private Integer creator; 			// the Event creator
	private String id; 			// the event's digest (randomly computed)

	public Event(Integer creator, int eventCounter) {
		this.creator = creator;
		this.id = creator + "_" + eventCounter;
	}

	public String getId() {
		return this.id;
	}
	
	public Integer getCreator() {
		return this.creator;
	}
}
