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

package ch.fork.AdHocRailway.services.impl.xml;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import org.apache.log4j.Logger;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class XMLTurnoutService implements TurnoutService {
    private static final Logger LOGGER = Logger
            .getLogger(XMLTurnoutService.class);
    private final SortedSet<Turnout> turnouts = new TreeSet<Turnout>();
    private final SortedSet<TurnoutGroup> turnoutGroups = new TreeSet<TurnoutGroup>();
    private TurnoutServiceListener listener;

    public XMLTurnoutService() {
        LOGGER.info("FileTurnoutPersistence loaded");

    }

    @Override
    public void addTurnout(final Turnout turnout) {
        turnouts.add(turnout);
        turnout.setId(XMLServiceHelper.nextValue());
        listener.turnoutAdded(turnout);
    }

    @Override
    public void removeTurnout(final Turnout turnout) {
        turnouts.remove(turnout);
        listener.turnoutRemoved(turnout);

    }

    @Override
    public void updateTurnout(final Turnout turnout) {
        turnouts.remove(turnout);
        turnouts.add(turnout);
        listener.turnoutUpdated(turnout);

    }

    @Override
    public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
        return turnoutGroups;
    }

    @Override
    public void addTurnoutGroup(final TurnoutGroup group) {
        turnoutGroups.add(group);
        group.setId(XMLServiceHelper.nextValue());
        listener.turnoutGroupAdded(group);
    }

    @Override
    public void removeTurnoutGroup(final TurnoutGroup group) {
        turnoutGroups.remove(group);
        listener.turnoutGroupRemoved(group);

    }

    @Override
    public void updateTurnoutGroup(final TurnoutGroup group) {
        turnoutGroups.remove(group);
        turnoutGroups.add(group);
        listener.turnoutGroupUpdated(group);

    }

    @Override
    public void clear() {
        turnouts.clear();
        turnoutGroups.clear();
    }

    @Override
    public void init(final TurnoutServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public void disconnect() {

    }

    public void loadTurnoutGroupsFromXML(final SortedSet<TurnoutGroup> groups) {
        turnoutGroups.clear();
        turnouts.clear();
        if (groups != null) {
            for (final TurnoutGroup turnoutGroup : groups) {
                turnoutGroup.init();
                turnoutGroup.setId(UUID.randomUUID().toString());
                turnoutGroups.add(turnoutGroup);
                for (final Turnout turnout : turnoutGroup.getTurnouts()) {
                    turnout.init();
                    turnout.setId(UUID.randomUUID().toString());
                    turnouts.add(turnout);
                    turnout.setTurnoutGroup(turnoutGroup);
                }
            }
        }
        listener.turnoutsUpdated(turnoutGroups);
    }
}
