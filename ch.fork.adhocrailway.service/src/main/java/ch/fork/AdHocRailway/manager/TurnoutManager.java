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

package ch.fork.AdHocRailway.manager;

import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.TurnoutService;

import java.util.List;
import java.util.SortedSet;

public interface TurnoutManager {

    void initialize();

    void addTurnoutManagerListener(final TurnoutManagerListener listener);

    void removeTurnoutManagerListenerInNextEvent(final TurnoutManagerListener turnoutAddListener);

    List<Turnout> getAllTurnouts();

    Turnout getTurnoutByNumber(final int number);

    void addTurnoutToGroup(final Turnout turnout, final TurnoutGroup group);

    void removeTurnout(final Turnout turnout);

    void updateTurnout(int numberOfTrnoutBeforePotentialAlteration, final Turnout turnout);

    SortedSet<TurnoutGroup> getAllTurnoutGroups();

    TurnoutGroup getTurnoutGroupByName(final String name);

    void addTurnoutGroup(final TurnoutGroup group);

    void removeTurnoutGroup(final TurnoutGroup group);

    void updateTurnoutGroup(final TurnoutGroup group);

    int getLastProgrammedAddress();

    int getNextFreeTurnoutNumber();

    boolean isTurnoutNumberFree(final int number);

    @Deprecated
    void setTurnoutService(final TurnoutService instance);

    void clear();

    void clearToService();

    void disconnect();

    TurnoutService getService();

    Turnout getTurnoutById(String turnoutId);

    void setupLinkedRoutes(Route route);
}