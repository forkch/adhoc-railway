/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutPersistenceIface.java 199 2012-01-14 23:46:24Z fork_ch $
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
import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public interface TurnoutService {

	public abstract List<Turnout> getAllTurnouts();

	public abstract void addTurnout(Turnout turnout);

	public abstract void deleteTurnout(Turnout turnout);

	public abstract void updateTurnout(Turnout turnout);

	public abstract List<TurnoutGroup> getAllTurnoutGroups();

	public abstract void addTurnoutGroup(TurnoutGroup group);

	public abstract void deleteTurnoutGroup(TurnoutGroup group);

	public abstract void updateTurnoutGroup(TurnoutGroup group);

	public abstract SortedSet<TurnoutType> getAllTurnoutTypes();

	public abstract void addTurnoutType(TurnoutType type);

	public abstract void deleteTurnoutType(TurnoutType type);

	public abstract void clear();

	public abstract void flush();

}