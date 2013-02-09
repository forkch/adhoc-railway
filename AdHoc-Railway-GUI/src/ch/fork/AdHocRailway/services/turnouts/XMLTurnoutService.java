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

package ch.fork.AdHocRailway.services.turnouts;

import java.util.List;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

public class XMLTurnoutService implements TurnoutService {
	static Logger logger = Logger.getLogger(XMLTurnoutService.class);
	private static XMLTurnoutService instance;

	private XMLTurnoutService() {
		logger.info("FileTurnoutPersistence loaded");

	}

	public static XMLTurnoutService getInstance() {
		if (instance == null) {
			instance = new XMLTurnoutService();
		}
		return instance;
	}

	@Override
	public void addTurnout(Turnout turnout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteTurnout(Turnout turnout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTurnout(Turnout turnout) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTurnoutGroup(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteTurnoutGroup(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTurnoutGroup(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(TurnoutServiceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

}
