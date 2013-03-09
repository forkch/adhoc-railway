package ch.fork.AdHocRailway.services.routes;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;

public class SIOService {

	private static Logger LOGGER = Logger.getLogger(SIOService.class);
	private static final String URL = "http://localhost:3000";
	private static final SIOService INSTANCE = new SIOService();

	private static SocketIO socket;

	private final Set<IOCallback> otherCallbacks = new HashSet<IOCallback>();

	private SIOService() {

	}

	public static SIOService getInstance() {

		return INSTANCE;
	}

	public void connect() {
		try {
			if (socket == null) {
				socket = new SocketIO(URL);
				socket.connect(new IOCallback() {

					@Override
					public void on(String arg0, IOAcknowledge arg1,
							Object... arg2) {
						for (IOCallback cb : otherCallbacks) {
							cb.on(arg0, arg1, arg2);
						}
					}

					@Override
					public void onConnect() {
						LOGGER.info("successfully connected to socket.io at "
								+ URL);
					}

					@Override
					public void onDisconnect() {
						LOGGER.info("successfully disconnected from socket.io at "
								+ URL);

					}

					@Override
					public void onError(SocketIOException arg0) {
						LOGGER.error(
								"failed to connect to socket.io at " + URL,
								arg0);
						for (IOCallback cb : otherCallbacks) {
							cb.onError(arg0);
						}
					}

					@Override
					public void onMessage(String arg0, IOAcknowledge arg1) {
						for (IOCallback cb : otherCallbacks) {
							cb.onMessage(arg0, arg1);
						}

					}

					@Override
					public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
						for (IOCallback cb : otherCallbacks) {
							cb.onMessage(arg0, arg1);
						}
					}
				});
			}
		} catch (MalformedURLException e) {
			throw new TurnoutManagerException(
					"failed to initialize socket.io on " + URL, e);
		}
	}

	public void checkSocket() {
		if (!socket.isConnected()) {
			throw new TurnoutManagerException(
					"not connected to socket.io server");
		}
	}

	public void addIOCallback(IOCallback callback) {
		otherCallbacks.add(callback);
	}

	public void removeIOCallback(IOCallback callback) {
		otherCallbacks.remove(callback);
	}

	public SocketIO getSocket() {
		return socket;
	}

	public void disconnect() {
		if (socket != null) {
			socket.disconnect();
			socket = null;
		}
	}

}
