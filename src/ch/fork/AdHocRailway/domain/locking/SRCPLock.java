/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

import de.dermoba.srcp.devices.LOCK;

public class SRCPLock {

	private LOCK	lock;
	private boolean	locked;
	private int		sessionID;

	public SRCPLock(LOCK lock, boolean locked, int sessionID) {
		super();
		this.lock = lock;
		this.locked = locked;
		this.sessionID = sessionID;
	}

	public void setLock(LOCK lock) {
		this.lock = lock;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public LOCK getLock() {
		return lock;
	}

	public boolean isLocked() {
		return locked;
	}

	public int getSessionID() {
		return sessionID;
	}

}
