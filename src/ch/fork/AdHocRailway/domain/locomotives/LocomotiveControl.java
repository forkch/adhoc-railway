
package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GLInfoListener;

public class LocomotiveControl implements GLInfoListener {
    private static LocomotiveControl       instance;
    private List<LocomotiveChangeListener> listeners;
    private Map<Address, Locomotive> addressToLocomotives;
    private List<LocomotiveGroup>          locomotiveGroups;
    private SRCPSession                    session;

    private LocomotiveControl() {
        listeners = new ArrayList<LocomotiveChangeListener>();
        addressToLocomotives = new HashMap<Address, Locomotive>();
        locomotiveGroups = new ArrayList<LocomotiveGroup>();
    }

    public static LocomotiveControl getInstance() {
        if (instance == null) {
            instance = new LocomotiveControl();
        }
        return instance;
    }

    public void registerLocomotive(Locomotive locomotiveToRegister) {
        addressToLocomotives
            .put(locomotiveToRegister.getAddress(), locomotiveToRegister);
        locomotiveToRegister.setSession(session);
    }

    public void registerLocomotives(Collection<Locomotive> locomotivesToRegister) {
        for (Locomotive l : locomotivesToRegister) {
            addressToLocomotives.put(l.getAddress(), l);
            l.setSession(session);
        }
    }

    public void unregisterAllLocomotives() throws LocomotiveException {
        for (Locomotive l : addressToLocomotives.values()) {
            l.term();
        }
        addressToLocomotives.clear();
    }

    public SortedSet<Locomotive> getLocomotives() {
        return new TreeSet<Locomotive>(addressToLocomotives.values());
    }

    public void registerLocomotiveGroup(LocomotiveGroup locomotiveGroup) {
        locomotiveGroups.add(locomotiveGroup);
    }

    public void registerLocomotiveGroups(
        Collection<LocomotiveGroup> locomotiveGroupsToRegister) {
        locomotiveGroups.addAll(locomotiveGroupsToRegister);
    }

    public void unregisterAllLocomotiveGroups() {
        locomotiveGroups.clear();
    }

    public List<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
        for (Locomotive l : addressToLocomotives.values()) {
            l.setSession(session);
        }
        session.getInfoChannel().addGLInfoListener(this);
    }

    public void toggleDirection(Locomotive locomotive)
        throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.toggleDirection();
    }

    public void setSpeed(Locomotive locomotive, int speed)
        throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.setSpeed(speed);
    }

    public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeed();
    }

    public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeed();
    }

    public void increaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeedStep();
    }

    public void decreaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeedStep();
    }

    public void setFunctions(Locomotive locomotive, boolean[] functions)
        throws LocomotiveException {
        checkSwitch(locomotive);
        initLocomotive(locomotive);
        locomotive.setFunctions(functions);
    }

    public void GLinit(double timestamp, int bus, int address, String protocol,
        String[] params) {
        Address addr = new Address(bus, address);
        Locomotive locomotive = addressToLocomotives.get(addr);
        if (locomotive != null) {
            locomotive.locomotiveInitialized(addr, protocol, params);
            informListeners(locomotive);
        }
    }

    public void GLset(double timestamp, int bus, int address, String drivemode,
        int v, int vMax, boolean[] functions) {
        // FIXME: removed to get a smoother LocomotiveWidget
        // Address addr = new Address(bus, address);
        // Locomotive locomotive = locomotives.get(addr);
        // locomotive.locomotiveChanged(drivemode, v, vMax, functions);
        // informListeners(locomotive);
    }

    public void GLterm(double timestamp, int bus, int address) {
        Address addr = new Address(bus, address);
        Locomotive locomotive = addressToLocomotives.get(addr);
        if (locomotive != null) {
            locomotive.locomotiveTerminated();
            informListeners(locomotive);
        }
    }

    public void addLocomotiveChangeListener(LocomotiveChangeListener l) {
        listeners.add(l);
    }

    public void removeLocomotiveChangeListener(LocomotiveChangeListener l) {
        listeners.remove(l);
    }

    private void informListeners(Locomotive changedLocomotive) {
        for (LocomotiveChangeListener l : listeners) {
            l.locomotiveChanged(changedLocomotive);
        }
    }

    private void checkSwitch(Locomotive locomotive)
        throws LocomotiveException {
        if (locomotive instanceof NoneLocomotive) {
            return;
        }
        if (locomotive.getSession() == null) {
            throw new LocomotiveException(Constants.ERR_NO_SESSION);
        }
        if(locomotive.getAddress().getAddress() == 0) {
            throw new LocomotiveException(Constants.ERR_INVALID_ADDRESS);
        }
    }

    private void initLocomotive(Locomotive locomotive)
        throws LocomotiveException {
        if (!locomotive.isInitialized()) {
            locomotive.init();
        }
    }
}
