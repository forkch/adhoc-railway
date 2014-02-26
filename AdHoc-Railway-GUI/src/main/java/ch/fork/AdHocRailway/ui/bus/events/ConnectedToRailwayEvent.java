package ch.fork.AdHocRailway.ui.bus.events;

public class ConnectedToRailwayEvent {

    private final boolean connected;

    public ConnectedToRailwayEvent(final boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

}
