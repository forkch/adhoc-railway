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


public interface LockControlIface<E> {

	public boolean isLocked(E object) throws LockingException;

	public boolean isLockedByMe(E object) throws LockingException;

	public boolean acquireLock(E object) throws LockingException;

	public boolean releaseLock(E object) throws LockingException;

}