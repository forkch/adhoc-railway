package ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio;

import ch.fork.AdHocRailway.services.AdHocServiceException;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

public class SIOService {

    private static final Logger LOGGER = Logger.getLogger(SIOService.class);
    private static final SIOService INSTANCE = new SIOService();
    private static String uuid;
    private final Set<IOCallback> otherCallbacks = new HashSet<IOCallback>();
    private SocketIO socket;

    private SIOService() {

    }

    public static SIOService getInstance() {
        return INSTANCE;
    }

    public static void setUUID(String uuid) {
        SIOService.uuid = uuid;
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
                        LOGGER.info("successfully connected to AdHoc-Server at "
                                + url);
                        socket.emit("register", uuid);
                        mainCallback.connected();
                    }

                    @Override
                    public void onDisconnect() {
                        LOGGER.info("successfully disconnected from AdHoc-Server at "
                                + url);
                        mainCallback.disconnected();
                    }

                    @Override
                    public void onError(final SocketIOException arg0) {
                        LOGGER.error("failed to connect to AdHoc-Server at "
                                + url, arg0);
                        mainCallback.connectionError(new AdHocServiceException(
                                "failed to connect to AdHoc-Server at " + url,
                                arg0));
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
            throw new AdHocServiceException(
                    "failed to initialize socket.io on " + url, e);
        }
    }

    public void checkSocket() {
        if (!socket.isConnected()) {
            throw new AdHocServiceException(
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
