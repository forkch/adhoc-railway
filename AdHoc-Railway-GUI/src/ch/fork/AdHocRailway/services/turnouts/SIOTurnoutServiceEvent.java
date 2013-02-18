package ch.fork.AdHocRailway.services.turnouts;

public enum SIOTurnoutServiceEvent {
	// @formatter:off
	TURNOUT_INIT("turnout:init"),
	TURNOUT_ADDED("turnout:added"),
	TURNOUT_UPDATED("turnout:updated"),
	TURNOUT_REMOVED("turnout:removed"),
	TURNOUT_GROUP_ADDED("turnoutGroup:added"),
	TURNOUT_GROUP_UPDATED("turnoutGroup:updated"),
	TURNOUT_GROUP_REMOVED("turnoutGroup:removed");
	// @formatter:on
	private final String event;

	private SIOTurnoutServiceEvent(String event) {
		this.event = event;
	}

	public static SIOTurnoutServiceEvent fromEvent(String event2) {
		for (SIOTurnoutServiceEvent e : values()) {
			if (e.event.equalsIgnoreCase(event2)) {
				return e;
			}
		}
		return null;
	}
}
