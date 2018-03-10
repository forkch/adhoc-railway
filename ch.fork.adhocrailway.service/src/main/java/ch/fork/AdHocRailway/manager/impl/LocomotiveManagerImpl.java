/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryLocomotivePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.manager.impl;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.LocomotiveManagerListener;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class LocomotiveManagerImpl implements LocomotiveManager,
        LocomotiveServiceListener {
    private static final Logger LOGGER = Logger
            .getLogger(LocomotiveManager.class);

    private final SortedSet<Locomotive> allLocomotivesCache = new TreeSet<Locomotive>();
    private final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
    private final Set<LocomotiveManagerListener> listeners = new HashSet<LocomotiveManagerListener>();
    private final Set<LocomotiveManagerListener> listenersToBeRemovedInNextEvent = new HashSet<LocomotiveManagerListener>();
    private final Map<Integer, Locomotive> numberToLocomotiveMap = new HashMap<Integer, Locomotive>();
    private LocomotiveService locomotiveService;

    public LocomotiveManagerImpl(LocomotiveService locomotiveService) {
        this.locomotiveService = locomotiveService;
        LOGGER.info("LocomotiveManager loaded");
    }

    @Override
    public void addLocomotiveGroup(final LocomotiveGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        locomotiveService.addLocomotiveGroup(group);
    }

    @Override
    public void removeLocomotiveGroup(final LocomotiveGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        if (StringUtils.isBlank(group.getId())) {
            throw new ManagerException(
                    "Cannot delete ALL_LOCOMOTIVES_GROUP");
        }
        if (!group.getLocomotives().isEmpty()) {
            throw new ManagerException(
                    "Cannot delete locomotive group with associated locomotives");
        }
        locomotiveService.removeLocomotiveGroup(group);
    }

    @Override
    public void updateLocomotiveGroup(final LocomotiveGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        locomotiveService.updateLocomotiveGroup(group);
    }

    @Override
    public void addLocomotiveToGroup(final Locomotive locomotive,
                                     final LocomotiveGroup group) {
        group.addLocomotive(locomotive);
        locomotive.setGroup(group);
        locomotive.setGroupId(group.getId());
        locomotiveService.addLocomotive(locomotive);
    }

    @Override
    public void removeLocomotiveFromGroup(final Locomotive locomotive,
                                          final LocomotiveGroup group) {
        Preconditions.checkNotNull(locomotive);
        Preconditions.checkNotNull(group);
        if (!group.getLocomotives().contains(locomotive)) {
            throw new IllegalArgumentException("locomotive " + locomotive
                    + " does not belong to group " + group);
        }
        locomotiveService.removeLocomotive(locomotive);
        group.removeLocomotive(locomotive);
    }

    @Override
    public void updateLocomotive(final Locomotive locomotive) {
        if (locomotive == null) {
            throw new IllegalArgumentException("locomotive must not be null");
        }
        locomotiveService.updateLocomotive(locomotive);

    }

    @Override
    public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
        return locomotiveGroups;
    }

    @Override
    public SortedSet<Locomotive> getAllLocomotives() {
        return allLocomotivesCache;
    }

    @Override
    public void setActiveLocomotive(final int locomotiveNumber,
                                    final Locomotive locomotive) {
        numberToLocomotiveMap.put(locomotiveNumber, locomotive);
    }

    @Override
    public Locomotive getActiveLocomotive(final int locomotiveNumber) {
        return numberToLocomotiveMap.get(locomotiveNumber);
    }

    @Override
    public void setLocomotiveService(final LocomotiveService instance) {
        this.locomotiveService = instance;
    }

    @Override
    public LocomotiveService getService() {
        return locomotiveService;
    }

    @Override
    public void addLocomotiveGroups(SortedSet<LocomotiveGroup> groups) {
        locomotiveService.addLocomotiveGroups(groups);
    }

    @Override
    public void initialize() {
        clearCache();
        cleanupListeners();
        locomotiveService.init(this);
    }

    @Override
    public void addLocomotiveManagerListener(
            final LocomotiveManagerListener listener) {
        this.listeners.add(listener);
        listener.locomotivesUpdated(getAllLocomotiveGroups());
    }

    @Override
    public void removeLocomotiveManagerListenerInNextEvent(
            final LocomotiveManagerListener listener) {
        listenersToBeRemovedInNextEvent.add(listener);
    }

    @Override
    public void clear() {
        clearCache();
        locomotivesUpdated(getAllLocomotiveGroups());
    }

    @Override
    public void clearToService() {
        LOGGER.debug("clearToService()");
        clear();
        locomotiveService.clear();
    }

    @Override
    public void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> updatedLocomotiveGroups) {
        LOGGER.debug("locomotivesUpdated: " + updatedLocomotiveGroups);
        cleanupListeners();
        clearCache();
        for (final LocomotiveGroup group : updatedLocomotiveGroups) {
            putLocomotiveGroupInCache(group);
            for (final Locomotive locomotive : group.getLocomotives()) {
                putInCache(locomotive);
            }
        }
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotivesUpdated(updatedLocomotiveGroups);
        }

    }

    @Override
    public void locomotiveAdded(final Locomotive locomotive) {
        LOGGER.info("locomotiveAdded: " + locomotive);
        cleanupListeners();

        for (LocomotiveGroup locomotiveGroup : locomotiveGroups) {
            if(StringUtils.equals(locomotive.getGroupId(), locomotiveGroup.getId())) {
                locomotiveGroup.addLocomotive(locomotive);
                locomotive.setGroup(locomotiveGroup);
            }
        }

        putInCache(locomotive);
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotiveAdded(locomotive);
        }
    }

    @Override
    public void locomotiveUpdated(final Locomotive locomotive) {
        LOGGER.info("locomotiveUpdated: " + locomotive);
        cleanupListeners();

        for (LocomotiveGroup locomotiveGroup : locomotiveGroups) {
            if(StringUtils.equals(locomotive.getGroupId(), locomotiveGroup.getId())) {
                locomotiveGroup.addLocomotive(locomotive);
                locomotive.setGroup(locomotiveGroup);
            }
        }

        putInCache(locomotive);
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotiveUpdated(locomotive);
        }
    }

    @Override
    public void locomotiveRemoved(final Locomotive locomotive) {
        LOGGER.info("locomotiveRemoved: " + locomotive);
        cleanupListeners();
        for (final LocomotiveGroup locomotiveGroup : locomotiveGroups) {
            if(StringUtils.equals(locomotive.getGroupId(), locomotiveGroup.getId())) {
                locomotiveGroup.removeLocomotive(locomotive);
            }
        }
        removeFromCache(locomotive);
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotiveRemoved(locomotive);
        }
    }

    @Override
    public void locomotiveGroupAdded(final LocomotiveGroup group) {
        LOGGER.info("locomotiveGroupAdded: " + group);
        cleanupListeners();
        putLocomotiveGroupInCache(group);
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotiveGroupAdded(group);
        }
    }

    @Override
    public void locomotiveGroupUpdated(final LocomotiveGroup group) {
        LOGGER.info("locomotiveGroupUpdated: " + group);
        cleanupListeners();
        removeTurnoutGroupFromCache(group);
        putLocomotiveGroupInCache(group);
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotiveGroupUpdated(group);
        }
    }

    @Override
    public void locomotiveGroupRemoved(final LocomotiveGroup group) {
        LOGGER.info("locomotiveGroupRemoved: " + group);
        cleanupListeners();
        removeTurnoutGroupFromCache(group);
        for (final LocomotiveManagerListener l : listeners) {
            l.locomotiveGroupRemoved(group);
        }
    }

    @Override
    public void failure(
            final AdHocServiceException serviceException) {
        LOGGER.warn("failure", serviceException);
        cleanupListeners();
        for (final LocomotiveManagerListener l : listeners) {
            l.failure(serviceException);
        }
    }

    @Override
    public void disconnect() {
        cleanupListeners();
        if (locomotiveService != null) {
            locomotiveService.disconnect();
        }
    }

    private void cleanupListeners() {
        listeners.removeAll(listenersToBeRemovedInNextEvent);
        listenersToBeRemovedInNextEvent.clear();
    }

    private void putLocomotiveGroupInCache(final LocomotiveGroup group) {
        locomotiveGroups.add(group);
    }

    private void removeTurnoutGroupFromCache(final LocomotiveGroup group) {
        locomotiveGroups.remove(group);
    }

    private void putInCache(final Locomotive locomotive) {
        allLocomotivesCache.add(locomotive);
    }

    private void removeFromCache(final Locomotive locomotive) {
        allLocomotivesCache.remove(locomotive);
    }

    private void clearCache() {
        allLocomotivesCache.clear();
        locomotiveGroups.clear();
        numberToLocomotiveMap.clear();
    }

}
