package ch.fork.adhocrailway.ui.bus.events;

public class ConnectedToRailwayEvent {

    private final boolean connected;

    public ConnectedToRailwayEvent(final boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

}
