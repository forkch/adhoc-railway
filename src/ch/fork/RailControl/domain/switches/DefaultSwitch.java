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

import de.dermoba.srcp.client.Session;
import de.dermoba.srcp.client.GA;
import de.dermoba.srcp.common.exception.SRCPException;

public class DefaultSwitch extends Switch {

    private int address;
    private GA ga;
    private Session session;

    private int STRAIGHT_PORT = 0;
    private int CURVED_PORT = 1;

    private enum SwitchState { STRAIGHT, CURVED, UNDEF };
    private SwitchState switchState = SwitchState.UNDEF;

    public DefaultSwitch(Session pSession, String pName, String pDesc, 
        int pAddress ) {
        session = pSession;
        name = pName;
        desc = pDesc;
        address = pAddress;
        ga = new GA(session);
    }

	protected void toggle() throws SRCPException {
	    switch(switchState) {
            case STRAIGHT:
                ga.set(CURVED_PORT, SWITCH_ACTION, SWITCH_DELAY);
            case CURVED:
                ga.set(STRAIGHT_PORT, SWITCH_ACTION, SWITCH_DELAY);
            case UNDEF:
                ga.set(STRAIGHT_PORT, SWITCH_ACTION, SWITCH_DELAY);
        }
	}
}
