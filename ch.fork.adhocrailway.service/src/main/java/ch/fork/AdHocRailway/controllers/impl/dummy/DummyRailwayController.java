package ch.fork.AdHocRailway.controllers.impl.dummy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fork on 01.03.15.
 */
public class DummyRailwayController {

    private static final DummyRailwayController INSTANCE = new DummyRailwayController();
    private List<DummyListener> dummyListeners = new ArrayList<DummyListener>();

    private DummyRailwayController() {

    }

    public static DummyRailwayController getInstance() {
        return INSTANCE;
    }

    public void addDummyListener(final DummyListener listener) {
        dummyListeners.add(listener);
    }

    public void removeDummyListener(final DummyListener listener) {
        dummyListeners.remove(listener);
    }

    void informDummyListeners(String message) {
        for (DummyListener dummyListener : dummyListeners) {
            dummyListener.sentDummyMessage(message);
        }
    }

    public void send(String command) {
        informDummyListeners(command);
    }
}
