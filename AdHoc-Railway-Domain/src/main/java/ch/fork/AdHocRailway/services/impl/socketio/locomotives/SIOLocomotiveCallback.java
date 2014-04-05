package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
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

        final SIOLocomotiveServiceEvent serviceEvent = SIOLocomotiveServiceEvent
                .fromEvent(event);
        if (serviceEvent == null) {
            return;
        }
        LOGGER.info("on(message: " + event + ", args: " + jsonData + ")");
        try {
            switch (serviceEvent) {
                case LOCOMOTIVE_INIT:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveInit((JSONArray) (jsonData[0]),
                            listener);
                    break;
                case LOCOMOTIVE_ADDED:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveAdded((JSONObject) (jsonData[0]),
                            listener);
                    break;
                case LOCOMOTIVE_GROUP_ADDED:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveGroupAdded(
                            (JSONObject) (jsonData[0]), listener);
                    break;
                case LOCOMOTIVE_GROUP_REMOVED:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveGroupRemoved(
                            (JSONObject) (jsonData[0]), listener);
                    break;
                case LOCOMOTIVE_GROUP_UPDATED:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveGroupUpdated(
                            (JSONObject) (jsonData[0]), listener);
                    break;
                case LOCOMOTIVE_REMOVED:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveRemoved((JSONObject) (jsonData[0]),
                            listener);
                    break;
                case LOCOMOTIVE_UPDATED:
                    SIOLocomotiveServiceEventHandler.handleLocomotiveUpdated((JSONObject) (jsonData[0]),
                            listener);
                    break;
                default:
                    listener.failure(new ManagerException(
                            "unregonized event '" + event + "' received"));
                    break;

            }
        } catch (final JSONException e) {
            listener.failure(new ManagerException(
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
        listener.failure(new ManagerException(
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
