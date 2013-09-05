package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import java.util.Arrays;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import ch.fork.AdHocRailway.services.turnouts.RouteServiceListener;

public class SIORouteService implements RouteService, IOCallback {

	private static final Logger LOGGER = Logger
			.getLogger(SIORouteService.class);

	private RouteServiceListener listener;
	private SIOService sioService;

	public SIORouteService() {
	}

	@Override
	public void init(final RouteServiceListener listener) {
		this.listener = listener;
		sioService = SIOService.getInstance();
		sioService.addIOCallback(this);
	}

	@Override
	public void clear() {
		LOGGER.info(SIORouteServiceEvent.ROUTE_CLEAR_REQUEST);
		sioService.checkSocket();
		final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(final Object... arg0) {
				LOGGER.info("ack: " + Arrays.toString(arg0));
				final Boolean err = (Boolean) arg0[0];
				final String msg = (String) arg0[1];
				if (err) {
					listener.failure(new RouteManagerException(msg));
				} else {
					final JSONObject data = (JSONObject) arg0[2];
					try {
						SIORouteServiceEventHandler.handleRouteInit(data,
								listener);
					} catch (final JSONException e) {
						listener.failure(new RouteManagerException(
								"error clearing rutnouts", e));
					}
				}
			}
		};

		sioService.getSocket().emit(
				SIORouteServiceEvent.ROUTE_CLEAR_REQUEST.getEvent(),
				ioAcknowledge, "");
	}

	@Override
	public void addRoute(final Route route) {
		LOGGER.info(SIORouteServiceEvent.ROUTE_ADD_REQUEST);

		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						final String sioId = (String) arg0[2];
						SIORouteServiceEventHandler.addIdToRoute(route, sioId);
						listener.routeAdded(route);
					}
				}
			};

			final JSONObject addRouteJson = SIORouteMapper
					.mapRouteToJSON(route);

			sioService.getSocket().emit(
					SIORouteServiceEvent.ROUTE_ADD_REQUEST.getEvent(),
					ioAcknowledge, addRouteJson);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error adding route", e);
		}

	}

	@Override
	public void removeRoute(final Route route) {
		LOGGER.info(SIORouteServiceEvent.ROUTE_REMOVE_REQUEST);

		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeRemoved(route);
					}

				}
			};

			final JSONObject removeRouteJson = SIORouteMapper
					.mapRouteToJSON(route);

			sioService.getSocket().emit(
					SIORouteServiceEvent.ROUTE_REMOVE_REQUEST.getEvent(),
					ioAcknowledge, removeRouteJson);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error removing route", e);
		}
	}

	@Override
	public void updateRoute(final Route route) {
		LOGGER.info(SIORouteServiceEvent.ROUTE_UPDATE_REQUEST);

		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeUpdated(route);
					}

				}
			};

			final JSONObject updateTurnoutJson = SIORouteMapper
					.mapRouteToJSON(route);

			sioService.getSocket().emit(
					SIORouteServiceEvent.ROUTE_UPDATE_REQUEST.getEvent(),
					ioAcknowledge, updateTurnoutJson);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error updating route", e);
		}

	}

	@Override
	public SortedSet<RouteGroup> getAllRouteGroups() {
		LOGGER.info(SIORouteServiceEvent.ROUTE_GROUP_GET_ALL_REQUEST);

		sioService.checkSocket();
		final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(final Object... arg0) {
				LOGGER.info("ack: " + Arrays.toString(arg0));

				try {
					SIORouteServiceEventHandler.handleRouteInit(
							(JSONObject) arg0[0], listener);

				} catch (final JSONException e) {
					throw new TurnoutManagerException(
							"error getting all turnout groups", e);
				}
			}
		};
		sioService.getSocket().emit(
				SIORouteServiceEvent.ROUTE_GROUP_GET_ALL_REQUEST.getEvent(),
				ioAcknowledge, new Object[] {});
		return null;
	}

	@Override
	public void addRouteGroup(final RouteGroup group) {
		LOGGER.info(SIORouteServiceEvent.ROUTE_GROUP_ADD_REQUEST);

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
						listener.failure(new RouteManagerException(msg));
					} else {
						SIORouteServiceEventHandler.addIdToRouteGroup(group,
								sioId);
						listener.routeGroupAdded(group);
					}

				}
			};

			final JSONObject addRouteGroupJSON = SIORouteMapper
					.mapRouteGroupToJSON(group);

			sioService.getSocket().emit(
					SIORouteServiceEvent.ROUTE_GROUP_ADD_REQUEST.getEvent(),
					ioAcknowledge, addRouteGroupJSON);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error adding route group", e);
		}

	}

	@Override
	public void removeRouteGroup(final RouteGroup group) {
		LOGGER.info(SIORouteServiceEvent.ROUTE_GROUP_REMOVE_REQUEST);
		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeGroupRemoved(group);
					}

				}
			};

			final JSONObject removeTurnoutGroupJSON = SIORouteMapper
					.mapRouteGroupToJSON(group);

			sioService.getSocket().emit(
					SIORouteServiceEvent.ROUTE_GROUP_REMOVE_REQUEST.getEvent(),
					ioAcknowledge, removeTurnoutGroupJSON);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error removing turnout group", e);
		}
	}

	@Override
	public void updateRouteGroup(final RouteGroup group) {
		LOGGER.info(SIORouteServiceEvent.ROUTE_GROUP_UPDATE_REQUEST);

		sioService.checkSocket();
		try {
			final IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					final Boolean err = (Boolean) arg0[0];
					final String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeGroupUpdated(group);
					}

				}
			};

			final JSONObject updateRouteGroupJSON = SIORouteMapper
					.mapRouteGroupToJSON(group);

			sioService.getSocket().emit(
					SIORouteServiceEvent.ROUTE_GROUP_UPDATE_REQUEST.getEvent(),
					ioAcknowledge, updateRouteGroupJSON);
		} catch (final JSONException e) {
			throw new TurnoutManagerException("error updating route group", e);
		}
	}

	@Override
	public void addRouteItem(final RouteItem item) throws RouteManagerException {

	}

	@Override
	public void removeRouteItem(final RouteItem item)
			throws RouteManagerException {

	}

	@Override
	public void updateRouteItem(final RouteItem item)
			throws RouteManagerException {

	}

	@Override
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
		final JSONObject data = (JSONObject) jsonData[0];
		LOGGER.info("on(message: " + event + ", args: " + data + ")");
		try {
			switch (serviceEvent) {
			case ROUTE_INIT:
				SIORouteServiceEventHandler.handleRouteInit(data, listener);
				break;
			case ROUTE_ADDED:
				SIORouteServiceEventHandler.handleRouteAdded(data, listener);
				break;
			case ROUTE_GROUP_ADDED:
				SIORouteServiceEventHandler.handleRouteGroupAdded(data,
						listener);
				break;
			case ROUTE_GROUP_REMOVED:
				SIORouteServiceEventHandler.handleRouteGroupRemoved(data,
						listener);
				break;
			case ROUTE_GROUP_UPDATED:
				SIORouteServiceEventHandler.handleRouteGroupUpdated(data,
						listener);
				break;
			case ROUTE_REMOVED:
				SIORouteServiceEventHandler.handleRouteRemoved(data, listener);
				break;
			case ROUTE_UPDATED:
				SIORouteServiceEventHandler.handleRouteUpdated(data, listener);
				break;
			default:
				break;
			}
		} catch (final JSONException e) {
			e.printStackTrace();
			listener.failure(new RouteManagerException("error parsing event '"
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
		listener.failure(new RouteManagerException(
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
