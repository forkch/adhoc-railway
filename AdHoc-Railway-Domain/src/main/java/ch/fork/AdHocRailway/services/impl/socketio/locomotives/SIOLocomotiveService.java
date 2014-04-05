package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.UUID;

public class SIOLocomotiveService implements IOCallback {

    private static final Logger LOGGER = Logger
            .getLogger(SIOLocomotiveService.class);

    private LocomotiveServiceListener listener;
    private SIOService sioService;


    public SIOLocomotiveService() {
    }

    public void init(final LocomotiveServiceListener listener) {
        this.listener = listener;

        sioService = SIOService.getInstance();
        sioService.addIOCallback(this);
    }

    /*@Override
    public void init(final LocomotiveServiceListener listener) {
        this.listener = listener;

        sioService = SIOService.getInstance();
        sioService.addIOCallback(this);
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public void clear() {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_CLEAR_REQUEST);
        sioService.checkSocket();
        final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

            @Override
            public void ack(final Object... arg0) {
                LOGGER.info("ack: " + Arrays.toString(arg0));
                final Boolean err = (Boolean) arg0[0];
                final String msg = (String) arg0[1];
                if (err) {
                    listener.failure(new ManagerException(msg));
                } else {
                    final JSONArray data = (JSONArray) arg0[2];
                    try {
                        SIOLocomotiveServiceEventHandler.handleLocomotiveInit(
                                data, listener);
                    } catch (final JSONException e) {
                        listener.failure(new ManagerException(
                                "error clearing locomotives", e));
                    }
                }
            }
        };

        sioService.getSocket().emit(
                SIOLocomotiveServiceEvent.LOCOMOTIVE_CLEAR_REQUEST.getEvent(),
                ioAcknowledge, "");
    }

    @Override
    public void addLocomotive(final Locomotive locomotive) {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_ADD_REQUEST);
        sioService.checkSocket();
        try {
            final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

                @Override
                public void ack(final Object... arg0) {
                    LOGGER.info("ack: " + Arrays.toString(arg0));
                    final Boolean err = (Boolean) arg0[0];
                    final String msg = (String) arg0[1];
                    if (err) {
                        listener.failure(new ManagerException(msg));
                    } else {
                        final String sioId = (String) arg0[2];
                        SIOLocomotiveServiceEventHandler.addIdToLocomotive(
                                locomotive, sioId);
                        listener.locomotiveAdded(locomotive);
                    }
                }
            };

            final JSONObject addLocomotiveJson = SIOLocomotiveMapper
                    .mapLocomotiveToJSON(locomotive);

            sioService
                    .getSocket()
                    .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_ADD_REQUEST
                                    .getEvent(),
                            ioAcknowledge, addLocomotiveJson
                    );
        } catch (final JSONException e) {
            throw new ManagerException("error adding turnout", e);
        }

    }

    @Override
    public void removeLocomotive(final Locomotive turnout) {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_REMOVE_REQUEST);
        sioService.checkSocket();
        try {
            final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

                @Override
                public void ack(final Object... arg0) {
                    LOGGER.info("ack: " + Arrays.toString(arg0));
                    final Boolean err = (Boolean) arg0[0];
                    final String msg = (String) arg0[1];
                    if (err) {
                        listener.failure(new ManagerException(msg));
                    } else {
                        listener.locomotiveRemoved(turnout);
                    }

                }
            };

            final JSONObject removeLocomotiveJson = SIOLocomotiveMapper
                    .mapLocomotiveToJSON(turnout);

            sioService
                    .getSocket()
                    .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_REMOVE_REQUEST
                                    .getEvent(),
                            ioAcknowledge, removeLocomotiveJson
                    );
        } catch (final JSONException e) {
            throw new ManagerException("error removing turnout", e);
        }
    }

    @Override
    public void updateLocomotive(final Locomotive turnout) {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_UPDATE_REQUEST);
        sioService.checkSocket();
        try {
            final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

                @Override
                public void ack(final Object... arg0) {
                    LOGGER.info("ack: " + Arrays.toString(arg0));
                    final Boolean err = (Boolean) arg0[0];
                    final String msg = (String) arg0[1];
                    if (err) {
                        listener.failure(new ManagerException(msg));
                    } else {
                        listener.locomotiveUpdated(turnout);
                    }

                }
            };

            final JSONObject updateLocomotiveJson = SIOLocomotiveMapper
                    .mapLocomotiveToJSON(turnout);

            sioService
                    .getSocket()
                    .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_UPDATE_REQUEST
                                    .getEvent(),
                            ioAcknowledge, updateLocomotiveJson
                    );
        } catch (final JSONException e) {
            throw new ManagerException("error updating turnout", e);
        }

    }

    @Override
    public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_GET_ALL_REQUEST);
        sioService.checkSocket();
        final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

            @Override
            public void ack(final Object... arg0) {
                LOGGER.info("ack: " + Arrays.toString(arg0));

                try {
                    SIOLocomotiveServiceEventHandler.handleLocomotiveInit(
                            (JSONArray) arg0[0], listener);

                } catch (final JSONException e) {
                    throw new ManagerException(
                            "error getting all turnout groups", e);
                }
            }
        };
        sioService
                .getSocket()
                .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_GET_ALL_REQUEST
                        .getEvent(), ioAcknowledge, new Object[]{});
        return null;
    }

    @Override
    public void addLocomotiveGroup(final LocomotiveGroup group) {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_ADD_REQUEST);
        sioService.checkSocket();
        try {
            final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

                @Override
                public void ack(final Object... arg0) {
                    LOGGER.info("ack: " + Arrays.toString(arg0));
                    final Boolean err = (Boolean) arg0[0];
                    final String msg = (String) arg0[1];
                    final String sioId = (String) arg0[2];
                    if (err) {
                        listener.failure(new ManagerException(msg));
                    } else {
                        SIOLocomotiveServiceEventHandler
                                .addIdToLocomotiveGroup(group, sioId);
                        listener.locomotiveGroupAdded(group);
                    }

                }
            };

            final JSONObject addLocomotiveGroupJSON = SIOLocomotiveMapper
                    .mapLocomotiveGroupToJSON(group);

            sioService
                    .getSocket()
                    .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_ADD_REQUEST
                            .getEvent(), ioAcknowledge, addLocomotiveGroupJSON);
        } catch (final JSONException e) {
            throw new ManagerException("error adding turnout group",
                    e);
        }

    }

    @Override
    public void removeLocomotiveGroup(final LocomotiveGroup group) {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_REMOVE_REQUEST);
        sioService.checkSocket();
        try {
            final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

                @Override
                public void ack(final Object... arg0) {
                    LOGGER.info("ack: " + Arrays.toString(arg0));
                    final Boolean err = (Boolean) arg0[0];
                    final String msg = (String) arg0[1];
                    if (err) {
                        listener.failure(new ManagerException(msg));
                    } else {
                        listener.locomotiveGroupRemoved(group);
                    }

                }
            };

            final JSONObject removeLocomotiveGroupJSON = SIOLocomotiveMapper
                    .mapLocomotiveGroupToJSON(group);

            sioService
                    .getSocket()
                    .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_REMOVE_REQUEST
                                    .getEvent(), ioAcknowledge,
                            removeLocomotiveGroupJSON
                    );
        } catch (final JSONException e) {
            throw new ManagerException(
                    "error removing turnout group", e);
        }
    }

    @Override
    public void updateLocomotiveGroup(final LocomotiveGroup group) {
        LOGGER.info(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_UPDATE_REQUEST);
        sioService.checkSocket();
        try {
            final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

                @Override
                public void ack(final Object... arg0) {
                    LOGGER.info("ack: " + Arrays.toString(arg0));
                    final Boolean err = (Boolean) arg0[0];
                    final String msg = (String) arg0[1];
                    if (err) {
                        listener.failure(new ManagerException(msg));
                    } else {
                        listener.locomotiveGroupUpdated(group);
                    }

                }
            };

            final JSONObject updateLocomotiveGroupJSON = SIOLocomotiveMapper
                    .mapLocomotiveGroupToJSON(group);

            sioService
                    .getSocket()
                    .emit(SIOLocomotiveServiceEvent.LOCOMOTIVE_GROUP_UPDATE_REQUEST
                                    .getEvent(), ioAcknowledge,
                            updateLocomotiveGroupJSON
                    );
        } catch (final JSONException e) {
            throw new ManagerException(
                    "error updating turnout group", e);
        }
    }*/

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
                    "error parsing event '" + event + "'",e));
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
