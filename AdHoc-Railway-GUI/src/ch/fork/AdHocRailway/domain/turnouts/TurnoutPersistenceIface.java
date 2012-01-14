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

import java.util.Set;
import java.util.SortedSet;

import com.jgoodies.binding.list.ArrayListModel;

import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public interface TurnoutPersistenceIface {

	public abstract ArrayListModel<Turnout> getAllTurnouts();

	public abstract Turnout getTurnoutByNumber(int number);

	public abstract Turnout getTurnoutByAddressBus(int bus, int address);

	public abstract void addTurnout(Turnout turnout);

	public abstract void deleteTurnout(Turnout turnout);

	public abstract void updateTurnout(Turnout turnout);

	public abstract ArrayListModel<TurnoutGroup> getAllTurnoutGroups();

	public abstract TurnoutGroup getTurnoutGroupByName(String name);

	public abstract void addTurnoutGroup(TurnoutGroup group);

	public abstract void deleteTurnoutGroup(TurnoutGroup group);

	public abstract void updateTurnoutGroup(TurnoutGroup group);

	public abstract SortedSet<TurnoutType> getAllTurnoutTypes();

	public abstract TurnoutType getTurnoutType(SRCPTurnoutTypes typeName);

	public abstract void addTurnoutType(TurnoutType type);

	public abstract void deleteTurnoutType(TurnoutType type);

	public int getNextFreeTurnoutNumber();

	public Set<Integer> getUsedTurnoutNumbers();

	public abstract void clear();

	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup);
	
	public void enlargeTurnoutGroups();
	
	public abstract void flush();
	
	public abstract void reload();
}