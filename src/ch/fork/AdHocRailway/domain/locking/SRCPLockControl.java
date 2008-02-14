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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.LOCK;
import de.dermoba.srcp.devices.LOCKInfoListener;

public class SRCPLockControl implements LOCKInfoListener, Constants {
	private static Logger					logger		= Logger
																.getLogger(SRCPLockControl.class);
	private static SRCPLockControl			instance	= null;
	private SRCPSession						session;

	Map<String, Map<LookupAddress, Object>>	addressToControlObject;
	Map<Object, SRCPLock>					locks;

	private List<LockChangeListener>		listeners;

	private SRCPLockControl() {
		addressToControlObject = new HashMap<String, Map<LookupAddress, Object>>();
		locks = new HashMap<Object, SRCPLock>();
		listeners = new ArrayList<LockChangeListener>();
	}

	public static SRCPLockControl getInstance() {
		if (instance == null) {
			instance = new SRCPLockControl();
		}
		return instance;
	}

	public void registerControlObject(String deviceGroup,
			LookupAddress address, Object object) {
		System.out.println(object);
		if (addressToControlObject.get(deviceGroup) == null)
			addressToControlObject.put(deviceGroup,
					new HashMap<LookupAddress, Object>());
		addressToControlObject.get(deviceGroup).put(address, object);
		SRCPLock lock = new SRCPLock(new LOCK(session, address.getBus1()),
				false, -1);
		locks.put(object, lock);
	}

	public void unregisterControlObject(String deviceGroup,
			LookupAddress address) {
		if (addressToControlObject.get(deviceGroup) == null)
			return;
		addressToControlObject.get(deviceGroup).remove(address);
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		if (session != null)
			session.getInfoChannel().addLOCKInfoListener(this);
	}

	public boolean acquireLock(String deviceGroup, LookupAddress address)
			throws LockingException {
		logger.info("acquireLock( " + deviceGroup + " , " + address + " )");
		if (addressToControlObject.get(deviceGroup) == null)
			throw new LockingException("Object to lock not found");
		if (addressToControlObject.get(deviceGroup).get(address) == null)
			throw new LockingException("Object to lock not found");

		Object obj = addressToControlObject.get(deviceGroup).get(address);
		SRCPLock sLock = locks.get(obj);
		LOCK lock = sLock.getLock();
		try {
			lock.set(deviceGroup, address.getAddress1(), Preferences
					.getInstance().getIntValue(PreferencesKeys.LOCK_DURATION));
			sLock.setLocked(true);
			sLock.setSessionID(session.getCommandChannelID());
		} catch (SRCPDeviceLockedException e) {
			throw new LockingException(ERR_LOCKED, e);
		} catch (SRCPException e) {
			throw new LockingException(ERR_FAILED, e);
		}
		return true;
	}

	public boolean releaseLock(String deviceGroup, LookupAddress address)
			throws LockingException {

		logger.info("acquireLock( " + deviceGroup + " , " + address + " )");
		if (addressToControlObject.get(deviceGroup) == null)
			throw new LockingException("Object to lock not found");
		if (addressToControlObject.get(deviceGroup).get(address) == null)
			throw new LockingException("Object to lock not found");

		Object obj = addressToControlObject.get(deviceGroup).get(address);

		SRCPLock sLock = locks.get(obj);
		LOCK lock = sLock.getLock();
		try {
			lock.term(deviceGroup, address.getAddress1());
			sLock.setLocked(false);
			sLock.setSessionID(-1);
			System.out.println(">>>" + false);
		} catch (SRCPDeviceLockedException e) {
			throw new LockingException(ERR_LOCKED, e);
		} catch (SRCPException e) {
			throw new LockingException(ERR_FAILED, e);
		}
		return true;
	}

	public void releaseAllLocks() throws LockingException {

	}

	public void LOCKset(double timestamp, int bus, int address,
			String deviceGroup, int duration, int sessionID) {
		logger.debug("LOCKset( " + bus + " , " + address + " , " + deviceGroup
				+ " , " + duration + " , " + sessionID + " )");
		LookupAddress addr = new LookupAddress(bus, address);
		Object object = addressToControlObject.get(deviceGroup).get(addr);
		if (object != null) {
			SRCPLock sLock = locks.get(object);
			sLock.setLocked(true);
			sLock.setSessionID(sessionID);
			informListeners(object);
		}
	}

	public void LOCKterm(double timestamp, int bus, int address,
			String deviceGroup) {
		logger.debug("LOCKset( " + bus + " , " + address + " , " + deviceGroup
				+ " )");
		LookupAddress addr = new LookupAddress(bus, address);
		Object object = addressToControlObject.get(deviceGroup).get(addr);
		if (object != null) {
			SRCPLock sLock = locks.get(object);
			sLock.setLocked(false);
			sLock.setSessionID(-1);
			informListeners(object);
		}
	}

	private void informListeners(Object object) {
		for (LockChangeListener l : listeners) {
			l.lockChanged(object);
		}
	}

	public void addLockChangeListener(LockChangeListener l) {
		listeners.add(l);
	}

	public void removeAllLockChangeListener() {
		listeners.clear();
	}

	public boolean isLocked(String deviceGroup, LookupAddress lookupAddress) {
		Object object = addressToControlObject.get(deviceGroup).get(
				lookupAddress);

		if (object != null) {
			SRCPLock sLock = locks.get(object);
			return sLock.isLocked();
		} else {
			return false;
		}
	}

	public int getLockingSessionID(String deviceGroup,
			LookupAddress lookupAddress) {
		Object object = addressToControlObject.get(deviceGroup).get(
				lookupAddress);
		if (object != null) {
			SRCPLock sLock = locks.get(object);
			return sLock.getSessionID();
		} else {
			return -1;
		}
	}
}
