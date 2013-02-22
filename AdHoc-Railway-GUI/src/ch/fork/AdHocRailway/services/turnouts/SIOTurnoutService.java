package ch.fork.AdHocRailway.services.turnouts;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;

public class SIOTurnoutService implements TurnoutService, IOCallback {

	private static final String TURNOUT_GROUP_ADD_REQUEST = "turnoutGroup:add";
	private static final String TURNOUT_GROUP_REMOVE_REQUEST = "turnoutGroup:remove";
	private static final String TURNOUT_GROUP_UPDATE_REQUEST = "turnoutGroup:update";

	private static final String TURNOUT_GROUP_GET_ALL_REQUEST = "turnoutGroup:getAll";

	private static final String TURNOUT_ADD_REQUEST = "turnout:add";

	private static final String TURNOUT_REMOVE_REQUEST = "turnout:remove";

	private static final String TURNOUT_UPDATE_REQUEST = "turnout:update";

	private static Logger LOGGER = Logger.getLogger(SIOTurnoutService.class);

	private static final SIOTurnoutService INSTANCE = new SIOTurnoutService();

	private static final String URL = "http://localhost:3000";

	private SocketIO socket;

	private TurnoutServiceListener listener;

	private SIOTurnoutService() {
	}

	public static SIOTurnoutService getInstance() {
		return INSTANCE;
	}

	@Override
	public void init(TurnoutServiceListener listener) {
		this.listener = listener;
		try {

			socket = new SocketIO(URL);
			socket.connect(this);
		} catch (MalformedURLException e) {
			throw new TurnoutManagerException(
					"failed to initialize socket.io on " + URL, e);
		}
	}

	@Override
	public void clear() {

	}

	@Override
	public void addTurnout(final Turnout turnout) {
		LOGGER.info(TURNOUT_ADD_REQUEST);
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutAdded(turnout);
					}
				}
			};

			JSONObject addTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			socket.emit(TURNOUT_ADD_REQUEST, ioAcknowledge, addTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error adding turnout", e);
		}

	}

	@Override
	public void deleteTurnout(final Turnout turnout) {
		LOGGER.info(TURNOUT_REMOVE_REQUEST);
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutRemoved(turnout);
					}

				}
			};

			JSONObject removeTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			socket.emit(TURNOUT_REMOVE_REQUEST, ioAcknowledge,
					removeTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error removing turnout", e);
		}
	}

	@Override
	public void updateTurnout(final Turnout turnout) {
		LOGGER.info(TURNOUT_UPDATE_REQUEST);
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutUpdated(turnout);
					}

				}
			};

			JSONObject updateTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			socket.emit(TURNOUT_UPDATE_REQUEST, ioAcknowledge,
					updateTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error updating turnout", e);
		}

	}

	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		LOGGER.info(TURNOUT_GROUP_GET_ALL_REQUEST);
		checkSocket();
		IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(Object... arg0) {
				LOGGER.info("ack: " + Arrays.toString(arg0));

				try {
					SIOTurnoutServiceEventHandler.handleTurnoutInit(
							(JSONObject) arg0[0], listener);

				} catch (JSONException e) {
					throw new TurnoutManagerException(
							"error getting all turnout groups", e);
				}
			}
		};
		socket.emit(TURNOUT_GROUP_GET_ALL_REQUEST, ioAcknowledge,
				new Object[] {});
		return null;
	}

	@Override
	public void addTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(TURNOUT_GROUP_ADD_REQUEST);
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutGroupAdded(group);
					}

				}
			};

			JSONObject addTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			socket.emit(TURNOUT_GROUP_ADD_REQUEST, ioAcknowledge,
					addTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error adding turnout group", e);
		}

	}

	@Override
	public void removeTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(TURNOUT_GROUP_REMOVE_REQUEST);
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutGroupRemoved(group);
					}

				}
			};

			JSONObject removeTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			socket.emit(TURNOUT_GROUP_REMOVE_REQUEST, ioAcknowledge,
					removeTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error removing turnout group", e);
		}
	}

	@Override
	public void updateTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(TURNOUT_GROUP_UPDATE_REQUEST);
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutGroupUpdated(group);
					}

				}
			};

			JSONObject updateTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			socket.emit(TURNOUT_GROUP_UPDATE_REQUEST, ioAcknowledge,
					updateTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error updating turnout group", e);
		}
	}

	@Override
	public void disconnect() {
		socket.disconnect();
	}

	@Override
	public synchronized void on(String event, IOAcknowledge arg1,
			Object... jsonData) {
		JSONObject data = (JSONObject) jsonData[0];
		LOGGER.info("on(message: " + event + ", args: " + data + ")");

		SIOTurnoutServiceEvent serviceEvent = SIOTurnoutServiceEvent
				.fromEvent(event);
		if (serviceEvent == null) {
			listener.failure(new TurnoutManagerException("unregonized event '"
					+ event + "' received"));
			return;
		}
		try {
			switch (serviceEvent) {
			case TURNOUT_INIT:
				SIOTurnoutServiceEventHandler.handleTurnoutInit(data, listener);
				break;
			case TURNOUT_ADDED:
				SIOTurnoutServiceEventHandler
						.handleTurnoutAdded(data, listener);
				break;
			case TURNOUT_GROUP_ADDED:
				SIOTurnoutServiceEventHandler.handleTurnoutGroupAdded(data,
						listener);
				break;
			case TURNOUT_GROUP_REMOVED:
				SIOTurnoutServiceEventHandler.handleTurnoutGroupRemoved(data,
						listener);
				break;
			case TURNOUT_GROUP_UPDATED:
				SIOTurnoutServiceEventHandler.handleTurnoutGroupUpdated(data,
						listener);
				break;
			case TURNOUT_REMOVED:
				SIOTurnoutServiceEventHandler.handleTurnoutRemoved(data,
						listener);
				break;
			case TURNOUT_UPDATED:
				SIOTurnoutServiceEventHandler.handleTurnoutUpdated(data,
						listener);
				break;
			default:
				listener.failure(new TurnoutManagerException(
						"unregonized event '" + event + "' received"));
				break;

			}
		} catch (JSONException e) {

			listener.failure(new TurnoutManagerException(
					"error parsing event '" + event + "'"));
		}
	}

	@Override
	public void onConnect() {
		LOGGER.info("successfully connected to socket.io at " + URL);
	}

	@Override
	public void onDisconnect() {
		LOGGER.info("successfully disconnected from socket.io at " + URL);

	}

	@Override
	public void onError(SocketIOException arg0) {
		LOGGER.error("failed to connect to socket.io at " + URL, arg0);
		listener.failure(new TurnoutManagerException(
				"failed to connect to socket.io at " + URL, arg0));
	}

	@Override
	public void onMessage(String arg0, IOAcknowledge arg1) {
		LOGGER.info("onMessage(" + arg0 + ")");
	}

	@Override
	public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
		LOGGER.info("onMessage(" + arg0 + ")");

	}

	private void checkSocket() {
		if (!socket.isConnected()) {
			throw new TurnoutManagerException(
					"not connected to socket.io server");
		}
	}
}
