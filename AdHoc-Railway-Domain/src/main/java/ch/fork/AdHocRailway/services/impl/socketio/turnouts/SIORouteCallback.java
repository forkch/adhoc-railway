package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.services.RouteServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SIORouteCallback implements IOCallback {

    private static final Logger LOGGER = Logger
            .getLogger(SIORouteCallback.class);

    private RouteServiceListener listener;
    private SIOService sioService;

    public SIORouteCallback() {
    }

    public void init(final RouteServiceListener listener) {
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
        final SIORouteServiceEvent serviceEvent = SIORouteServiceEvent
                .fromEvent(event);

        if (serviceEvent == null) {
            return;
        }
        LOGGER.info("on(message: " + event + ", args: " + jsonData.toString() + ")");
        try {
            switch (serviceEvent) {
                case ROUTE_INIT:
                    SIORouteServiceEventHandler.handleRouteInit((JSONArray) jsonData[0], listener);
                    break;
                case ROUTE_ADDED:
                    SIORouteServiceEventHandler.handleRouteAdded((JSONObject) jsonData[0], listener);
                    break;
                case ROUTE_GROUP_ADDED:
                    SIORouteServiceEventHandler.handleRouteGroupAdded((JSONObject) jsonData[0],
                            listener);
                    break;
                case ROUTE_GROUP_REMOVED:
                    SIORouteServiceEventHandler.handleRouteGroupRemoved((JSONObject) jsonData[0],
                            listener);
                    break;
                case ROUTE_GROUP_UPDATED:
                    SIORouteServiceEventHandler.handleRouteGroupUpdated((JSONObject) jsonData[0],
                            listener);
                    break;
                case ROUTE_REMOVED:
                    SIORouteServiceEventHandler.handleRouteRemoved((JSONObject) jsonData[0], listener);
                    break;
                case ROUTE_UPDATED:
                    SIORouteServiceEventHandler.handleRouteUpdated((JSONObject) jsonData[0], listener);
                    break;
                default:
                    break;
            }
        } catch (final JSONException e) {
            e.printStackTrace();
            listener.failure(new ManagerException("error parsing event '"
                    + event + "'"));
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
