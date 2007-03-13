/*------------------------------------------------------------------------
 * 
 * <./domain/switches/ThreeWaySwitch.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:54:59 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/


package ch.fork.AdHocRailway.domain.switches;

import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;

public class ThreeWaySwitch extends Switch {
    private DefaultSwitch        switch1;
    private DefaultSwitch        switch2;
    private Map<Address, Switch> addressToSwitch;

    public ThreeWaySwitch(int pNumber, String pDesc) {
        this(pNumber, pDesc, new Address[] { new Address(DEFAULT_BUS, 0),
            new Address(DEFAULT_BUS, 0) });
    }

    public ThreeWaySwitch(int pNumber, String pDesc, Address[] addresses) {
        super(pNumber, pDesc, addresses);
        switch1 = new DefaultSwitch(number, desc, new Address(addresses[0]
            .getBus(), addresses[0].getAddress()));
        switch2 = new DefaultSwitch(number, desc, new Address(addresses[1]
            .getBus(), addresses[1].getAddress()));
        addressToSwitch = new HashMap<Address, Switch>();
        addressToSwitch.put(addresses[0], switch1);
        addressToSwitch.put(addresses[1], switch2);
    }

    protected void init() throws SwitchException {
        switch1.setSession(session);
        switch2.setSession(session);
        switch1.init();
        switch2.init();
        initialized = true;
    }

    protected void term() throws SwitchException {
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
        switch (switchState) {
        case LEFT:
            switch1.setStraight();
            switch2.setStraight();
            //switchState = SwitchState.STRAIGHT;
            break;
        case STRAIGHT:
            switch1.setStraight();
            switch2.setCurvedRight();
            //switchState = SwitchState.RIGHT;
            break;
        case RIGHT:
            switch1.setCurvedRight();
            switch2.setStraight();
            //switchState = SwitchState.LEFT;
            break;
        case UNDEF:
            switch1.setStraight();
            switch2.setStraight();
            //switchState = SwitchState.STRAIGHT;
            break;
        }
    }

    protected void switchPortChanged(Address addr, int pChangedPort, int value) {
        Switch s = addressToSwitch.get(addr);
        s.switchPortChanged(addr, pChangedPort, value);
        if (switch1.getSwitchState() == SwitchState.STRAIGHT
            && switch2.getSwitchState() == SwitchState.STRAIGHT) {
            switchState = SwitchState.STRAIGHT;
        } else if (switch1.getSwitchState() == SwitchState.LEFT
            && switch2.getSwitchState() == SwitchState.STRAIGHT) {
            switchState = SwitchState.LEFT;
        } else if (switch1.getSwitchState() == SwitchState.STRAIGHT
            && switch2.getSwitchState() == SwitchState.LEFT) {
            switchState = SwitchState.RIGHT;
        } else {
            switchState = SwitchState.UNDEF;
        }
    }

    @Override
    protected void switchInitialized(Address addr) {
        addressToSwitch.get(addr).switchInitialized(addr);
    }

    @Override
    protected void switchTerminated(Address addr) {
        addressToSwitch.get(addr).switchTerminated(addr);
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

    protected void setSession(SRCPSession session) {
        this.session = session;
        switch1.setSession(session);
        switch2.setSession(session);
    }

    public void setBus(int bus) {
        this.addresses[0].setBus(bus);
        this.addresses[1].setBus(bus);
        switch1.getAddress(0).setBus(bus);
        switch2.getAddress(1).setBus(bus);
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
        Address address1 = new Address(addresses[0].getBus(), addresses[0]
            .getAddress());
        address1.setAddressSwitched(addresses[0].isAddressSwitched());
        Address address2 = new Address(addresses[1].getBus(), addresses[1]
            .getAddress());
        address1.setAddressSwitched(addresses[1].isAddressSwitched());
        switch1.setAddress(address1);
        switch2.setAddress(address2);
        addressToSwitch.clear();
        addressToSwitch.put(addresses[0], switch1);
        addressToSwitch.put(addresses[1], switch2);
        initialized = false;
    }

    @Override
    public Switch clone() {
        ThreeWaySwitch newSwitch = new ThreeWaySwitch(number, desc, addresses);
        newSwitch.setSession(session);
        newSwitch.setSwitchOrientation(switchOrientation);
        newSwitch.setDefaultState(defaultState);
        return newSwitch;
    }

}
