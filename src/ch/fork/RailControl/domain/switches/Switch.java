/*------------------------------------------------------------------------
 * 
 * <Switch.java>  -  <Represents a switch>
 * 
 * begin     : Tue Jan  3 21:24:40 CET 2006
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
import de.dermoba.srcp.common.exception.SRCPException;

public abstract class Switch {

    protected String name;
    protected int bus;
    protected String desc;

    protected int SWITCH_ACTION = 1;
    protected int SWITCH_DELAY  = 100;
    protected String ERR_SWITCH_LOCKED  = "Switch locked";
    protected String ERR_TOGGLE_FAILED  = "Toggle of switch failed";
	protected SRCPSession session;

    protected abstract void toggle() throws SwitchException;
    protected abstract boolean switchChanged(int pAddress, int pActivatedPort);
    
    /**
     * Get name.
     *
     * @return name as String.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Get desc.
     *
     * @return desc as String.
     */
    public String getDesc()
    {
        return desc;
    }
    
    /**
     * Set desc.
     *
     * @param desc the value to set.
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }
}
