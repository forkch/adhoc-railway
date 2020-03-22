/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPLockControl.java,v 1.5 2011-12-18 09:15:44 andre_schenk Exp $
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

package de.dermoba.srcp.model.locking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dermoba.srcp.model.locomotives.SRCPLocomotive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.LOCK;
import de.dermoba.srcp.devices.listener.LOCKInfoListener;
import de.dermoba.srcp.model.Constants;
import de.dermoba.srcp.model.SRCPAddress;

public class SRCPLockControl implements LOCKInfoListener, Constants {
	private static Logger logger = LoggerFactory.getLogger(SRCPLockControl.class);
	private static SRCPLockControl instance = null;
	private SRCPSession session;

	Map<String, Map<SRCPAddress, Object>> addressToControlObject;
	Map<Object, SRCPLock> locks;

	private final List<SRCPLockChangeListener> listeners;
	private int lockDuration = Constants.DEFAULT_LOCK_DURATION;

	private SRCPLockControl() {
		addressToControlObject = new HashMap<String, Map<SRCPAddress, Object>>();
		locks = new HashMap<Object, SRCPLock>();
		listeners = new ArrayList<SRCPLockChangeListener>();
	}

	public static SRCPLockControl getInstance() {
		if (instance == null) {
			instance = new SRCPLockControl();
		}
		return instance;
	}

	public void registerControlObject(final String deviceGroup,
			final SRCPAddress address, final Object object) {
		if (addressToControlObject.get(deviceGroup) == null) {
			addressToControlObject.put(deviceGroup,
					new HashMap<SRCPAddress, Object>());
		}

		if (addressToControlObject.get(deviceGroup).get(address) != null) {
			// got it already
			return;
		}
		addressToControlObject.get(deviceGroup).put(address, object);
		final SRCPLock lock = new SRCPLock(
				new LOCK(session, address.getBus1()), false, -1);
		locks.put(object, lock);
	}

	public void unregisterControlObject(final String deviceGroup,
			final SRCPAddress address) {
		if (addressToControlObject.get(deviceGroup) == null) {
			return;
		}
		addressToControlObject.get(deviceGroup).remove(address);
	}

	public void setSession(final SRCPSession session) {
		this.session = session;
		if (session != null) {
			session.getInfoChannel().addLOCKInfoListener(this);
		}
    }

	public boolean acquireLock(final String deviceGroup,
			final SRCPAddress address) throws SRCPLockingException,
			SRCPDeviceLockedException {
		logger.info("acquireLock( " + deviceGroup + " , " + address + " )");
		if (addressToControlObject.get(deviceGroup) == null) {
			throw new SRCPLockingException("Object to lock not found");
		}
		if (addressToControlObject.get(deviceGroup).get(address) == null) {
			throw new SRCPLockingException("Object to lock not found");
		}

		final Object obj = addressToControlObject.get(deviceGroup).get(address);
		final SRCPLock sLock = locks.get(obj);
		final LOCK lock = sLock.getLock();
		try {
			lock.set(deviceGroup, address.getAddress1(), lockDuration);
		} catch (final SRCPException e) {
			throw new SRCPLockingException(ERR_FAILED, e);
		}
		return true;
	}

	public boolean releaseLock(final String deviceGroup,
			final SRCPAddress address) throws SRCPLockingException,
			SRCPDeviceLockedException {

		logger.info("releaseLock( " + deviceGroup + " , " + address + " )");
		if (addressToControlObject.get(deviceGroup) == null) {
			throw new SRCPLockingException("Object to unlock not found");
		}
		if (addressToControlObject.get(deviceGroup).get(address) == null) {
			throw new SRCPLockingException("Object to unlock not found");
		}

		final Object obj = addressToControlObject.get(deviceGroup).get(address);

		final SRCPLock sLock = locks.get(obj);
		final LOCK lock = sLock.getLock();
		try {
			lock.term(deviceGroup, address.getAddress1());
		} catch (final SRCPException e) {
			throw new SRCPLockingException(ERR_FAILED, e);
		}
		return true;
	}

	public void releaseAllLocks() throws SRCPLockingException {

	}

	public void LOCKset(final double timestamp, final int bus,
			final int address, final String deviceGroup, final int duration,
			final int sessionID) {
		logger.debug("LOCKset( " + bus + " , " + address + " , " + deviceGroup
				+ " , " + duration + " , " + sessionID + " )");
		final SRCPAddress addr = new SRCPAddress(bus, address);

		if (addressToControlObject.get(deviceGroup) != null) {
			final Object object = addressToControlObject.get(deviceGroup).get(
					addr);
			if (object != null) {
				final SRCPLock sLock = locks.get(object);
				sLock.setLocked(true);
				sLock.setSessionID(sessionID);
				informListeners(object, true);
			}
		}
	}

	public void LOCKterm(final double timestamp, final int bus,
			final int address, final String deviceGroup) {
		logger.debug("LOCKterm( " + bus + " , " + address + " , " + deviceGroup
				+ " )");
		final SRCPAddress addr = new SRCPAddress(bus, address);
		if (addressToControlObject.get(deviceGroup) != null) {
			final Object object = addressToControlObject.get(deviceGroup).get(
					addr);
			if (object != null) {
				final SRCPLock sLock = locks.get(object);
				sLock.setLocked(false);
				sLock.setSessionID(-1);
				informListeners(object, false);
			}
		}
	}

	private void informListeners(final Object object, final boolean locked) {
		for (final SRCPLockChangeListener l : listeners) {
			l.lockChanged(object, locked);
		}
		logger.debug("lockChanged");
	}

	public void addLockChangeListener(final SRCPLockChangeListener l) {
		listeners.add(l);
	}

	public void removeLockChangeListener(final SRCPLockChangeListener l) {
		listeners.remove(l);
	}

	public void removeAllLockChangeListener() {
		listeners.clear();
	}

	public boolean isLocked(final String deviceGroup,
			final SRCPAddress lookupAddress) {

		final Map<SRCPAddress, Object> deviceGroupLocks = addressToControlObject
				.get(deviceGroup);
		if (deviceGroupLocks == null) {
			return false;
		}
		final Object object = deviceGroupLocks.get(lookupAddress);

		if (object != null) {
			final SRCPLock sLock = locks.get(object);
			return sLock.isLocked();
		} else {
			return false;
		}
	}

	public int getLockingSessionID(final String deviceGroup,
			final SRCPAddress lookupAddress) {

		final Object object = addressToControlObject.get(deviceGroup).get(
				lookupAddress);
		if (object != null) {
			final SRCPLock sLock = locks.get(object);
			return sLock.getSessionID();
		} else {
			return -1;
		}
	}

	public void setLockDuration(final int lockDuration) {
		this.lockDuration = lockDuration;
	}
}
