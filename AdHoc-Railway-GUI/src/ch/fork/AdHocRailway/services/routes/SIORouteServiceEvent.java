package ch.fork.AdHocRailway.services.routes;

public enum SIORouteServiceEvent {
	// @formatter:off
	ROUTE_INIT("route:init"),
	ROUTE_ADDED("route:added"),
	ROUTE_UPDATED("route:updated"),
	ROUTE_REMOVED("route:removed"),
	ROUTE_GROUP_ADDED("routeGroup:added"),
	ROUTE_GROUP_UPDATED("routeGroup:updated"),
	ROUTE_GROUP_REMOVED("routeGroup:removed");
	// @formatter:on
	private final String event;

	private SIORouteServiceEvent(String event) {
		this.event = event;
	}

	public static SIORouteServiceEvent fromEvent(String event2) {
		for (SIORouteServiceEvent e : values()) {
			if (e.event.equalsIgnoreCase(event2)) {
				return e;
			}
		}
		return null;
	}
}
