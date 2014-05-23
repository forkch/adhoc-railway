package ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.turnouts;

import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.RouteServiceListener;
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
        final SIORouteCallbackEvent serviceEvent = SIORouteCallbackEvent
                .fromEvent(event);

        if (serviceEvent == null) {
            return;
        }
        LOGGER.info("on(message: " + event + ", args: " + jsonData.toString() + ")");
        try {
            switch (serviceEvent) {
                case ROUTE_INIT:
                    SIORouteCallbackEventHandler.handleRouteInit((JSONArray) jsonData[0], listener);
                    break;
                case ROUTE_ADDED:
                    SIORouteCallbackEventHandler.handleRouteAdded((JSONObject) jsonData[0], listener);
                    break;
                case ROUTE_GROUP_ADDED:
                    SIORouteCallbackEventHandler.handleRouteGroupAdded((JSONObject) jsonData[0],
                            listener);
                    break;
                case ROUTE_GROUP_REMOVED:
                    SIORouteCallbackEventHandler.handleRouteGroupRemoved((JSONObject) jsonData[0],
                            listener);
                    break;
                case ROUTE_GROUP_UPDATED:
                    SIORouteCallbackEventHandler.handleRouteGroupUpdated((JSONObject) jsonData[0],
                            listener);
                    break;
                case ROUTE_REMOVED:
                    SIORouteCallbackEventHandler.handleRouteRemoved((JSONObject) jsonData[0], listener);
                    break;
                case ROUTE_UPDATED:
                    SIORouteCallbackEventHandler.handleRouteUpdated((JSONObject) jsonData[0], listener);
                    break;
                default:
                    break;
            }
        } catch (final JSONException e) {
            e.printStackTrace();
            listener.failure(new AdHocServiceException("error parsing event '"
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
