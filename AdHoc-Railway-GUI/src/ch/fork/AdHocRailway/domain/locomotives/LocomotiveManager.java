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

import java.util.List;

public interface LocomotiveManager {
	public abstract List<Locomotive> getAllLocomotives()
			throws LocomotiveManagerException;

	public abstract void addLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException;

	public abstract void deleteLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException;

	public abstract void updateLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException;

	public abstract Locomotive getLocomotiveByBusAddress(int bus, int address)
			throws LocomotiveManagerException;

	public abstract void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException;

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException;

	public abstract void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException;

	public abstract List<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotiveManagerException;

	public abstract void clear() throws LocomotiveManagerException;

	public abstract void initialize();

	public abstract void setLocomotiveControl(LocomotiveControlface locomotiveControl);

}