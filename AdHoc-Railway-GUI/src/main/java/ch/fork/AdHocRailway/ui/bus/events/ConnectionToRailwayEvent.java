package ch.fork.AdHocRailway.ui.bus.events;

public class ConnectionToRailwayEvent {

	private final boolean connected;

	public ConnectionToRailwayEvent(final boolean connected) {
		this.connected = connected;
	}

	public boolean isConnected() {
		return connected;
	}

}
