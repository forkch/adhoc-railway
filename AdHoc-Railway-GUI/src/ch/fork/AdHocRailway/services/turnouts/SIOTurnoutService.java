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
		// TODO Auto-generated method stub

	}

	@Override
	public void addTurnout(final Turnout turnout) {
		LOGGER.info("addTurnout()");
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					listener.turnoutAdded(turnout);

				}
			};

			JSONObject addTurnoutJson = SIOTurnoutMapper
					.mapTurnoutToJSON(turnout);

			socket.emit("turnout:add", ioAcknowledge, addTurnoutJson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void deleteTurnout(Turnout turnout) {
		checkSocket();

	}

	@Override
	public void updateTurnout(Turnout turnout) {
		checkSocket();

	}

	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		checkSocket();
		IOAcknowledge ioAcknowledge = new IOAcknowledge() {

			@Override
			public void ack(Object... arg0) {

				try {
					SIOTurnoutServiceEventHandler.handleTurnoutInit(
							(JSONObject) arg0[0], listener);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		socket.emit("turnoutGroup:getAll", ioAcknowledge, new Object[] {});
		return null;
	}

	@Override
	public void addTurnoutGroup(final TurnoutGroup group) {
		LOGGER.info("addTurnoutGroup()");
		checkSocket();
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					listener.turnoutGroupAdded(group);

				}
			};

			JSONObject addTurnoutGroupJSON = SIOTurnoutMapper
					.mapTurnoutGroupToJSON(group);

			socket.emit("turnoutGroup:add", ioAcknowledge, addTurnoutGroupJSON);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void deleteTurnoutGroup(TurnoutGroup group) {
		checkSocket();

	}

	@Override
	public void updateTurnoutGroup(TurnoutGroup group) {
		checkSocket();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onConnect() {
		LOGGER.info("successfully connected to socket.io at " + URL);
		listener.ready();
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
