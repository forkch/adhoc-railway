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

import java.awt.Image;
import java.awt.image.ImageObserver;

import ch.fork.RailControl.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;

public abstract class Switch {

    protected int number;
    protected int bus;
    protected Address address;
    protected String desc;
    protected enum SwitchState {
		LEFT, STRAIGHT, RIGHT, UNDEF
	};
	protected SwitchState switchState = SwitchState.STRAIGHT;
	protected boolean initialized = false;

    protected int SWITCH_PORT_ACTIVATE = 1;
    protected int SWITCH_PORT_DEACTIVATE = 0;
    protected int SWITCH_DELAY  = 100;
    protected String ERR_SWITCH_LOCKED  = "Switch locked";
    protected String ERR_TOGGLE_FAILED  = "Toggle of switch failed";
    protected String ERR_INIT_FAILED  = "Init failed";
    protected String ERR_NO_SESSION  = "Not connected";
	protected SRCPSession session;

	public Switch(int number, String desc, int bus, Address address) {
		this.number = number;
		this.bus = bus;
		this.address = address;
		this.desc = desc;
	}
	
	public void init() throws SwitchException {
		if(session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
	}
    protected abstract void toggle() throws SwitchException;
    protected abstract void setStraight() throws SwitchException;
    protected abstract void setCurvedLeft() throws SwitchException;
    protected abstract void setCurvedRight() throws SwitchException;
    protected abstract void switchPortChanged(int pAddress, int pActivatedPort, int value);
    protected abstract void switchInitialized(int pAddress);
    protected abstract void switchTerminated(int pAddress);
    public abstract Image getImage(ImageObserver obs);
    /**
     * Get name.
     *
     * @return name as String.
     */
    public int getNumber()
    {
        return number;
    }
    
    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setNumber(int number)
    {
        this.number = number;
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
    
    public String getType() {
        return this.getClass().getSimpleName();
    }

	public int getBus() {
		return bus;
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
	}

	public boolean isInitialized() {
		return initialized;
	}
	
	
}
