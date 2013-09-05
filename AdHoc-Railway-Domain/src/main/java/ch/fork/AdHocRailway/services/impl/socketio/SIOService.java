package ch.fork.AdHocRailway.services.impl.socketio;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;

public class SIOService {

	private static final Logger LOGGER = Logger.getLogger(SIOService.class);
	private static final SIOService INSTANCE = new SIOService();

	private SocketIO socket;

	private final Set<IOCallback> otherCallbacks = new HashSet<IOCallback>();

	private SIOService() {

	}

	public static SIOService getInstance() {

		return INSTANCE;
	}

	public void connect(final String url, final ServiceListener mainCallback) {
		try {
			if (socket == null) {
				socket = new SocketIO(url);
				socket.connect(new IOCallback() {

					@Override
					public void on(final String arg0, final IOAcknowledge arg1,
							final Object... arg2) {
						for (final IOCallback cb : otherCallbacks) {
							cb.on(arg0, arg1, arg2);
						}
					}

					@Override
					public void onConnect() {
						LOGGER.info("successfully connected to socket.io at "
								+ url);
						mainCallback.connected();
					}

					@Override
					public void onDisconnect() {
						LOGGER.info("successfully disconnected from socket.io at "
								+ url);
						mainCallback.disconnected();
					}

					@Override
					public void onError(final SocketIOException arg0) {
						LOGGER.error(
								"failed to connect to socket.io at " + url,
								arg0);
						mainCallback.connectionError(arg0);
						for (final IOCallback cb : otherCallbacks) {
							cb.onError(arg0);
						}
					}

					@Override
					public void onMessage(final String arg0,
							final IOAcknowledge arg1) {
						for (final IOCallback cb : otherCallbacks) {
							cb.onMessage(arg0, arg1);
						}

					}

					@Override
					public void onMessage(final JSONObject arg0,
							final IOAcknowledge arg1) {
						for (final IOCallback cb : otherCallbacks) {
							cb.onMessage(arg0, arg1);
						}
					}
				});
			}
		} catch (final MalformedURLException e) {
			throw new TurnoutManagerException(
					"failed to initialize socket.io on " + url, e);
		}
	}

	public void checkSocket() {
		if (!socket.isConnected()) {
			throw new TurnoutManagerException(
					"not connected to socket.io server");
		}
	}

	public void addIOCallback(final IOCallback callback) {
		otherCallbacks.add(callback);
	}

	public void removeIOCallback(final IOCallback callback) {
		otherCallbacks.remove(callback);
		if (otherCallbacks.isEmpty()) {
			disconnect();
		}
	}

	public SocketIO getSocket() {
		return socket;
	}

	public void disconnect() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		if (socket != null) {
			socket.disconnect();
			socket = null;
		}
	}

}
