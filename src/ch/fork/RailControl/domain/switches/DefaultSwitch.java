/*------------------------------------------------------------------------
 * 
 * <DefaultSwitch.java>  -  <A standard switch>
 * 
 * begin     : Tue Jan  3 21:26:08 CET 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : bm@fork.ch
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

package ch.fork.RailControl.domain.switches;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GA;
import de.dermoba.srcp.common.exception.*;

public class DefaultSwitch extends Switch {

    private int address;
    private GA ga;
    private int STRAIGHT_PORT = 0;
    private int CURVED_PORT = 1;

    private enum SwitchState { STRAIGHT, CURVED, UNDEF };
    private SwitchState switchState = SwitchState.CURVED;

    public DefaultSwitch(SRCPSession pSession, String pName, String pDesc, 
        int pAddress, int pBus ) throws SRCPException {
        session = pSession;
        name = pName;
        desc = pDesc;
        address = pAddress;
        bus = pBus;
        ga = new GA(session);
        ga.init(pBus, pAddress, "M");
        //TODO: immediately a get to determine state !!!!
    }

	protected void toggle() throws SwitchException {
	    try {
    	    switch(switchState) {
                case STRAIGHT:
                    ga.set(CURVED_PORT, SWITCH_ACTION, SWITCH_DELAY);
                    //FIXME
                    switchState = SwitchState.CURVED;
                    break;
                case CURVED:
                    ga.set(STRAIGHT_PORT, SWITCH_ACTION, SWITCH_DELAY);
                    //FIXME
                    switchState = SwitchState.STRAIGHT;
                    break;
                case UNDEF:
                    return;
            }
	    } catch (SRCPException x ) {
            if(x instanceof SRCPDeviceLockedException) {
                throw new SwitchLockedException(ERR_SWITCH_LOCKED);
            } else {
                throw new SwitchException(ERR_TOGGLE_FAILED, x);
            }
	    }
	}

    protected boolean switchChanged(int pAddress, int pActivatedPort) {
        if(address == pAddress) {
            if(pActivatedPort == STRAIGHT_PORT) {
                switchState = SwitchState.STRAIGHT;
            } else if(pActivatedPort == CURVED_PORT) {
                switchState = SwitchState.CURVED;
            } else {
                return false;
            }
        } else {
            //should not happen
            return false;
        }
        return true;
    }
    
    /**
     * Get address.
     *
     * @return address as int.
     */
    public int getAddress() {
        return address;
    }
    
    /**
     * Set address.
     *
     * @param address the value to set.
     */
    public void setAddress(int address) {
        this.address = address;
    }
}
