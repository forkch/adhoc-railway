/*------------------------------------------------------------------------
 * 
 * <ControlObject.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:54:05 BST 2006
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


package ch.fork.AdHocRailway.domain;

import de.dermoba.srcp.devices.LOCK;

/**
 * Basic Controlable-Object. Each ControlObject has the following attributes:
 * <ul>
 * <li>SRCPSession</li>
 * <li>LOCK</li>
 * <li>Address[]</li>
 * </ul>
 * 
 * @author fork
 * 
 */
public abstract class ControlObject {
	
    protected LOCK        lock;
    protected boolean     locked      = false;
    protected int         lockedBySession;
    protected int         lockDuration;
    protected boolean     initialized = false;

    public ControlObject() {
    	
    }

    public abstract String getDeviceGroup();
    public abstract int[] getAddresses();
    
    public void lockSet(Address addr, int duration, int sessionID) {
        lockDuration = duration;
        lockedBySession = sessionID;
        locked = true;
    }

    public void lockTerm(Address addr) {
        lockDuration = 0;
        lockedBySession = 0;
        locked = false;
    }

    public boolean isInitialized() {
        return initialized;
    }
    
    public void setInitialized(boolean init) {
        initialized = init;
    }

    public LOCK getLock() {
        return lock;
    }

    public int getLockDuration() {
        return lockDuration;
    }

    public int getLockedBySession() {
        return lockedBySession;
    }

    public boolean isLocked() {
        return locked;
    }
}
