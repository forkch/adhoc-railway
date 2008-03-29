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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.LookupAddress;

import com.jgoodies.binding.list.ArrayListModel;

public class FileLocomotivePersistence extends CachingLocomotivePersistence {
	private static FileLocomotivePersistence	instance;

	private static Logger						logger	= Logger
																.getLogger(LocomotivePersistenceIface.class);


	private Map<String, LocomotiveType>			locomotiveTypes;

	private FileLocomotivePersistence() {
		logger.info("FileLocomotivePersistence loaded");

		if (getLocomotiveTypeByName("DELTA") == null) {
			LocomotiveType deltaType = new LocomotiveType(0, "DELTA");
			deltaType.setDrivingSteps(14);
			deltaType.setStepping(4);
			deltaType.setFunctionCount(4);
			addLocomotiveType(deltaType);
		}

		if (getLocomotiveTypeByName("DIGITAL") == null) {
			LocomotiveType digitalType = new LocomotiveType(0, "DIGITAL");
			digitalType.setDrivingSteps(28);
			digitalType.setStepping(2);
			digitalType.setFunctionCount(5);
			addLocomotiveType(digitalType);
		}

	}

	public static FileLocomotivePersistence getInstance() {
		if (instance == null) {
			instance = new FileLocomotivePersistence();
		}
		return instance;
	}
}
