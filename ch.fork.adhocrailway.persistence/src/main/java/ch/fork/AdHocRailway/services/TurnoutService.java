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

package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;

import java.util.SortedSet;

public interface TurnoutService {

    void init(TurnoutServiceListener listener);

    void addTurnout(Turnout turnout);

    void removeTurnout(Turnout turnout);

    void updateTurnout(Turnout turnout);

    SortedSet<TurnoutGroup> getAllTurnoutGroups();

    void addTurnoutGroup(TurnoutGroup group);

    void removeTurnoutGroup(TurnoutGroup group);

    void updateTurnoutGroup(TurnoutGroup group);

    void clear();

    void disconnect();
}