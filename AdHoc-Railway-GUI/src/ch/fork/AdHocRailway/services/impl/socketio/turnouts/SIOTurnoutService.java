package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;

public class SIOTurnoutService implements TurnoutService, IOCallback {

	private static Logger LOGGER = Logger.getLogger(SIOTurnoutService.class);

	private static final SIOTurnoutService INSTANCE = new SIOTurnoutService();

	private TurnoutServiceListener listener;
	private SIOService sioService;

	private SIOTurnoutService() {
	}

	public static SIOTurnoutService getInstance() {
		return INSTANCE;
	}

	@Override
	public void init(TurnoutServiceListener listener) {
		this.listener = listener;

		sioService = SIOService.getInstance();
		sioService.addIOCallback(this);
		sioService.connect();
	}

	@Override
	public void clear() {

	}

	@Override
	public void addTurnout(final Turnout turnout) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_ADD_REQUEST);
		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					String sioId = (String) arg0[2];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						SIOTurnoutServiceEventHandler.addIdToTurnout(turnout,
								sioId);
						listener.turnoutAdded(turnout);
					}
				}
			};

			JSONObject addTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			sioService.getSocket().emit(
					SIOTurnoutServiceEvent.TURNOUT_ADD_REQUEST.getEvent(),
					ioAcknowledge, addTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error adding turnout", e);
		}

	}

	@Override
	public void deleteTurnout(final Turnout turnout) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_REMOVE_REQUEST);
		sioService.checkSocket();
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

			sioService.getSocket().emit(
					SIOTurnoutServiceEvent.TURNOUT_REMOVE_REQUEST.getEvent(),
					ioAcknowledge, removeTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error removing turnout", e);
		}
	}

	@Override
	public void updateTurnout(final Turnout turnout) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_UPDATE_REQUEST);
		sioService.checkSocket();
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

			sioService.getSocket().emit(
					SIOTurnoutServiceEvent.TURNOUT_UPDATE_REQUEST.getEvent(),
					ioAcknowledge, updateTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error updating turnout", e);
		}

	}

	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_GET_ALL_REQUEST);
		sioService.checkSocket();
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
		sioService
				.getSocket()
				.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_GET_ALL_REQUEST
						.getEvent(),
						ioAcknowledge, new Object[] {});
		return null;
	}

	@Override
	public void addTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_ADD_REQUEST);
		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					String sioId = (String) arg0[2];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						SIOTurnoutServiceEventHandler.addIdToTurnoutGroup(
								group, sioId);
						listener.turnoutGroupAdded(group);
					}

				}
			};

			JSONObject addTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			sioService
					.getSocket()
					.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_ADD_REQUEST
							.getEvent(),
							ioAcknowledge, addTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error adding turnout group", e);
		}

	}

	@Override
	public void removeTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_REMOVE_REQUEST);
		sioService.checkSocket();
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

			sioService
					.getSocket()
					.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_REMOVE_REQUEST
							.getEvent(),
							ioAcknowledge, removeTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error removing turnout group", e);
		}
	}

	@Override
	public void updateTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_UPDATE_REQUEST);
		sioService.checkSocket();
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

			sioService
					.getSocket()
					.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_UPDATE_REQUEST
							.getEvent(),
							ioAcknowledge, updateTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error updating turnout group", e);
		}
	}

	@Override
	public void disconnect() {

		sioService.disconnect();
	}

	@Override
	public synchronized void on(String event, IOAcknowledge arg1,
			Object... jsonData) {

		SIOTurnoutServiceEvent serviceEvent = SIOTurnoutServiceEvent
				.fromEvent(event);
		if (serviceEvent == null) {
			return;
		}
		JSONObject data = (JSONObject) jsonData[0];
		LOGGER.info("on(message: " + event + ", args: " + data + ")");
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
	}

	@Override
	public void onDisconnect() {

	}

	@Override
	public void onError(SocketIOException arg0) {
	}

	@Override
	public void onMessage(String arg0, IOAcknowledge arg1) {
		LOGGER.info("onMessage(" + arg0 + ")");
	}

	@Override
	public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
		LOGGER.info("onMessage(" + arg0 + ")");

	}

}
