/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotivePersistenceIface.java 199 2012-01-14 23:46:24Z fork_ch $
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

package ch.fork.AdHocRailway.services.locomotives;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public interface LocomotiveService {
	public abstract SortedSet<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException;

	public abstract void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException;

	public abstract void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract SortedSet<LocomotiveType> getAllLocomotiveTypes()
			throws LocomotivePersistenceException;

	public abstract void addLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException;

	public abstract void clear() throws LocomotivePersistenceException;

	public abstract void flush();

}