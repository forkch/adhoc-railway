package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

public enum SIORouteServiceEvent {
    // @formatter:off
    ROUTE_INIT("route:init"),
    ROUTE_ADDED("route:added"),
    ROUTE_UPDATED("route:updated"),
    ROUTE_REMOVED("route:removed"),
    ROUTE_GROUP_ADDED("routeGroup:added"),
    ROUTE_GROUP_UPDATED("routeGroup:updated"),
    ROUTE_GROUP_REMOVED("routeGroup:removed"),

    ROUTE_GROUP_ADD_REQUEST("routeGroup:add"),
    ROUTE_GROUP_REMOVE_REQUEST("routeGroup:remove"),
    ROUTE_GROUP_UPDATE_REQUEST("routeGroup:update"),

    ROUTE_GROUP_GET_ALL_REQUEST("routeGroup:getAll"),

    ROUTE_ADD_REQUEST("route:add"),

    ROUTE_REMOVE_REQUEST("route:remove"),

    ROUTE_UPDATE_REQUEST("route:update"),
    ROUTE_CLEAR_REQUEST("route:clear");
    // @formatter:on
    private final String event;

    private SIORouteServiceEvent(String event) {
        this.event = event;
    }

    public static SIORouteServiceEvent fromEvent(String event2) {
        for (SIORouteServiceEvent e : values()) {
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
