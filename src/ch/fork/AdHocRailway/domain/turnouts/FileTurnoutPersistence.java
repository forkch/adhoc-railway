/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryTurnoutPersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.domain.turnouts;

import org.apache.log4j.Logger;

import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class FileTurnoutPersistence extends CachingTurnoutPersistence{
	static Logger							logger	= Logger
															.getLogger(FileTurnoutPersistence.class);
	private static FileTurnoutPersistence	instance;


	private FileTurnoutPersistence() {
		super.clear();
		logger.info("FileTurnoutPersistence loaded");
		
		addDefaultTurnoutTypes();
	}

	private void addDefaultTurnoutTypes() {
		if (getTurnoutType(SRCPTurnoutTypes.DEFAULT) == null) {
			TurnoutType defaultType = new TurnoutType(0, "DEFAULT");
			addTurnoutType(defaultType);
		}
		if (getTurnoutType(SRCPTurnoutTypes.DOUBLECROSS) == null) {
			TurnoutType doublecrossType = new TurnoutType(0, "DOUBLECROSS");
			addTurnoutType(doublecrossType);
		}
		if (getTurnoutType(SRCPTurnoutTypes.THREEWAY) == null) {
			TurnoutType threewayType = new TurnoutType(0, "THREEWAY");
			addTurnoutType(threewayType);
		}
	}
	
	public void clear() {
		super.clear();
		addDefaultTurnoutTypes();
	}

	public static FileTurnoutPersistence getInstance() {
		if (instance == null) {
			instance = new FileTurnoutPersistence();
		}
		return instance;
	}

	public void reload() {
		// TODO Auto-generated method stub
		
	}
}
