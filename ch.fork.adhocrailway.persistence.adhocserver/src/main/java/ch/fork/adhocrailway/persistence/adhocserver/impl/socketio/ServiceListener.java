package ch.fork.adhocrailway.persistence.adhocserver.impl.socketio;

import ch.fork.adhocrailway.services.AdHocServiceException;

public interface ServiceListener {
    public void connected();

    public void connectionError(final AdHocServiceException ex);

    public void disconnected();
}
