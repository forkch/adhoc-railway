package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import java.util.Arrays;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;

public class SIOTurnoutService implements TurnoutService, IOCallback {

	private static final Logger LOGGER = Logger
			.getLogger(SIOTurnoutService.class);

	private TurnoutServiceListener listener;
	private SIOService sioService;

	public SIOTurnoutService() {
	}

	@Override
	public void init(final TurnoutServiceListener listener) {
		this.listener = listener;

		sioService = SIOService.getInstance();
		sioService.addIOCallback(this);
	}

	@Override
	public void clear() {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_CLEAR_REQUEST);
		sioService.checkSocket();
		final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(final Object... arg0) {
				LOGGER.info("ack: " + Arrays.toString(arg0));
				final Boolean err = (Boolean) arg0[0];
				final String msg = (String) arg0[1];
				if (err) {
					listener.failure(new TurnoutManagerException(msg));
				} else {
					final JSONObject data = (JSONObject) arg0[2];
					try {
						SIOTurnoutServiceEventHandler.handleTurnoutInit(data,
								listener);
					} catch (final JSONException e) {
						listener.failure(new TurnoutManagerException(
								"error clearing rutnouts", e));
					}
				}
			}
		};

		sioService.getSocket().emit(
				SIOTurnoutServiceEvent.TURNOUT_CLEAR_REQUEST.getEvent(),
				ioAcknowledge, "");
	}

	@Override
	public void addTurnout(final Turnout turnout) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_ADD_REQUEST);
		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						final String sioId = (String) arg0[2];
						SIOTurnoutServiceEventHandler.addIdToTurnout(turnout,
								sioId);
						listener.turnoutAdded(turnout);
					}
				}
			};

			final JSONObject addTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			sioService.getSocket().emit(
					SIOTurnoutServiceEvent.TURNOUT_ADD_REQUEST.getEvent(),
					ioAcknowledge, addTurnoutJson);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error adding turnout", e);
		}

	}

	@Override
	public void removeTurnout(final Turnout turnout) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_REMOVE_REQUEST);
		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutRemoved(turnout);
					}

				}
			};

			final JSONObject removeTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			sioService.getSocket().emit(
					SIOTurnoutServiceEvent.TURNOUT_REMOVE_REQUEST.getEvent(),
					ioAcknowledge, removeTurnoutJson);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error removing turnout", e);
		}
	}

	@Override
	public void updateTurnout(final Turnout turnout) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_UPDATE_REQUEST);
		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutUpdated(turnout);
					}

				}
			};

			final JSONObject updateTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			sioService.getSocket().emit(
					SIOTurnoutServiceEvent.TURNOUT_UPDATE_REQUEST.getEvent(),
					ioAcknowledge, updateTurnoutJson);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error updating turnout", e);
		}

	}

	@Override
	public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_GET_ALL_REQUEST);
		sioService.checkSocket();
		final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(final Object... arg0) {
				LOGGER.info("ack: " + Arrays.toString(arg0));

				try {
					SIOTurnoutServiceEventHandler.handleTurnoutInit(
							(JSONObject) arg0[0], listener);

				} catch (final JSONException e) {
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
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					final String sioId = (String) arg0[2];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						SIOTurnoutServiceEventHandler.addIdToTurnoutGroup(
								group, sioId);
						listener.turnoutGroupAdded(group);
					}

				}
			};

			final JSONObject addTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			sioService
					.getSocket()
					.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_ADD_REQUEST
							.getEvent(),
							ioAcknowledge, addTurnoutGroupJSON);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error adding turnout group", e);
		}

	}

	@Override
	public void removeTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_REMOVE_REQUEST);
		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutGroupRemoved(group);
					}

				}
			};

			final JSONObject removeTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			sioService
					.getSocket()
					.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_REMOVE_REQUEST
							.getEvent(),
							ioAcknowledge, removeTurnoutGroupJSON);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error removing turnout group", e);
		}
	}

	@Override
	public void updateTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info(SIOTurnoutServiceEvent.TURNOUT_GROUP_UPDATE_REQUEST);
		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new TurnoutManagerException(msg));
					} else {
						listener.turnoutGroupUpdated(group);
					}

				}
			};

			final JSONObject updateTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			sioService
					.getSocket()
					.emit(SIOTurnoutServiceEvent.TURNOUT_GROUP_UPDATE_REQUEST
							.getEvent(),
							ioAcknowledge, updateTurnoutGroupJSON);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error updating turnout group", e);
		}
	}

	@Override
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
		final JSONObject data = (JSONObject) jsonData[0];
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
		} catch (final JSONException e) {
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
	public void onError(final SocketIOException arg0) {
		listener.failure(new TurnoutManagerException(
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
