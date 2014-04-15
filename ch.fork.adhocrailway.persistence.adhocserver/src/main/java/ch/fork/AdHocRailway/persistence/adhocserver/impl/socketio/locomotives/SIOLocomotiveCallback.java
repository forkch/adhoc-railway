package ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.locomotives;

import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SIOLocomotiveCallback implements IOCallback {

    private static final Logger LOGGER = Logger
            .getLogger(SIOLocomotiveCallback.class);

    private LocomotiveServiceListener listener;
    private SIOService sioService;


    public SIOLocomotiveCallback() {
    }

    public void init(final LocomotiveServiceListener listener) {
        this.listener = listener;

        sioService = SIOService.getInstance();
        sioService.addIOCallback(this);
    }

    public void disconnect() {
        sioService.removeIOCallback(this);
    }

    @Override
    public synchronized void on(final String event, final IOAcknowledge arg1,
                                final Object... jsonData) {

        final SIOLocomotiveCallbackEvent serviceEvent = SIOLocomotiveCallbackEvent
                .fromEvent(event);
        if (serviceEvent == null) {
            return;
        }
        LOGGER.info("on(message: " + event + ", args: " + jsonData + ")");
        try {
            switch (serviceEvent) {
                case LOCOMOTIVE_INIT:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveInit((JSONArray) (jsonData[0]),
                            listener);
                    break;
                case LOCOMOTIVE_ADDED:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveAdded((JSONObject) (jsonData[0]),
                            listener);
                    break;
                case LOCOMOTIVE_GROUP_ADDED:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveGroupAdded(
                            (JSONObject) (jsonData[0]), listener);
                    break;
                case LOCOMOTIVE_GROUP_REMOVED:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveGroupRemoved(
                            (JSONObject) (jsonData[0]), listener);
                    break;
                case LOCOMOTIVE_GROUP_UPDATED:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveGroupUpdated(
                            (JSONObject) (jsonData[0]), listener);
                    break;
                case LOCOMOTIVE_REMOVED:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveRemoved((JSONObject) (jsonData[0]),
                            listener);
                    break;
                case LOCOMOTIVE_UPDATED:
                    SIOLocomotiveCallbackEventHandler.handleLocomotiveUpdated((JSONObject) (jsonData[0]),
                            listener);
                    break;
                default:
                    listener.failure(new AdHocServiceException(
                            "unregonized event '" + event + "' received"));
                    break;

            }
        } catch (final JSONException e) {
            listener.failure(new AdHocServiceException(
                    "error parsing event '" + event + "'", e));
        }
    }

    @Override
    public void onConnect() {
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onError(final SocketIOException arg0) {
        listener.failure(new AdHocServiceException(
                "failure in communication with adhoc-server", arg0));
    }

    @Override
    public void onMessage(final String arg0, final IOAcknowledge arg1) {
        LOGGER.info("onMessage(" + arg0 + ")");
    }

    @Override
    public void onMessage(final JSONObject arg0, final IOAcknowledge arg1) {
        LOGGER.info("onMessage(" + arg0 + ")");

    }

}
