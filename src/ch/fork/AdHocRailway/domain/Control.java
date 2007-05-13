/*------------------------------------------------------------------------
 * 
 * <Control.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:54:02 BST 2006
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

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import de.dermoba.srcp.client.SRCPSession;

/**
 * Basic Control class. Each Control should inherit from this class, so that it
 * can check the session of its associated ControlObject and if its initialized.
 * 
 * @author fork
 * 
 */
public abstract class Control {

    protected SRCPSession session = null;

    /** Checks the validity of the given ControlObject.
     * 
     * @param co The ControlObject to test
     * @throws NoSessionException If the ControlObject has no valid SRCPSession
     * @throws InvalidAddressException If the ControlObject has no valid Address
     */
    public void checkControlObject(ControlObject co) throws NoSessionException,
        InvalidAddressException {
        if (co.getSession() == null) {
            throw new NoSessionException();
        }
        for (Address a : co.getAddresses()) {
            if (a.getAddress() == 0) {
                throw new InvalidAddressException();
            }
        }
    }

    /** Sets the active SRCPSession on the ControlObject.
     * 
     * @param co
     */
    public void setSessionOnControlObject(ControlObject co) {
        co.setSession(session);
    }

    /** Initialises the ControlObject
     * 
     * @param object
     * @throws ControlException If the initialization fails
     */
    protected void initControlObject(ControlObject object)
        throws ControlException {
        if (!object.isInitialized()) {
            object.init();
        }
    }
    
    abstract public void undoLastChange() throws ControlException;
    abstract public void previousDeviceToDefault() throws ControlException;
}
