/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryRoutePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.domain.routes;

import org.apache.log4j.Logger;

public class FileRoutePersistence extends CachingRoutePersistence {
	private static Logger					logger	= Logger
															.getLogger(FileRoutePersistence.class);
	private static FileRoutePersistence	instance;

	private FileRoutePersistence() {
		logger.info("FileRoutePersistence loaded");
	}

	public static FileRoutePersistence getInstance() {
		if (instance == null) {
			instance = new FileRoutePersistence();
		}
		return instance;
	}
}
