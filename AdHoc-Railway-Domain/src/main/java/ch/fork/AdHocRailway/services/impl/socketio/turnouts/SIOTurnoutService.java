package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SIOTurnoutService implements IOCallback {

    private static final Logger LOGGER = Logger
            .getLogger(SIOTurnoutService.class);

    private TurnoutServiceListener listener;
    private SIOService sioService;

    public SIOTurnoutService() {
    }

    public void init(final TurnoutServiceListener listener) {
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

        final SIOTurnoutServiceEvent serviceEvent = SIOTurnoutServiceEvent
                .fromEvent(event);
        if (serviceEvent == null) {
            return;
        }

        LOGGER.info("on(message: " + event + ", args: " + jsonData + ")");
        try {
            switch (serviceEvent) {
                case TURNOUT_INIT:
                    SIOTurnoutServiceEventHandler.handleTurnoutInit((JSONArray) jsonData[0], listener);
                    break;
                case TURNOUT_ADDED:
                    SIOTurnoutServiceEventHandler
                            .handleTurnoutAdded((JSONObject) jsonData[0], listener);
                    break;
                case TURNOUT_GROUP_ADDED:
                    SIOTurnoutServiceEventHandler.handleTurnoutGroupAdded((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_GROUP_REMOVED:
                    SIOTurnoutServiceEventHandler.handleTurnoutGroupRemoved((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_GROUP_UPDATED:
                    SIOTurnoutServiceEventHandler.handleTurnoutGroupUpdated((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_REMOVED:
                    SIOTurnoutServiceEventHandler.handleTurnoutRemoved((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_UPDATED:
                    SIOTurnoutServiceEventHandler.handleTurnoutUpdated((JSONObject) jsonData[0],
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
