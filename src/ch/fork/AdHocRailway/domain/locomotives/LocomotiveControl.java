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
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GLInfoListener;

public class LocomotiveControl extends Control implements GLInfoListener {
    private static LocomotiveControl       instance;
    private List<LocomotiveChangeListener> listeners;
    private Map<Address, Locomotive>       addressToLocomotives;
    private SortedSet<LocomotiveGroup>          locomotiveGroups;

    private LocomotiveControl() {
        listeners = new ArrayList<LocomotiveChangeListener>();
        addressToLocomotives = new HashMap<Address, Locomotive>();
        locomotiveGroups = new TreeSet<LocomotiveGroup>();
    }

    public static LocomotiveControl getInstance() {
        if (instance == null) {
            instance = new LocomotiveControl();
        }
        return instance;
    }

    public void registerLocomotive(Locomotive locomotiveToRegister) {
        addressToLocomotives.put(locomotiveToRegister.getAddress(),
            locomotiveToRegister);
        setSessionOnControlObject(locomotiveToRegister);
        LockControl.getInstance().registerControlObject(locomotiveToRegister);
    }

    public void registerLocomotives(Collection<Locomotive> locomotivesToRegister) {
        LockControl lc = LockControl.getInstance();
        for (Locomotive l : locomotivesToRegister) {
            addressToLocomotives.put(l.getAddress(), l);
            setSessionOnControlObject(l);
            lc.registerControlObject(l);
        }
    }

    public void unregisterLocomotive(Locomotive locomotiveToUnregister) {
        addressToLocomotives.remove(locomotiveToUnregister.getAddress());
        LockControl.getInstance().unregisterControlObject(
            locomotiveToUnregister);
    }

    public void unregisterAllLocomotives() {
        for (Locomotive l : addressToLocomotives.values()) {
            // l.term();
        }
        addressToLocomotives.clear();
        LockControl.getInstance().unregisterAllControlObjects();
    }

    public SortedSet<Locomotive> getLocomotives() {
        return new TreeSet<Locomotive>(addressToLocomotives.values());
    }

    public void registerLocomotiveGroup(LocomotiveGroup locomotiveGroup) {
        locomotiveGroups.add(locomotiveGroup);
    }

    public void registerLocomotiveGroups(
        Collection<LocomotiveGroup> locomotiveGroupsToRegister) {
        locomotiveGroups.addAll(locomotiveGroupsToRegister);
    }

    public void unregisterAllLocomotiveGroups() {
        locomotiveGroups.clear();
    }

    public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
        for (Locomotive l : addressToLocomotives.values()) {
            setSessionOnControlObject(l);
        }
        session.getInfoChannel().addGLInfoListener(this);
    }

    public void toggleDirection(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.toggleDirection();
    }

    public void setSpeed(Locomotive locomotive, int speed)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.setSpeed(speed);
    }

    public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeed();
    }

    public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeed();
    }

    public void increaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.increaseSpeedStep();
    }

    public void decreaseSpeedStep(Locomotive locomotive)
        throws LocomotiveException {
        checkLocomotive(locomotive);
        initLocomotive(locomotive);
        locomotive.decreaseSpeedStep();
    }

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
        // FIXME: removed to get a smoother LocomotiveWidget
        // Address addr = new Address(bus, address);
        // Locomotive locomotive = locomotives.get(addr);
        // locomotive.locomotiveChanged(drivemode, v, vMax, functions);
        // informListeners(locomotive);
    }

    public void GLterm(double timestamp, int bus, int address) {
        Address addr = new Address(bus, address);
        Locomotive locomotive = addressToLocomotives.get(addr);
        if (locomotive != null) {
            locomotive.locomotiveTerminated();
            informListeners(locomotive);
        }
    }

    public void addLocomotiveChangeListener(LocomotiveChangeListener l) {
        listeners.add(l);
    }

    public void removeLocomotiveChangeListener(LocomotiveChangeListener l) {
        listeners.remove(l);
    }

    private void informListeners(Locomotive changedLocomotive) {
        for (LocomotiveChangeListener l : listeners) {
            l.locomotiveChanged(changedLocomotive);
        }
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

}
