package ch.fork.AdHocRailway.services.routes;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;

public class SIORouteService implements RouteService, IOCallback {

	private static final String ROUTE_GROUP_ADD_REQUEST = "routeGroup:add";
	private static final String ROUTE_GROUP_REMOVE_REQUEST = "routeGroup:remove";
	private static final String ROUTE_GROUP_UPDATE_REQUEST = "routeGroup:update";

	private static final String ROUTE_GROUP_GET_ALL_REQUEST = "routeGroup:getAll";

	private static final String ROUTE_ADD_REQUEST = "route:add";

	private static final String ROUTE_REMOVE_REQUEST = "route:remove";

	private static final String ROUTE_UPDATE_REQUEST = "route:update";

	private static Logger LOGGER = Logger.getLogger(SIORouteService.class);

	private static final SIORouteService INSTANCE = new SIORouteService();

	private RouteServiceListener listener;
	private SIOService sioService;

	private SIORouteService() {
	}

	public static SIORouteService getInstance() {
		return INSTANCE;
	}

	@Override
	public void init(RouteServiceListener listener) {
		this.listener = listener;
		sioService = SIOService.getInstance();
		sioService.addIOCallback(this);
		sioService.connect();
	}

	@Override
	public void clear() {

	}

	@Override
	public void addRoute(final Route route) {
		LOGGER.info(ROUTE_ADD_REQUEST);

		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeAdded(route);
					}
				}
			};

			JSONObject addRouteJson = SIORouteMapper.mapRouteToJSON(route);

			sioService.getSocket().emit(ROUTE_ADD_REQUEST, ioAcknowledge,
					addRouteJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error adding route", e);
		}

	}

	@Override
	public void removeRoute(final Route route) {
		LOGGER.info(ROUTE_REMOVE_REQUEST);

		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeRemoved(route);
					}

				}
			};

			JSONObject removeRouteJson = SIORouteMapper.mapRouteToJSON(route);

			sioService.getSocket().emit(ROUTE_REMOVE_REQUEST, ioAcknowledge,
					removeRouteJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error removing route", e);
		}
	}

	@Override
	public void updateRoute(final Route route) {
		LOGGER.info(ROUTE_UPDATE_REQUEST);

		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeUpdated(route);
					}

				}
			};

			JSONObject updateTurnoutJson = SIORouteMapper.mapRouteToJSON(route);

			sioService.getSocket().emit(ROUTE_UPDATE_REQUEST, ioAcknowledge,
					updateTurnoutJson);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error updating route", e);
		}

	}

	@Override
	public List<RouteGroup> getAllRouteGroups() {
		LOGGER.info(ROUTE_GROUP_GET_ALL_REQUEST);

		sioService.checkSocket();
		IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(Object... arg0) {
				LOGGER.info("ack: " + Arrays.toString(arg0));

				try {
					SIORouteServiceEventHandler.handleRouteInit(
							(JSONObject) arg0[0], listener);

				} catch (JSONException e) {
					throw new TurnoutManagerException(
							"error getting all turnout groups", e);
				}
			}
		};
		sioService.getSocket().emit(ROUTE_GROUP_GET_ALL_REQUEST, ioAcknowledge,
				new Object[] {});
		return null;
	}

	@Override
	public void addRouteGroup(final RouteGroup group) {
		LOGGER.info(ROUTE_GROUP_ADD_REQUEST);

		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeGroupAdded(group);
					}

				}
			};

			JSONObject addRouteGroupJSON = SIORouteMapper
					.mapRouteGroupToJSON(group);

			sioService.getSocket().emit(ROUTE_GROUP_ADD_REQUEST, ioAcknowledge,
					addRouteGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error adding route group", e);
		}

	}

	@Override
	public void removeRouteGroup(final RouteGroup group) {
		LOGGER.info(ROUTE_GROUP_REMOVE_REQUEST);
		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeGroupRemoved(group);
					}

				}
			};

			JSONObject removeTurnoutGroupJSON = SIORouteMapper
					.mapRouteGroupToJSON(group);

			sioService.getSocket().emit(ROUTE_GROUP_REMOVE_REQUEST,
					ioAcknowledge, removeTurnoutGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error removing turnout group", e);
		}
	}

	@Override
	public void updateRouteGroup(final RouteGroup group) {
		LOGGER.info(ROUTE_GROUP_UPDATE_REQUEST);

		sioService.checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(final Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					Boolean err = (Boolean) arg0[0];
					String msg = (String) arg0[1];
					if (err) {
						listener.failure(new RouteManagerException(msg));
					} else {
						listener.routeGroupUpdated(group);
					}

				}
			};

			JSONObject updateRouteGroupJSON = SIORouteMapper
					.mapRouteGroupToJSON(group);

			sioService.getSocket().emit(ROUTE_GROUP_UPDATE_REQUEST,
					ioAcknowledge, updateRouteGroupJSON);
		} catch (JSONException e) {
			throw new TurnoutManagerException("error updating route group", e);
		}
	}

	@Override
	public void addRouteItem(RouteItem item) throws RouteManagerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRouteItem(RouteItem item) throws RouteManagerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateRouteItem(RouteItem item) throws RouteManagerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		sioService.disconnect();
	}

	@Override
	public synchronized void on(String event, IOAcknowledge arg1,
			Object... jsonData) {
		SIORouteServiceEvent serviceEvent = SIORouteServiceEvent
				.fromEvent(event);

		if (serviceEvent == null) {
			return;
		}
		JSONObject data = (JSONObject) jsonData[0];
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
		} catch (JSONException e) {
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
