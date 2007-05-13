/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/LocomotiveControl.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:01 BST 2006
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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GLInfoListener;

/**
 * Controls all actions which can be performed on Locomotives.
 * 
 * @author fork
 * 
 */
public class LocomotiveControl extends Control implements GLInfoListener {

    private static LocomotiveControl       instance;
    private List<LocomotiveChangeListener> listeners;
    private Map<Address, Locomotive>       addressToLocomotives;
    private SortedSet<LocomotiveGroup>     locomotiveGroups;

    private LocomotiveControl() {
        listeners = new ArrayList<LocomotiveChangeListener>();
        addressToLocomotives = new HashMap<Address, Locomotive>();
        locomotiveGroups = new TreeSet<LocomotiveGroup>();
    }

    /**
     * Gets an instance of a LocomotiveControl.
     * 
     * @return an instance of LocomotiveControl
     */
    public static LocomotiveControl getInstance() {
        if (instance == null) {
            instance = new LocomotiveControl();
        }
        return instance;
    }

    /**
     * Registers a new Locomotive.
     * 
     * @param locomotiveToRegister
     */
    public void registerLocomotive(Locomotive locomotiveToRegister) {
        addressToLocomotives.put(locomotiveToRegister.getAddress(),
            locomotiveToRegister);
        setSessionOnControlObject(locomotiveToRegister);
        LockControl.getInstance().registerControlObject(locomotiveToRegister);
    }

    /**
     * Registers a Collection of Locomotives.
     * 
     * @param locomotivesToRegister
     */
    public void registerLocomotives(Collection<Locomotive> locomotivesToRegister) {
        LockControl lc = LockControl.getInstance();
        for (Locomotive l : locomotivesToRegister) {
            addressToLocomotives.put(l.getAddress(), l);
            setSessionOnControlObject(l);
            lc.registerControlObject(l);
        }
    }

    /**
     * Unregisters a Locomotive
     * 
     * @param locomotiveToUnregister
     */
    public void unregisterLocomotive(Locomotive locomotiveToUnregister) {
        addressToLocomotives.remove(locomotiveToUnregister.getAddress());
        LockControl.getInstance().unregisterControlObject(
            locomotiveToUnregister);
    }

    /**
     * Get a SortedSet of Locomotives.
     * 
     * @return locomotives
     */
    public SortedSet<Locomotive> getLocomotives() {
        return new TreeSet<Locomotive>(addressToLocomotives.values());
    }

    /**
     * Registers a new LocomotiveGroup
     * 
     * @param locomotiveGroup
     */
    public void registerLocomotiveGroup(LocomotiveGroup locomotiveGroup) {
        locomotiveGroups.add(locomotiveGroup);
    }

    /**
     * Registers a Collection of LocomotiveGroups
     * 
     * @param locomotiveGroupsToRegister
     */
    public void registerLocomotiveGroups(
        Collection<LocomotiveGroup> locomotiveGroupsToRegister) {
        locomotiveGroups.addAll(locomotiveGroupsToRegister);
    }

    public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    /**
     * Unregisters all Locomotives and LocomotiveGroups.
     * 
     */
    public void clear() {
        for (Locomotive l : addressToLocomotives.values()) {
            // l.term();
        }
        addressToLocomotives.clear();
        locomotiveGroups.clear();
        LockControl.getInstance().unregisterAllControlObjects();
    }

    /**
     * Sets the SRCPSession on this Control.
     * 
     * @param session
     */
    public void setSession(SRCPSession session) {
        this.session = session;
        for (Locomotive l : addressToLocomotives.values()) {
            setSessionOnControlObject(l);
        }
        session.getInfoChannel().addGLInfoListener(this);
    }

    /**
     * Toggles the direction of the Locomotive
     * 
     * @param locomotive
     * @throws LocomotiveException
     */
    public void toggleDirection(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.toggleDirection();
    }

    /**
     * Sets the speed of the Locomotive
     * 
     * @param locomotive
     * @param speed
     * @throws LocomotiveException
     */
    public void setSpeed(Locomotive locomotive, int speed)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.setSpeed(speed);
    }

    /**
     * Increases the speed of the Locomotive.
     * 
     * @param locomotive
     * @throws LocomotiveException
     */
    public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeed();
    }

    /**
     * Decreases the speed of the Locomotive.
     * 
     * @param locomotive
     * @throws LocomotiveException
     */
    public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeed();
    }

    /**
     * Increases the speed of the Locomotive by a step.
     * 
     * @param locomotive
     * @throws LocomotiveException
     */
    public void increaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeedStep();
    }

    /**
     * Decreases the speed of the Locomotive by a step.
     * 
     * @param locomotive
     * @throws LocomotiveException
     */
    public void decreaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeedStep();
    }

    /**
     * Sets the functions of the Locomotive on or off.
     * 
     * @param locomotive
     * @param functions
     * @throws LocomotiveException
     */
    public void setFunctions(Locomotive locomotive, boolean[] functions)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.setFunctions(functions);
    }

    public void GLinit(double timestamp, int bus, int address, String protocol,
        String[] params) {
        Address addr = new Address(bus, address);
        Locomotive locomotive = addressToLocomotives.get(addr);
        if (locomotive != null) {
            locomotive.locomotiveInitialized(addr, protocol, params);
            informListeners(locomotive);
        }
    }

    public void GLset(double timestamp, int bus, int address, String drivemode,
        int v, int vMax, boolean[] functions) {
        Address addr = new Address(bus, address);
        Locomotive locomotive = addressToLocomotives.get(addr);
        //locomotive.locomotiveChanged(drivemode, v, vMax, functions);
        //informListeners(locomotive);
    }

    public void GLterm(double timestamp, int bus, int address) {
        Address addr = new Address(bus, address);
        Locomotive locomotive = addressToLocomotives.get(addr);
        if (locomotive != null) {
            locomotive.locomotiveTerminated();
            informListeners(locomotive);
        }
    }

    public void addLocomotiveChangeListener(Locomotive loco,
        LocomotiveChangeListener l) {
        listeners.add(l);
    }

    public void removeAllLocomotiveChangeListener() {
        listeners.clear();
    }

    private void informListeners(Locomotive changedLocomotive) {
        for (LocomotiveChangeListener l : listeners)
            l.locomotiveChanged(changedLocomotive);

    }

    private void checkLocomotive(Locomotive locomotive)
        throws LocomotiveException {
        if (locomotive instanceof NoneLocomotive) {
            return;
        }
        try {
            checkControlObject(locomotive);
        } catch (NoSessionException e) {
            throw new LocomotiveException(Constants.ERR_NOT_CONNECTED, e);
        } catch (InvalidAddressException e) {
            throw new LocomotiveException(Constants.ERR_FAILED, e);
        }
    }

    private void initLocomotive(Locomotive locomotive)
        throws LocomotiveException {
        if (!locomotive.isInitialized()) {
            locomotive.init();
        }
    }

	@Override
	public void undoLastChange() throws ControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void previousDeviceToDefault() throws ControlException {
		// TODO Auto-generated method stub
		
	}

}
