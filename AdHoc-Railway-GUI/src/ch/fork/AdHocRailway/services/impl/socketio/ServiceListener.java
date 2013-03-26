package ch.fork.AdHocRailway.services.impl.socketio;

public interface ServiceListener {
	public void connected();

	public void connectionError(Exception ex);

	public void disconnected();
}
