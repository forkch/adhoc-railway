package ch.fork.AdHocRailway.services.impl.socketio;

import ch.fork.AdHocRailway.AdHocRailwayException;

public interface ServiceListener {
	public void connected();

	public void connectionError(final AdHocRailwayException ex);

	public void disconnected();
}
