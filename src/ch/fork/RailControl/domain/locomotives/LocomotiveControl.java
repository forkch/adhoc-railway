package ch.fork.RailControl.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.RailControl.domain.Constants;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GLInfoListener;

public class LocomotiveControl implements GLInfoListener {

    private static LocomotiveControl instance;

    private List<LocomotiveChangeListener> listeners;

    private Map<Integer, Locomotive> locomotives;

    private SRCPSession session;

    private LocomotiveControl() {
        listeners = new ArrayList<LocomotiveChangeListener>();
        locomotives = new HashMap<Integer, Locomotive>();
    }

    public static LocomotiveControl getInstance() {
        if (instance == null) {
            instance = new LocomotiveControl();
            return instance;
        } else {
            return instance;
        }
    }

    public void registerLocomotives(List<Locomotive> locomotivesToRegister) {
        
        for (Locomotive l : locomotivesToRegister) {
            locomotives.put(l.getAddress(), l);
        }
    }

    public void unregisterAllLocomotives() throws LocomotiveException {
        for (Locomotive l : locomotives.values()) {
            l.term();
        }
        locomotives.clear();
    }

    public void setSessionOnLocomotives(SRCPSession session) {
        for (Locomotive l : locomotives.values()) {
            l.setSession(session);
        }
    }

    public void addLocomotiveChangeListener(LocomotiveChangeListener l) {
        listeners.add(l);
    }

    public void toggleDirection(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.toggleDirection();
    }

    public void setSpeed(Locomotive locomotive, int speed)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.setSpeed(speed);
    }

    public void increaseSpeed(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeed();
    }

    public void decreaseSpeed(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeed();
    }

    public void increaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeedStep();
    }

    public void decreaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeedStep();
    }

    public void setFunctions(Locomotive locomotive, boolean[] functions)
        throws LocomotiveException {
        checkLocomotiveSession(locomotive);
        initLocomotive(locomotive);
        locomotive.setFunctions(functions);
    }

    public void GLset(double timestamp, int bus, int address,
        String drivemode, int v, int vMax, boolean[] functions) {

        Locomotive locomotive = locomotives.get(address);
        locomotive.locomotiveChanged(drivemode, v, vMax, functions);
        informListeners(locomotive);
    }

    public void GLinit(double timestamp, int bus, int address,
        String protocol, String[] params) {

        Locomotive locomotive = locomotives.get(address);
        if (locomotive != null) {
            locomotive.locomotiveInitialized(
                bus, address, protocol, params);
            informListeners(locomotive);
        }
    }

    public void GLterm(double timestamp, int bus, int address) {

        Locomotive locomotive = locomotives.get(address);
        if (locomotive != null) {
            locomotive.locomotiveTerminated();
            informListeners(locomotive);
        }
    }

    private void informListeners(Locomotive changedLocomotive) {
        for (LocomotiveChangeListener l : listeners) {
            l.locomotiveChanged(changedLocomotive);
        }
    }

    public SRCPSession getSession() {
        return session;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
        session.getInfoChannel().addGLInfoListener(this);
    }

    private void checkLocomotiveSession(Locomotive locomotive)
        throws LocomotiveException {
        if (locomotive instanceof NoneLocomotive) {
            return;
        }
        if (locomotive.getSession() == null) {
            throw new LocomotiveException(Constants.ERR_NO_SESSION);
        }
    }

    private void initLocomotive(Locomotive locomotive)
        throws LocomotiveException {
        if (!locomotive.isInitialized()) {
            locomotive.init();
        }
    }

}
