package lpbcast;

import org.apache.commons.lang3.RandomStringUtils;

public class Event {

	private Node creator; 			// the Event creator
	private String digest; 			// the event's digest (randomly computed)

	public Event(Node creator) {
		this.creator = creator;
		this.digest = RandomStringUtils.random(15, true, true);
	}

	public String getDigest() {
		return this.digest;
	}
}
