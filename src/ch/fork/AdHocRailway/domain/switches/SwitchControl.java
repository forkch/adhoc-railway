
package ch.fork.AdHocRailway.domain.switches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GAInfoListener;

public class SwitchControl implements GAInfoListener {
    private static SwitchControl       instance;
    private SRCPSession                session;
    private List<SwitchChangeListener> listeners;
    private List<SwitchGroup>          switchGroups;
    private Map<Integer, Switch>       addressToSwitch;
    private Map<Integer, Switch>       numberToSwitch;

    private SwitchControl() {
        listeners = new ArrayList<SwitchChangeListener>();
        addressToSwitch = new HashMap<Integer, Switch>();
        numberToSwitch = new HashMap<Integer, Switch>();
        switchGroups = new ArrayList<SwitchGroup>();
    }

    public static SwitchControl getInstance() {
        if (instance == null) {
            instance = new SwitchControl();
        }
        return instance;
    }

    public void registerSwitch(Switch aSwitch) {
        Address address = aSwitch.getAddress();
        addressToSwitch.put(address.getAddress1(), aSwitch);
        if (address.getAddress2() != 0) {
            addressToSwitch.put(address.getAddress2(), aSwitch);
        }
        numberToSwitch.put(aSwitch.getNumber(), aSwitch);
        aSwitch.setSession(session);
    }

    public void registerSwitches(Collection<Switch> switches) {
        for (Switch aSwitch : switches) {
            Address address = aSwitch.getAddress();
            addressToSwitch.put(address.getAddress1(), aSwitch);
            if (address.getAddress2() != 0) {
                addressToSwitch.put(address.getAddress2(), aSwitch);
            }
            numberToSwitch.put(aSwitch.getNumber(), aSwitch);
            aSwitch.setSession(session);
        }
    }

    public void unregisterSwitch(Switch aSwitch) {
        Address address = aSwitch.getAddress();
        addressToSwitch.remove(address);
        numberToSwitch.remove(aSwitch.getNumber());
    }

    public void unregisterAllSwitches() {
        for (Switch aSwitch : addressToSwitch.values()) {
            // aSwitch.term();
        }
        addressToSwitch.clear();
        numberToSwitch.clear();
    }

    public Map<Integer, Switch> getNumberToSwitch() {
        return numberToSwitch;
    }

    public void registerSwitchGroup(SwitchGroup sg) {
        switchGroups.add(sg);
    }

    public void registerSwitchGroups(Collection<SwitchGroup> sgs) {
        switchGroups.addAll(sgs);
    }

    public void unregisterAllSwitchGroups() {
        switchGroups.clear();
    }

    public List<SwitchGroup> getSwitchGroups() {
        return switchGroups;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
        for (Switch aSwitch : addressToSwitch.values()) {
            aSwitch.setSession(session);
        }
        session.getInfoChannel().addGAInfoListener(this);
    }

    public void toggle(Switch aSwitch) throws SwitchException {
        checkSwitch(aSwitch);
        initSwitch(aSwitch);
        aSwitch.toggle();
        for (SwitchChangeListener l : listeners) {
            l.switchChanged(aSwitch);
        }
    }

    public void setStraight(Switch aSwitch) throws SwitchException {
        checkSwitch(aSwitch);
        initSwitch(aSwitch);
        aSwitch.setStraight();
    }

    public void setCurvedRight(Switch aSwitch) throws SwitchException {
        checkSwitch(aSwitch);
        initSwitch(aSwitch);
        aSwitch.setCurvedRight();
    }

    public void setCurvedLeft(Switch aSwitch) throws SwitchException {
        checkSwitch(aSwitch);
        initSwitch(aSwitch);
        aSwitch.setCurvedLeft();
    }

    public void GAset(double timestamp, int bus, int address, int port,
        int value) {
        /*
         * System.out.println("GAset(" + bus + " , " + address + " , " + port + " , " +
         * value + " )");
         */
        Switch s = addressToSwitch.get(address);
        if (s != null) {
            s.switchPortChanged(address, port, value);
            if (value != 0) {
                informListeners(s);
            }
        }
    }

    public void GAinit(double timestamp, int bus, int address, String protocol,
        String[] params) {
        /*
         * System.out.println("GAinit(" + bus + " , " + address + " , " +
         * protocol + " , " + params + " )");
         */
        Switch s = addressToSwitch.get(Integer.valueOf(address));
        if (s != null) {
            s.switchInitialized(bus, address);
            informListeners(s);
        }
    }

    public void GAterm(double timestamp, int bus, int address) {
        /*
         * System.out.println("GAterm( " + bus + " , " + address + " )");
         */
        Switch s = addressToSwitch.get(address);
        s.switchTerminated(address);
        informListeners(s);
    }

    public void addSwitchChangeListener(SwitchChangeListener listener) {
        listeners.add(listener);
    }

    public void removeSwitchChangeListener(SwitchChangeListener listener) {
        listeners.remove(listener);
    }

    private void informListeners(Switch changedSwitch) {
        for (SwitchChangeListener l : listeners) {
            l.switchChanged(changedSwitch);
        }
    }

    private void checkSwitch(Switch aSwitch) throws SwitchException {
        if (aSwitch.getSession() == null) {
            throw new SwitchException(Constants.ERR_NO_SESSION);
        }
        if (aSwitch.getAddress().getAddress1() == 0) {
            throw new SwitchException(Constants.ERR_INVALID_ADDRESS);
        }
        if (aSwitch instanceof ThreeWaySwitch
            && aSwitch.getAddress().getAddress2() == 0) {
            throw new SwitchException(Constants.ERR_INVALID_ADDRESS);
        }
    }

    private void initSwitch(Switch aSwitch) throws SwitchException {
        if (!aSwitch.isInitialized()) {
            aSwitch.init();
        }
    }
}
