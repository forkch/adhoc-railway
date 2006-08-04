
package ch.fork.AdHocRailway.domain.switches;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;

public abstract class Switch extends ControlObject implements Constants,
    Comparable {
    protected int    number;
    protected String desc;

    public enum SwitchState {
        LEFT, STRAIGHT, RIGHT, UNDEF
    };

    protected SwitchState switchState  = SwitchState.UNDEF;
    protected SwitchState defaultState = SwitchState.STRAIGHT;

    public enum SwitchOrientation {
        NORTH, SOUTH, WEST, EAST
    };

    protected SwitchOrientation switchOrientation      = SwitchOrientation.EAST;
    protected int               SWITCH_PORT_ACTIVATE   = 1;
    protected int               SWITCH_PORT_DEACTIVATE = 0;
    protected final int         MAX_ADDRESSES          = 2;
    protected String            ERR_TOGGLE_FAILED      = "Toggle of switch failed";

    public Switch(int number, String desc, Address address) {
        this(number, desc, new Address[] { address });

    }

    public Switch(int number, String desc, Address[] addresses) {
        super(addresses);
        this.number = number;
        this.desc = desc;
    }

    protected abstract void reinit() throws SwitchException;

    protected abstract void init() throws SwitchException;

    protected abstract void term() throws SwitchException;

    protected abstract void toggle() throws SwitchException;

    protected abstract void setStraight() throws SwitchException;

    protected abstract void setCurvedLeft() throws SwitchException;

    protected abstract void setCurvedRight() throws SwitchException;

    protected abstract void switchPortChanged(Address addr, int pChangedPort,
        int value);

    protected abstract void switchInitialized(Address addr);

    protected abstract void switchTerminated(Address addr);

    public abstract Switch clone();

    public boolean equals(Switch aSwitch) {
        for (int i = 0; i < addresses.length; i++) {
            if (!addresses[i].equals(aSwitch.getAddresses()[i])) {
                return false;
            }
        }
        return true;
    }

    public int compareTo(Object o) {
        if (o instanceof Switch) {
            Switch anotherSwitch = (Switch) o;
            if (number < anotherSwitch.getNumber()) {
                return -1;
            } else if (number > anotherSwitch.getNumber()) {
                return 1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    public String toString() {
        String buf = "\"" + number + ": " + getType() + " @";
        for (Address a : addresses) {
            buf += " " + a;
        }
        return buf;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

    public Address getAddress(int index) {
        return addresses[index];
    }


    public SwitchState getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(SwitchState defaultState) {
        this.defaultState = defaultState;
    }

    public SwitchState getSwitchState() {
        return switchState;
    }

    public SwitchOrientation getSwitchOrientation() {
        return switchOrientation;
    }

    public void setSwitchOrientation(SwitchOrientation switchOrientation) {
        this.switchOrientation = switchOrientation;
    }

    public String getDeviceGroup() {
        return "GA";
    }
    
}
