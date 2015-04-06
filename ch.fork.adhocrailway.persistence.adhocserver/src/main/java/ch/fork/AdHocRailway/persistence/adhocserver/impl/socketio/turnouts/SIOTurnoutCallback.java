package ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.turnouts;

import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.IOCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SIOTurnoutCallback implements IOCallback {

    private static final Logger LOGGER = Logger
            .getLogger(SIOTurnoutCallback.class);

    private TurnoutServiceListener listener;
    private SIOService sioService;

    public SIOTurnoutCallback(SIOService sioService) {
        this.sioService = sioService;
    }

    public void init(final TurnoutServiceListener listener) {
        this.listener = listener;

        for (SIOTurnoutCallbackEvent sioTurnoutCallbackEvent : SIOTurnoutCallbackEvent.values()) {
            sioService.addIOCallback(sioTurnoutCallbackEvent.getEvent(), this);
        }
    }

    public void disconnect() {
        sioService.removeIOCallback(this);
    }

    @Override
    public synchronized void on(final String event,
                                final Object... jsonData) {

        final SIOTurnoutCallbackEvent serviceEvent = SIOTurnoutCallbackEvent
                .fromEvent(event);
        if (serviceEvent == null) {
            return;
        }

        LOGGER.info("on(message: " + event + ", args: " + jsonData.toString() + ")");
        try {
            switch (serviceEvent) {
                case TURNOUT_INIT:
                    SIOTurnoutCallbackEventHandler.handleTurnoutInit((JSONArray) jsonData[0], listener);
                    break;
                case TURNOUT_ADDED:
                    SIOTurnoutCallbackEventHandler
                            .handleTurnoutAdded((JSONObject) jsonData[0], listener);
                    break;
                case TURNOUT_GROUP_ADDED:
                    SIOTurnoutCallbackEventHandler.handleTurnoutGroupAdded((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_GROUP_REMOVED:
                    SIOTurnoutCallbackEventHandler.handleTurnoutGroupRemoved((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_GROUP_UPDATED:
                    SIOTurnoutCallbackEventHandler.handleTurnoutGroupUpdated((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_REMOVED:
                    SIOTurnoutCallbackEventHandler.handleTurnoutRemoved((JSONObject) jsonData[0],
                            listener);
                    break;
                case TURNOUT_UPDATED:
                    SIOTurnoutCallbackEventHandler.handleTurnoutUpdated((JSONObject) jsonData[0],
                            listener);
                    break;
                default:
                    listener.failure(new AdHocServiceException(
                            "unrecognized event '" + event + "' received"));
                    break;

            }
        } catch (final JSONException e) {
            listener.failure(new AdHocServiceException(
                    "error parsing event '" + event + "'", e));
        }
    }

    @Override
    public void onError(final Exception arg0) {
        listener.failure(new AdHocServiceException(
                "failure in communication with adhoc-server", arg0));
    }

    @Override
    public void onMessage(final String arg0) {
        LOGGER.info("onMessage(" + arg0 + ")");
    }

    @Override
    public void onMessage(final JSONObject arg0) {
        LOGGER.info("onMessage(" + arg0 + ")");

    }

}
