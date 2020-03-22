package ch.fork.adhocrailway.persistence.adhocserver.impl.socketio;

import org.json.JSONObject;

/**
 * Created by fork on 06.04.15.
 */
public interface IOCallback {
    void on(String event, Object... jsonData);

    void onError(Exception arg0);

    void onMessage(String arg0);

    void onMessage(JSONObject arg0);
}
