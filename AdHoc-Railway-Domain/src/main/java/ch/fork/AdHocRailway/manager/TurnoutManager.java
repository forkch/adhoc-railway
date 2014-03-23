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

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.TurnoutService;
import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.SortedSet;

public interface TurnoutManager {

    public abstract void initialize(final EventBus eventBus);

    public abstract void addTurnoutManagerListener(
            final TurnoutManagerListener listener);

    public abstract void removeTurnoutManagerListenerInNextEvent(
            final TurnoutManagerListener turnoutAddListener);

    public abstract List<Turnout> getAllTurnouts();

    public abstract Turnout getTurnoutByNumber(final int number);

    public abstract void addTurnoutToGroup(final Turnout turnout,
                                           final TurnoutGroup group);

    public abstract void removeTurnout(final Turnout turnout);

    public abstract void updateTurnout(final Turnout turnout);

    public abstract SortedSet<TurnoutGroup> getAllTurnoutGroups();

    public abstract TurnoutGroup getTurnoutGroupByName(final String name);

    public abstract void addTurnoutGroup(final TurnoutGroup group);

    public abstract void removeTurnoutGroup(final TurnoutGroup group);

    public abstract void updateTurnoutGroup(final TurnoutGroup group);

    public int getLastProgrammedAddress();

    public int getNextFreeTurnoutNumber();

    public boolean isTurnoutNumberFree(final int number);

    public abstract void setTurnoutService(final TurnoutService instance);

    public abstract void clear();

    public abstract void clearToService();

    public abstract void disconnect();

    public abstract TurnoutService getService();
}