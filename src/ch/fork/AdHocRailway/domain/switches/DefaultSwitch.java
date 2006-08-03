
package ch.fork.AdHocRailway.domain.switches;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchLockedException;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class DefaultSwitch extends Switch {
    private GA  ga;
    private int STRAIGHT_PORT = 0;
    private int CURVED_PORT   = 1;

    public DefaultSwitch(int pNumber, String pDesc) {
        this(pNumber, pDesc, new Address(DEFAULT_BUS, 1));
    }

    public DefaultSwitch(int pNumber, String pDesc, Address address) {
        super(pNumber, pDesc, address);
    }

    protected void init() throws SwitchException {
        super.init();
        try {
            ga = new GA(session);
            if (!Preferences.getInstance().getBooleanValue("Interface6051")) {
                ga.init(addresses[0].getBus(), addresses[0].getAddress(), "M");
            } else {
                ga.setAddress(addresses[0].getAddress());
                ga.setBus(addresses[0].getBus());
            }
            initialized = true;
        } catch (SRCPException x) {
            if (x instanceof SRCPDeviceLockedException) {
                throw new SwitchLockedException(ERR_LOCKED);
            } else {
                throw new SwitchException(ERR_INIT_FAILED, x);
            }
        }
    }

    protected void term() throws SwitchException {
        try {
            super.term();
            ga.term();
            initialized = false;
        } catch (SRCPException x) {
            if (x instanceof SRCPDeviceLockedException) {
                throw new SwitchLockedException(ERR_LOCKED);
            } else {
                throw new SwitchException(ERR_TERM_FAILED, x);
            }
        }
    }

    @Override
    protected void reinit() throws SwitchException {
        try {
            if (ga != null) {
                ga.term();
            }
        } catch (SRCPException e) {
            throw new SwitchException(ERR_REINIT_FAILED, e);
        }
        if (session != null) {
            init();
        }
    }

    protected void toggle() throws SwitchException {
        try {
            int defaultActivationTime = Preferences.getInstance().getIntValue(
                "DefaultActivationTime");
            switch (switchState) {
            case STRAIGHT:
                ga.set(getPort(CURVED_PORT), SWITCH_PORT_ACTIVATE,
                    defaultActivationTime);
                ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_DEACTIVATE,
                    defaultActivationTime);
                switchState = SwitchState.LEFT;
                break;
            case RIGHT:
            case LEFT:
                ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_ACTIVATE,
                    defaultActivationTime);
                ga.set(getPort(CURVED_PORT), SWITCH_PORT_DEACTIVATE,
                    defaultActivationTime);
                switchState = SwitchState.STRAIGHT;
                break;
            case UNDEF:
                if (defaultState == SwitchState.STRAIGHT) {
                    ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_ACTIVATE,
                        defaultActivationTime);
                    ga.set(getPort(CURVED_PORT), SWITCH_PORT_DEACTIVATE,
                        defaultActivationTime);
                    switchState = SwitchState.STRAIGHT;
                } else if (defaultState == SwitchState.RIGHT
                    || defaultState == SwitchState.LEFT) {

                    ga.set(getPort(CURVED_PORT), SWITCH_PORT_ACTIVATE,
                        defaultActivationTime);
                    ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_DEACTIVATE,
                        defaultActivationTime);
                    switchState = SwitchState.LEFT;
                }
            }
        } catch (SRCPException x) {
            if (x instanceof SRCPDeviceLockedException) {
                throw new SwitchLockedException(ERR_LOCKED);
            } else {
                throw new SwitchException(ERR_TOGGLE_FAILED, x);
            }
        }
    }

    protected void switchPortChanged(Address addr, int pChangedPort, int value) {
        if (value == 0) {
        } else {
            // a port has been ACTIVATED
            if (pChangedPort == getPort(STRAIGHT_PORT)) {
                switchState = SwitchState.STRAIGHT;
            } else if (pChangedPort == getPort(CURVED_PORT)) {
                switchState = SwitchState.LEFT;
            }

        }
    }

    @Override
    protected void switchInitialized(Address addr) {
        ga = new GA(session);
        this.addresses[0] = addr;
        ga.setBus(addr.getBus());
        ga.setAddress(addr.getAddress());
        initialized = true;
    }

    @Override
    protected void switchTerminated(Address addr) {
        ga = null;
        initialized = false;
    }

    @Override
    protected void setStraight() throws SwitchException {
        try {
            int defaultActivationTime = Preferences.getInstance().getIntValue(
                "DefaultActivationTime");
            if (defaultState == SwitchState.STRAIGHT) {
                ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_ACTIVATE,
                    defaultActivationTime);
                ga.set(getPort(CURVED_PORT), SWITCH_PORT_DEACTIVATE,
                    defaultActivationTime);
                switchState = SwitchState.STRAIGHT;
            } else if (defaultState == SwitchState.LEFT
                || defaultState == SwitchState.RIGHT) {
                ga.set(getPort(CURVED_PORT), SWITCH_PORT_ACTIVATE,
                    defaultActivationTime);
                ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_DEACTIVATE,
                    defaultActivationTime);
                switchState = SwitchState.LEFT;
            }
        } catch (SRCPException e) {
            throw new SwitchException(ERR_TOGGLE_FAILED, e);
        }
    }

    @Override
    protected void setCurvedLeft() throws SwitchException {
        try {
            int defaultActivationTime = Preferences.getInstance().getIntValue(
                "DefaultActivationTime");
            if (defaultState == SwitchState.LEFT
                || defaultState == SwitchState.RIGHT) {
                ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_ACTIVATE,
                    defaultActivationTime);
                ga.set(getPort(CURVED_PORT), SWITCH_PORT_DEACTIVATE,
                    defaultActivationTime);
                switchState = SwitchState.STRAIGHT;
            } else if (defaultState == SwitchState.STRAIGHT) {
                ga.set(getPort(CURVED_PORT), SWITCH_PORT_ACTIVATE,
                    defaultActivationTime);
                ga.set(getPort(STRAIGHT_PORT), SWITCH_PORT_DEACTIVATE,
                    defaultActivationTime);
                switchState = SwitchState.LEFT;
            }
        } catch (SRCPException e) {
            throw new SwitchException(ERR_TOGGLE_FAILED, e);
        }
    }

    @Override
    protected void setCurvedRight() throws SwitchException {
        setCurvedLeft();
    }

    @Override
    public Switch clone() {
        DefaultSwitch newSwitch = new DefaultSwitch(number, desc, addresses[0]);
        newSwitch.setSession(session);
        newSwitch.setSwitchOrientation(switchOrientation);
        newSwitch.setDefaultState(defaultState);
        return newSwitch;
    }

    protected void setAddress(Address address) {
        this.addresses[0] = address;
    }

    /**
     * Returns the port to activate according to the addressSwitched flag.
     * 
     * @param wantedPort
     *            The port to 'convert'
     * @return The 'converted' port
     */
    private int getPort(int wantedPort) {
        if (!addresses[0].isAddressSwitched()) {
            return wantedPort;
        } else {
            if (wantedPort == STRAIGHT_PORT) {
                return CURVED_PORT;
            } else {
                return STRAIGHT_PORT;
            }
        }
    }
}
