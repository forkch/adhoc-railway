package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

public enum SIOTurnoutCallbackEvent {
    // @formatter:off
    TURNOUT_INIT("turnout:init"),
    TURNOUT_ADDED("turnout:added"),
    TURNOUT_UPDATED("turnout:updated"),
    TURNOUT_REMOVED("turnout:removed"),

    TURNOUT_GROUP_ADDED("turnoutGroup:added"),
    TURNOUT_GROUP_UPDATED("turnoutGroup:updated"),
    TURNOUT_GROUP_REMOVED("turnoutGroup:removed"),

    TURNOUT_GROUP_ADD_REQUEST("turnoutGroup:add"),
    TURNOUT_GROUP_REMOVE_REQUEST("turnoutGroup:remove"),
    TURNOUT_GROUP_UPDATE_REQUEST("turnoutGroup:update"),
    TURNOUT_GROUP_GET_ALL_REQUEST("turnoutGroup:getAll"),

    TURNOUT_ADD_REQUEST("turnout:add"),
    TURNOUT_REMOVE_REQUEST("turnout:remove"),
    TURNOUT_UPDATE_REQUEST("turnout:update"),
    TURNOUT_CLEAR_REQUEST("turnout:clear");

    // @formatter:on
    private final String event;

    private SIOTurnoutCallbackEvent(String event) {
        this.event = event;
    }

    public static SIOTurnoutCallbackEvent fromEvent(String event2) {
        for (SIOTurnoutCallbackEvent e : values()) {
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
