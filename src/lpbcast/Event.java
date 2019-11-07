package lpbcast;

public class Event implements Comparable {

	private Integer creatorId; 	// the Event creator
	private Integer eventId;	// the incremental identifier of a event (each node has its own)
	private String id; 			// the identifier of a event (creatorId + eventId)
	private int age; 			// the event's age

	public Event(Integer creatorId, int eventCounter) {
		this.creatorId = creatorId;
		this.eventId = eventCounter;
		this.id = creatorId + "_" + eventCounter;
		this.age = 0;
	}

	public String getId() {
		return this.id;
	}

	public Integer getCreatorId() {
		return this.creatorId;
	}

	public int getAge() {
		return this.age;
	}

	public void incrementAge() {
		this.age++;
	}

	public void updateAge(int age) {
		this.age = age;
	}

	public int getEventId() {
		return this.eventId;
	}

	@Override
	public int compareTo(Object o) {
		Event e = null;
		if (!(o instanceof Event)) {
			return -1;
		}
		e = (Event) o;
		if (this.eventId > e.getEventId()) {
			return 1;
		} else if (this.eventId == e.getEventId()) {
			return 0;
		} else {
			return -1;
		}
	}
}
