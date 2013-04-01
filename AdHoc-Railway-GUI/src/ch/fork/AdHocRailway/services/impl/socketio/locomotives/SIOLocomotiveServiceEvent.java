package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

public enum SIOLocomotiveServiceEvent {
	// @formatter:off
	LOCOMOTIVE_INIT("locomotive:init"),
	LOCOMOTIVE_ADDED("locomotive:added"),
	LOCOMOTIVE_UPDATED("locomotive:updated"),
	LOCOMOTIVE_REMOVED("locomotive:removed"),

	LOCOMOTIVE_GROUP_ADDED("locomotiveGroup:added"),
	LOCOMOTIVE_GROUP_UPDATED("locomotiveGroup:updated"),
	LOCOMOTIVE_GROUP_REMOVED("locomotiveGroup:removed"),

	LOCOMOTIVE_GROUP_ADD_REQUEST("locomotiveGroup:add"),
	LOCOMOTIVE_GROUP_REMOVE_REQUEST("locomotiveGroup:remove"),
	LOCOMOTIVE_GROUP_UPDATE_REQUEST("locomotiveGroup:update"),
	LOCOMOTIVE_GROUP_GET_ALL_REQUEST("locomotiveGroup:getAll"),

	LOCOMOTIVE_ADD_REQUEST("locomotive:add"),
	LOCOMOTIVE_REMOVE_REQUEST("locomotive:remove"),
	LOCOMOTIVE_UPDATE_REQUEST("locomotive:update"),
	LOCOMOTIVE_CLEAR_REQUEST("locomotive:clear");

	// @formatter:on
	private final String event;

	private SIOLocomotiveServiceEvent(final String event) {
		this.event = event;
	}

	public static SIOLocomotiveServiceEvent fromEvent(final String event2) {
		for (final SIOLocomotiveServiceEvent e : values()) {
			if (e.getEvent().equalsIgnoreCase(event2)) {
				return e;
			}
		}
		return null;
	}

	public String getEvent() {
		return event;
	}
}
