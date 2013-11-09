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

package ch.fork.AdHocRailway.manager.locomotives;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveService;

import java.util.SortedSet;

public interface LocomotiveManager {

	public abstract void initialize();

	void addLocomotiveManagerListener(LocomotiveManagerListener listener);

	void removeLocomotiveManagerListenerInNextEvent(
			LocomotiveManagerListener listener);

	public abstract SortedSet<Locomotive> getAllLocomotives()
			throws LocomotiveManagerException;

	public abstract SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotiveManagerException;

	public abstract void addLocomotiveToGroup(Locomotive locomotive,
			LocomotiveGroup group);

	public abstract void removeLocomotiveFromGroup(Locomotive locomotive,
			LocomotiveGroup group);

	public abstract void updateLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException;

	public abstract void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException;

	public abstract void removeLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException;

	public abstract void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException;

	public abstract void setLocomotiveControl(
			LocomotiveController locomotiveControl);

	public abstract void setLocomotiveService(LocomotiveService instance);

	public abstract void clear();

	public abstract void clearToService();

	public abstract void disconnect();

	public abstract void setActiveLocomotive(int locomotiveNumber,
			Locomotive locomotive);

	public abstract Locomotive getActiveLocomotive(int locomotiveNumber);

	public abstract LocomotiveService getService();

}