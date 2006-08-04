
package ch.fork.AdHocRailway.domain.switches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GAInfoListener;

public class SwitchControl extends Control implements GAInfoListener {
    private static SwitchControl       instance;
    private List<SwitchChangeListener> listeners;
    private List<SwitchGroup>          switchGroups;
    private Map<Address, Switch>       addressToSwitch;
    private Map<Integer, Switch>       numberToSwitch;

    private SwitchControl() {
        listeners = new ArrayList<SwitchChangeListener>();
        addressToSwitch = new HashMap<Address, Switch>();
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
        Address[] addresses = aSwitch.getAddresses();
        addressToSwitch.put(addresses[0], aSwitch);
        for (int i = 1; i < addresses.length; i++) {
            if (addresses[i] != null) {
                addressToSwitch.put(addresses[i], aSwitch);
            }
        }
        numberToSwitch.put(aSwitch.getNumber(), aSwitch);
        setSessionOnControlObject(aSwitch);
        LockControl.getInstance().registerControlObject(aSwitch);
    }

    public void registerSwitches(Collection<Switch> switches) {
        LockControl lc = LockControl.getInstance();
        for (Switch aSwitch : switches) {
            Address[] addresses = aSwitch.getAddresses();
            addressToSwitch.put(addresses[0], aSwitch);
            for (int i = 1; i < addresses.length; i++) {
                if (addresses[i] != null) {
                    addressToSwitch.put(addresses[i], aSwitch);
                }
            }
            numberToSwitch.put(aSwitch.getNumber(), aSwitch);
            setSessionOnControlObject(aSwitch);
            lc.registerControlObject(aSwitch);
        }
    }

    public void unregisterSwitch(Switch aSwitch) {
        Address[] addresses = aSwitch.getAddresses();
        addressToSwitch.remove(addresses[0]);
        for (int i = 1; i < addresses.length; i++) {
            if (addresses[i] != null) {
                addressToSwitch.remove(addresses[i]);
            }
            addressToSwitch.remove(addresses[i]);
        }
        numberToSwitch.remove(aSwitch.getNumber());
        LockControl.getInstance().unregisterControlObject(aSwitch);
    }

    public void unregisterAllSwitches() {
        for (Switch aSwitch : addressToSwitch.values()) {
            // aSwitch.term();
        }
        addressToSwitch.clear();
        numberToSwitch.clear();
        LockControl.getInstance().unregisterAllControlObjects();
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
            setSessionOnControlObject(aSwitch);
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
        Address addr = new Address(bus, address);
        Switch s = addressToSwitch.get(addr);
        if (s != null) {
            s.switchPortChanged(addr, port, value);
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
        Address addr = new Address(bus, address);
        Switch s = addressToSwitch.get(addr);
        if (s != null) {
            s.switchInitialized(addr);
            informListeners(s);
        }
    }

    public void GAterm(double timestamp, int bus, int address) {
        /*
         * System.out.println("GAterm( " + bus + " , " + address + " )");
         */
        Address addr = new Address(bus, address);
        Switch s = addressToSwitch.get(addr);
        s.switchTerminated(addr);
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
        try {
            checkControlObject(aSwitch);
        } catch (NoSessionException e) {
            throw new SwitchException(Constants.ERR_NOT_CONNECTED, e);
        } catch (InvalidAddressException e) {
            throw new SwitchException(Constants.ERR_FAILED, e);
        }
        if (aSwitch instanceof ThreeWaySwitch
            && aSwitch.getAddress(1).getAddress() == 0) {
            throw new SwitchException(Constants.ERR_FAILED,
                new InvalidAddressException());
        }
    }

    private void initSwitch(Switch aSwitch) throws SwitchException {
        if (!aSwitch.isInitialized()) {
            aSwitch.init();
        }
    }

}
