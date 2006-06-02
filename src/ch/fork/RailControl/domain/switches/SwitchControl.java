package ch.fork.RailControl.domain.switches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.RailControl.domain.Constants;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GAInfoListener;

public class SwitchControl implements GAInfoListener {

    private static SwitchControl instance;

    private SRCPSession session;

    private List<SwitchChangeListener> listeners;

    private Map<Integer, Switch> addressToSwitch;

    private SwitchControl() {
        listeners = new ArrayList<SwitchChangeListener>();
        addressToSwitch = new HashMap<Integer, Switch>();
    }

    public static SwitchControl getInstance() {
        if (instance == null) {
            instance = new SwitchControl();
            return instance;
        } else {
            return instance;
        }
    }

    public void registerSwitches(Collection<Switch> switches) {
        for (Switch aSwitch : switches) {
            Address address = aSwitch.getAddress();
            addressToSwitch.put(address.getAddress1(), aSwitch);
            if (address.getAddress2() != 0) {
                addressToSwitch.put(address.getAddress2(), aSwitch);
            }
        }
    }

    public void unregisterSwitches(List<Switch> switches) {
        for (Switch aSwitch : switches) {
            // aSwitch.term();
            addressToSwitch.remove(aSwitch.getAddress());
        }
    }

    public void unregisterAllSwitches() {
        for (Switch aSwitch : addressToSwitch.values()) {
            // aSwitch.term();
        }
        addressToSwitch.clear();
    }

    public void setSessionOnSwitches(SRCPSession session) {
        for (Switch aSwitch : addressToSwitch.values()) {
            aSwitch.setSession(session);
        }
    }

    public void toggle(Switch aSwitch) throws SwitchException {
        checkSwitchSession(aSwitch);
        initSwitch(aSwitch);
        aSwitch.toggle();

        for (SwitchChangeListener l : listeners) {
            l.switchChanged(aSwitch);
        }
    }

    public void setStraight(Switch aSwitch) throws SwitchException {
        checkSwitchSession(aSwitch);
        initSwitch(aSwitch);
        aSwitch.setStraight();
    }

    public void setCurvedRight(Switch aSwitch) throws SwitchException {
        checkSwitchSession(aSwitch);
        initSwitch(aSwitch);
        aSwitch.setCurvedRight();
    }

    public void setCurvedLeft(Switch aSwitch) throws SwitchException {
        checkSwitchSession(aSwitch);
        initSwitch(aSwitch);
        aSwitch.setCurvedLeft();
    }

    public void addSwitchChangeListener(SwitchChangeListener listener) {
        listeners.add(listener);
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

    public void GAinit(double timestamp, int bus, int address,
        String protocol, String[] params) {
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

    private void informListeners(Switch changedSwitch) {
        for (SwitchChangeListener l : listeners) {
            l.switchChanged(changedSwitch);
        }
    }

    public SRCPSession getSession() {
        return session;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
        setSessionOnSwitches(session);
        session.getInfoChannel().addGAInfoListener(this);
    }

    private void checkSwitchSession(Switch aSwitch) throws SwitchException {
        if (aSwitch.getSession() == null) {
            throw new SwitchException(Constants.ERR_NO_SESSION);
        }
    }

    private void initSwitch(Switch aSwitch) throws SwitchException {
        if (!aSwitch.isInitialized()) {
            aSwitch.init();
        }
    }

}
