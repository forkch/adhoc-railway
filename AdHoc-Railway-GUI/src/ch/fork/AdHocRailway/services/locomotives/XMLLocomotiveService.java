/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryLocomotivePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class XMLLocomotiveService implements LocomotiveService {
	private static XMLLocomotiveService instance;

	private static Logger logger = Logger.getLogger(LocomotiveManager.class);

	private XMLLocomotiveService() {
		logger.info("FileLocomotivePersistence loaded");

		// addDefaultLocomotiveTypes();

	}

	// private void addDefaultLocomotiveTypes() {
	// if (getLocomotiveTypeByName("DELTA") == null) {
	// LocomotiveType deltaType = new LocomotiveType(0, "DELTA");
	// deltaType.setDrivingSteps(14);
	// deltaType.setStepping(2);
	// deltaType.setFunctionCount(0);
	// addLocomotiveType(deltaType);
	// }
	//
	// if (getLocomotiveTypeByName("DIGITAL") == null) {
	// LocomotiveType digitalType = new LocomotiveType(0, "DIGITAL");
	// digitalType.setDrivingSteps(14);
	// digitalType.setStepping(2);
	// digitalType.setFunctionCount(5);
	// addLocomotiveType(digitalType);
	// }
	// }

	@Override
	public void clear() {
		// addDefaultLocomotiveTypes();
	}

	public static XMLLocomotiveService getInstance() {
		if (instance == null) {
			instance = new XMLLocomotiveService();
		}
		return instance;
	}

	@Override
	public SortedSet<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public SortedSet<LocomotiveType> getAllLocomotiveTypes()
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}
}
