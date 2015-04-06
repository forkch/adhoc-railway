package ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio;

import ch.fork.AdHocRailway.services.AdHocServiceException;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.apache.log4j.Logger;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class SIOService {

    private static final Logger LOGGER = Logger.getLogger(SIOService.class);
    private static String uuid;
    private final Set<IOCallback> otherCallbacks = new HashSet<>();
    private Socket socket;

    public SIOService(String uuid) {
        this.uuid = uuid;
    }

    public void connect(final String url, final ServiceListener mainCallback) {
        try {
            socket = IO.socket(url);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            LOGGER.info("successfully connected to AdHoc-Server at "
                                    + url);
                            socket.emit("register", uuid);
                            mainCallback.connected();
                        }
                    }
            ).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            LOGGER.info("successfully disconnected from AdHoc-Server at "
                                    + url);
                            mainCallback.disconnected();
                            socket.off();
                        }
                    }
            ).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LOGGER.error("failed to connect to AdHoc-Server at "
                            + url);
                    mainCallback.connectionError(new AdHocServiceException(
                            "failed to connect to AdHoc-Server at " + url));
                    for (final IOCallback cb : otherCallbacks) {
                        cb.onError((Exception) args[0]);
                    }
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            throw new AdHocServiceException(
                    "failed to initialize socket.io on " + url, e);
        }
    }

    public void addIOCallback(final String event, final IOCallback callback) {
        otherCallbacks.add(callback);
        socket.on(event, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                callback.on(event, args);
            }
        });
    }

    public void removeIOCallback(final IOCallback callback) {
        otherCallbacks.remove(callback);
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
        }
    }
}
