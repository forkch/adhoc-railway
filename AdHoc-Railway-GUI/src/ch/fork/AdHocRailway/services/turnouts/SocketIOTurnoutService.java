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

public class SocketIOTurnoutService implements TurnoutService {

	private static Logger LOGGER = Logger
			.getLogger(SocketIOTurnoutService.class);

	private static final SocketIOTurnoutService INSTANCE = new SocketIOTurnoutService();

	private static final String URL = "http://localhost:3000";

	private SocketIO socket;

	private TurnoutServiceListener listener;

	private SocketIOTurnoutService() {
	}

	public static SocketIOTurnoutService getInstance() {
		return INSTANCE;
	}

	@Override
	public void init(TurnoutServiceListener listener) {
		this.listener = listener;
		try {

			socket = new SocketIO(URL);
			socket.connect(new IOCallback() {

				@Override
				public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
				}

				@Override
				public void onMessage(String arg0, IOAcknowledge arg1) {
				}

				@Override
				public void onError(SocketIOException arg0) {
					throw new TurnoutManagerException(
							"failed to initialize socket.io on " + URL);
				}

				@Override
				public void onDisconnect() {
				}

				@Override
				public void onConnect() {
					LOGGER.info("successfully connected to " + URL);
				}

				@Override
				public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
				}
			});
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
		// @formatter:off
		try {
			IOAcknowledge ioAcknowledge = new IOAcknowledge() {

				@Override
				public void ack(Object... arg0) {
					LOGGER.info("ack: " + Arrays.toString(arg0));
					listener.turnoutAdded(turnout);

				}
			};
			JSONObject addTurnoutJson = new JSONObject();
			addTurnoutJson.put("number", turnout.getNumber());
			addTurnoutJson.put("bus1", turnout.getBus1());
			addTurnoutJson.put("address1", turnout.getAddress1());
			addTurnoutJson.put("bus2", turnout.getBus2());
			addTurnoutJson.put("address2", turnout.getAddress2());
			addTurnoutJson.put("type", turnout.getTurnoutType().toString());

			socket.emit("turnout:add", ioAcknowledge, addTurnoutJson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// @formatter:on

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
			}
		};
		socket.emit("turnouts:getAll", ioAcknowledge, new Object[] {});
		return null;
	}

	@Override
	public void addTurnoutGroup(TurnoutGroup group) {
		checkSocket();

	}

	@Override
	public void deleteTurnoutGroup(TurnoutGroup group) {
		checkSocket();

	}

	@Override
	public void updateTurnoutGroup(TurnoutGroup group) {
		checkSocket();
	}

	private void checkSocket() {
		if (!socket.isConnected()) {
			throw new TurnoutManagerException(
					"not connected to socket.io server");
		}
	}

	@Override
	public void disconnect() {
		socket.disconnect();
	}
}
