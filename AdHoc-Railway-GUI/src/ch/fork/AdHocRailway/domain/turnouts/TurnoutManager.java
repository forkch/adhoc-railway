/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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
import java.util.Set;

public interface TurnoutManager {

	public abstract List<Turnout> getAllTurnouts();

	public abstract Turnout getTurnoutByNumber(int number);

	public abstract Turnout getTurnoutByAddressBus(int bus, int address);

	public abstract void addTurnout(Turnout turnout);

	public abstract void removeTurnout(Turnout turnout);

	public abstract void updateTurnout(Turnout turnout);

	public abstract List<TurnoutGroup> getAllTurnoutGroups();

	public abstract TurnoutGroup getTurnoutGroupByName(String name);

	public abstract void addTurnoutGroup(TurnoutGroup group);

	public abstract void removeTurnoutGroup(TurnoutGroup group);

	public abstract void updateTurnoutGroup(TurnoutGroup group);

	public int getNextFreeTurnoutNumber();

	public Set<Integer> getUsedTurnoutNumbers();

	public abstract void clear();

	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup);

	public void enlargeTurnoutGroups();

	public abstract void initialize();

	void setTurnoutControl(TurnoutControlIface turnoutControl);

	public abstract void addTurnoutManagerListener(
			TurnoutManagerListener listener);

	public abstract void removeTurnoutManagerListenerInNextEvent(
			TurnoutManagerListener turnoutAddListener);
}