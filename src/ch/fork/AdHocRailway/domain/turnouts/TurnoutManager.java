/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutManager.java 302 2013-04-16 20:31:37Z fork_ch $
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

import java.util.List;
import java.util.SortedSet;

import ch.fork.AdHocRailway.services.turnouts.TurnoutService;

public interface TurnoutManager {

	public abstract void initialize();

	public abstract void addTurnoutManagerListener(
			TurnoutManagerListener listener);

	public abstract void removeTurnoutManagerListenerInNextEvent(
			TurnoutManagerListener turnoutAddListener);

	public abstract List<Turnout> getAllTurnouts();

	public abstract Turnout getTurnoutByNumber(int number);

	public abstract Turnout getTurnoutByAddressBus(int bus, int address);

	public abstract void addTurnoutToGroup(Turnout turnout, TurnoutGroup group);

	public abstract void removeTurnout(Turnout turnout);

	public abstract void updateTurnout(Turnout turnout);

	public abstract SortedSet<TurnoutGroup> getAllTurnoutGroups();

	public abstract TurnoutGroup getTurnoutGroupByName(String name);

	public abstract void addTurnoutGroup(TurnoutGroup group);

	public abstract void removeTurnoutGroup(TurnoutGroup group);

	public abstract void updateTurnoutGroup(TurnoutGroup group);

	public int getLastProgrammedAddress();

	public int getNextFreeTurnoutNumber();

	public boolean isTurnoutNumberFree(int number);

	public void setTurnoutControl(TurnoutControlIface turnoutControl);

	public abstract void setTurnoutService(TurnoutService instance);

	public abstract void clear();

	public abstract void clearToService();

	public abstract void disconnect();

	public abstract TurnoutService getService();
}