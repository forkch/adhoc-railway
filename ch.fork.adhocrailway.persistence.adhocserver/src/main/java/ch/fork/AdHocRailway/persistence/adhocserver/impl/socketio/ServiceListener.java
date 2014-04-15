package ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio;

import ch.fork.AdHocRailway.services.AdHocServiceException;

public interface ServiceListener {
    public void connected();

    public void connectionError(final AdHocServiceException ex);

    public void disconnected();
}
