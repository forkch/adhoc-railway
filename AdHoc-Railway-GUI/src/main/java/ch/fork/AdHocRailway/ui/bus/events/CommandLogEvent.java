package ch.fork.AdHocRailway.ui.bus.events;

public class CommandLogEvent {

    private final String message;

    public CommandLogEvent(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
