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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;

import com.jgoodies.binding.list.ArrayListModel;

public interface LocomotiveManager {
	public abstract ArrayListModel<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException;

	public abstract void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract Locomotive getLocomotiveByBusAddress(int bus, int address)
			throws LocomotivePersistenceException;

	public abstract void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException;

	public LocomotiveGroup getLocomotiveGroupById(int id);

	public abstract SortedSet<LocomotiveType> getAllLocomotiveTypes()
			throws LocomotivePersistenceException;

	public LocomotiveType getLocomotiveTypeById(int id);

	public abstract LocomotiveType getLocomotiveTypeByName(String typeName)
			throws LocomotivePersistenceException;

	public abstract void addLocomotiveType(LocomotiveType defaultType)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException;

	public abstract void clear() throws LocomotivePersistenceException;

	public abstract void flush();

	public abstract void reload();

}