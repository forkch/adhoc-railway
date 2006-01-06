/*------------------------------------------------------------------------
 * 
 * <DefaultSwitch.java>  -  <A standard switch>
 * 
 * begin     : j Tue Jan  3 21:26:08 CET 2006
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

public class ThreeWaySwitch extends Switch {

    private int address1;
    private int address2;
    private GA ga1;
    private GA ga2;
    private Session session;

    private int[] LEFT_PORTS = {1,0};
    private int[] STRAIGHT_PORTS = {0,0};
    private int[] RIGHT_PORTS = {0,1};

    private enum SwitchState { LEFT, STRAIGHT, RIGHT, UNDEF };
    private SwitchState switchState = SwitchState.UNDEF;

    public ThreeWaySwitch(Session pSession, String pName, String pDesc, 
        int pAddress1, int pAddress2) {
        session = pSession;
        name = pName;
        desc = pDesc;
        address1 = pAddress1;
        address2 = pAddress2;
        ga1 = new GA(session);
        ga2 = new GA(session);
    }

	protected void toggle() throws SRCPException {
	    switch(switchState) {
            case LEFT:
                ga1.set(STRAIGHT_PORTS[0], SWITCH_ACTION, SWITCH_DELAY);
                ga2.set(STRAIGHT_PORTS[1], SWITCH_ACTION, SWITCH_DELAY);
            case STRAIGHT:
                ga1.set(RIGHT_PORTS[0], SWITCH_ACTION, SWITCH_DELAY);
                ga2.set(RIGHT_PORTS[1], SWITCH_ACTION, SWITCH_DELAY);
            case RIGHT:
                ga1.set(LEFT_PORTS[0], SWITCH_ACTION, SWITCH_DELAY);
                ga2.set(LEFT_PORTS[1], SWITCH_ACTION, SWITCH_DELAY);
            case UNDEF:
                ga1.set(STRAIGHT_PORTS[0], SWITCH_ACTION, SWITCH_DELAY);
                ga2.set(STRAIGHT_PORTS[1], SWITCH_ACTION, SWITCH_DELAY);
        }
	}
    
    /**
     * Get address1.
     *
     * @return address1 as int.
     */
    public int getAddress1()
    {
        return address1;
    }
    
    /**
     * Set address1.
     *
     * @param address1 the value to set.
     */
    public void setAddress1(int address1)
    {
        this.address1 = address1;
    }
    
    /**
     * Get address2.
     *
     * @return address2 as int.
     */
    public int getAddress2()
    {
        return address2;
    }
    
    /**
     * Set address2.
     *
     * @param address2 the value to set.
     */
    public void setAddress2(int address2)
    {
        this.address2 = address2;
    }
}
