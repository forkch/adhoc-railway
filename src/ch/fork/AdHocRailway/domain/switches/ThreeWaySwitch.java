
package ch.fork.AdHocRailway.domain.switches;

import java.util.HashMap;
import java.util.Map;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;

public class ThreeWaySwitch extends Switch {
    private DefaultSwitch        switch1;
    private DefaultSwitch        switch2;
    private Map<Integer, Switch> addressToSwitch;

    public ThreeWaySwitch(int pNumber, String pDesc) {
        this(pNumber, pDesc, 1, new Address(0, 0));
    }

    public ThreeWaySwitch(int pNumber, String pDesc, int pBus, Address address) {
        super(pNumber, pDesc, pBus, address);
        switch1 = new DefaultSwitch(number, desc, bus, new Address(address
            .getAddress1()));
        switch2 = new DefaultSwitch(number, desc, bus, new Address(address
            .getAddress2()));
        addressToSwitch = new HashMap<Integer, Switch>();
        addressToSwitch.put(address.getAddress1(), switch1);
        addressToSwitch.put(address.getAddress2(), switch2);
    }

    protected void init() throws SwitchException {
        super.init();
        switch1.setSession(session);
        switch2.setSession(session);
        switch1.init();
        switch2.init();
        initialized = true;
    }

    protected void term() throws SwitchException {
        super.term();
        switch1.term();
        switch2.term();
        initialized = false;
    }

    @Override
    protected void reinit() throws SwitchException {
        if (switch1 != null) {
            switch1.reinit();
        }
        if (switch2 != null) {
            switch2.reinit();
        }
        if (session != null) {
            init();
        }
    }

    protected void toggle() throws SwitchException {
        if (session == null) {
            throw new SwitchException(ERR_NO_SESSION);
        }
        switch (switchState) {
        case LEFT:
            switch1.setStraight();
            switch2.setStraight();
            switchState = SwitchState.STRAIGHT;
            break;
        case STRAIGHT:
            switch1.setStraight();
            switch2.setCurvedRight();
            switchState = SwitchState.RIGHT;
            break;
        case RIGHT:
            switch1.setCurvedRight();
            switch2.setStraight();
            switchState = SwitchState.LEFT;
            break;
        case UNDEF:
            switch1.setStraight();
            switch2.setStraight();
            switchState = SwitchState.STRAIGHT;
            break;
        }
    }

    protected void switchPortChanged(int pAddress, int pChangedPort, int value) {
        Switch s = addressToSwitch.get(pAddress);
        s.switchPortChanged(pAddress, pChangedPort, value);
        if(switch1.getSwitchState() == SwitchState.STRAIGHT && switch2.getSwitchState() == SwitchState.STRAIGHT) {
            switchState = SwitchState.STRAIGHT;
        } else if(switch1.getSwitchState() == SwitchState.LEFT && switch2.getSwitchState() == SwitchState.STRAIGHT) {
            switchState = SwitchState.LEFT;
        } else if(switch1.getSwitchState() == SwitchState.STRAIGHT && switch2.getSwitchState() == SwitchState.LEFT) {
            switchState = SwitchState.RIGHT;
        } else {
            switchState = SwitchState.UNDEF;
        }
    }

    @Override
    protected void switchInitialized(int pBus, int pAddress) {
        addressToSwitch.get(pAddress).switchInitialized(pBus, pAddress);
    }

    @Override
    protected void switchTerminated(int pAddress) {
        addressToSwitch.get(pAddress).switchTerminated(pAddress);
    }

    @Override
    protected void setStraight() throws SwitchException {
        switch1.setStraight();
        switch2.setStraight();
        switchState = SwitchState.STRAIGHT;
    }

    @Override
    protected void setCurvedLeft() throws SwitchException {
        switch1.setCurvedRight();
        switch2.setStraight();
        switchState = SwitchState.LEFT;
    }

    @Override
    protected void setCurvedRight() throws SwitchException {
        switch1.setStraight();
        switch2.setCurvedRight();
        switchState = SwitchState.RIGHT;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
        switch1.setSession(session);
        switch2.setSession(session);
    }

    public void setBus(int bus) {
        this.bus = bus;
        switch1.setBus(bus);
        switch2.setBus(bus);
    }

    public void setAddress(Address address) {
        this.address = address;
        switch1.setAddress(new Address(address.getAddress1(), 0));
        switch2.setAddress(new Address(address.getAddress2(), 0));
        addressToSwitch.clear();
        addressToSwitch.put(address.getAddress1(), switch1);
        addressToSwitch.put(address.getAddress2(), switch2);
        initialized = false;
    }

    @Override
    public Switch clone() {
        ThreeWaySwitch newSwitch = new ThreeWaySwitch(number, desc, bus,
            address);
        newSwitch.setSession(session);
        newSwitch.setSwitchOrientation(switchOrientation);
        newSwitch.setDefaultState(defaultState);
        return newSwitch;
    }
}
