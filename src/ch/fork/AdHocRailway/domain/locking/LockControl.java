/*------------------------------------------------------------------------
 * 
 * <./domain/locking/LockControl.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:45 BST 2006
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

package ch.fork.AdHocRailway.domain.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import ch.fork.AdHocRailway.domain.locking.exception.LockingException;
import ch.fork.AdHocRailway.domain.locomotives.NoneLocomotive;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.LOCK;
import de.dermoba.srcp.devices.LOCKInfoListener;

public class LockControl extends Control implements LOCKInfoListener, Constants {

	private static LockControl instance = null;

	private LOCK locker;

	private Map<Address, ControlObject> addressToControlObject;

	private List<LockChangeListener> listeners;

	private LockControl() {
		addressToControlObject = new HashMap<Address, ControlObject>();
		listeners = new ArrayList<LockChangeListener>();
	}

	public static LockControl getInstance() {
		if (instance == null) {
			instance = new LockControl();
		}
		return instance;
	}

	public void registerControlObject(ControlObject object) {
		Address[] addresses = object.getAddresses();
		addressToControlObject.put(addresses[0], object);
		for (int i = 1; i < addresses.length; i++) {
			if (addresses[i] != null) {
				addressToControlObject.put(addresses[i], object);
			}
		}
		setSessionOnControlObject(object);
	}

	public void registerControlObjects(Collection<ControlObject> objects) {
		for (ControlObject anObject : objects) {
			Address[] addresses = anObject.getAddresses();
			addressToControlObject.put(addresses[0], anObject);
			for (int i = 1; i < addresses.length; i++) {
				if (addresses[i] != null) {
					addressToControlObject.put(addresses[i], anObject);
				}
			}
			setSessionOnControlObject(anObject);
		}
	}

	public void unregisterControlObject(ControlObject object) {
		Address[] addresses = object.getAddresses();
		addressToControlObject.remove(addresses[0]);
		for (int i = 1; i < addresses.length; i++) {
			if (addresses[i] != null) {
				addressToControlObject.remove(addresses[i]);
			}
		}
	}

	public void unregisterAllControlObjects() {
		addressToControlObject.clear();
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		session.getInfoChannel().addLOCKInfoListener(this);
	}

	public boolean acquireLock(ControlObject object) throws LockingException {
		try {
			if (object instanceof NoneLocomotive) {
				NoneLocomotive none = (NoneLocomotive) object;
				return false;
			}
			checkControlObject(object);
			initControlObject(object);
			for (Address address : object.getAddresses()) {
				LOCK lock = object.getLock();
				try {
					lock.set(object.getDeviceGroup(), address.getAddress(),
							Preferences.getInstance().getIntValue(
									PreferencesKeys.LOCK_DURATION));
				} catch (SRCPDeviceLockedException e) {
					throw new LockingException(ERR_LOCKED, e);
				} catch (SRCPException e) {
					throw new LockingException(ERR_FAILED, e);
				}
			}
		} catch (NoSessionException e) {
			throw new LockingException(Constants.ERR_NOT_CONNECTED, e);
		} catch (InvalidAddressException e) {
			throw new LockingException(Constants.ERR_FAILED, e);
		} catch (ControlException e) {
			throw new LockingException(Constants.ERR_FAILED, e);
		}
		return true;
	}

	public boolean releaseLock(ControlObject object) throws LockingException {
		try {
			if (object instanceof NoneLocomotive) {
				NoneLocomotive none = (NoneLocomotive) object;
				return false;
			}
			checkControlObject(object);
			initControlObject(object);
			for (Address address : object.getAddresses()) {
				LOCK lock = object.getLock();
				try {
					lock.term(object.getDeviceGroup(), address.getAddress());
				} catch (SRCPDeviceLockedException e) {
					throw new LockingException(ERR_LOCKED, e);
				} catch (SRCPException e) {
					throw new LockingException(ERR_FAILED, e);
				}
			}
		} catch (NoSessionException e) {
			throw new LockingException(Constants.ERR_NOT_CONNECTED, e);
		} catch (InvalidAddressException e) {
			throw new LockingException(Constants.ERR_FAILED, e);
		} catch (ControlException e) {
			throw new LockingException(Constants.ERR_FAILED, e);
		}
		return true;
	}

	public void LOCKset(double timestamp, int bus, int address,
			String deviceGroup, int duration, int sessionID) {
		/*
		 * System.out.println("LOCKset(" + bus + " , " + address + " , " +
		 * deviceGroup + " , " + duration + " , " + sessionID + " )");
		 */
		Address addr = new Address(bus, address);
		ControlObject object = addressToControlObject.get(addr);
		if (object != null) {
			object.lockSet(addr, duration, sessionID);
			informListeners(object);
		}
	}

	public void LOCKterm(double timestamp, int bus, int address,
			String deviceGroup) {
		/*
		 * System.out.println("LOCKtern(" + bus + " , " + address + " , " +
		 * deviceGroup + " )");
		 */
		Address addr = new Address(bus, address);
		ControlObject object = addressToControlObject.get(addr);
		if (object != null) {
			object.lockTerm(addr);
			informListeners(object);
		}
	}

	private void informListeners(ControlObject object) {
		for (LockChangeListener l : listeners) {
			l.lockChanged(object);
		}
	}

	public void addLockChangeListener(LockChangeListener l) {
		listeners.add(l);
	}

	public void removeLockChangeListener(LockChangeListener l) {
		listeners.remove(l);
	}

	public ControlObject getControlObject(Address address) {
		return null;
	}

	public int getSessionID() {
		if (session != null)
			return session.getCommandChannelID();
		else
			return -1;
	}
}
