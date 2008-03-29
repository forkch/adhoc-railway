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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

import com.jgoodies.binding.list.ArrayListModel;

public class FileTurnoutPersistence extends CachingTurnoutPersistence{
	static Logger							logger	= Logger
															.getLogger(FileTurnoutPersistence.class);
	private static FileTurnoutPersistence	instance;


	private FileTurnoutPersistence() {
		super.clear();
		logger.info("FileTurnoutPersistence loaded");

		if (getTurnoutType(TurnoutTypes.DEFAULT) == null) {
			TurnoutType defaultType = new TurnoutType(0, "DEFAULT");
			addTurnoutType(defaultType);
		}
		if (getTurnoutType(TurnoutTypes.DOUBLECROSS) == null) {
			TurnoutType doublecrossType = new TurnoutType(0, "DOUBLECROSS");
			addTurnoutType(doublecrossType);
		}
		if (getTurnoutType(TurnoutTypes.THREEWAY) == null) {
			TurnoutType threewayType = new TurnoutType(0, "THREEWAY");
			addTurnoutType(threewayType);
		}
	}

	public static FileTurnoutPersistence getInstance() {
		if (instance == null) {
			instance = new FileTurnoutPersistence();
		}
		return instance;
	}
}
