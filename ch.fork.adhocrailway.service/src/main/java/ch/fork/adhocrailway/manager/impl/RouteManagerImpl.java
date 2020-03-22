/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryRoutePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.adhocrailway.manager.impl;

import ch.fork.adhocrailway.manager.ManagerException;
import ch.fork.adhocrailway.manager.RouteManager;
import ch.fork.adhocrailway.manager.RouteManagerListener;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
import ch.fork.adhocrailway.model.turnouts.RouteItem;
import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.services.AdHocServiceException;
import ch.fork.adhocrailway.services.RouteService;
import ch.fork.adhocrailway.services.RouteServiceListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RouteManagerImpl implements RouteManager, RouteServiceListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteManagerImpl.class);
    private final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();
    private final Map<Integer, Route> numberToRouteCache = new HashMap<Integer, Route>();
    private final Set<RouteManagerListener> listeners = new HashSet<RouteManagerListener>();
    private final Set<RouteManagerListener> listenersToBeRemovedInNextEvent = new HashSet<RouteManagerListener>();
    private final TurnoutManager turnoutManager;
    private RouteService routeService;
    private int lastProgrammedNumber = 0;

    public RouteManagerImpl(final TurnoutManager turnoutManager, final RouteService routeService) {
        this.turnoutManager = turnoutManager;
        this.routeService = routeService;
        LOGGER.info("RouteMangerImpl loaded");
    }

    @Override
    public void addRouteManagerListener(final RouteManagerListener listener) {
        this.listeners.add(listener);
        listener.routesUpdated(getAllRouteGroups());
    }

    @Override
    public void removeRouteManagerListenerInNextEvent(
            final RouteManagerListener turnoutAddListener) {
        listenersToBeRemovedInNextEvent.add(turnoutAddListener);
    }

    private void cleanupListeners() {
        listeners.removeAll(listenersToBeRemovedInNextEvent);
        listenersToBeRemovedInNextEvent.clear();
    }

    @Override
    public void clear() {
        LOGGER.debug("clear()");
        clearCache();
        routesUpdated(getAllRouteGroups());
    }

    @Override
    public void clearToService() {
        LOGGER.debug("clearToService()");
        clear();
        routeService.clear();
    }

    @Override
    public List<Route> getAllRoutes() {
        LOGGER.debug("getAllRoutes()");
        return new ArrayList<Route>(numberToRouteCache.values());
    }

    @Override
    public Route getRouteByNumber(final int number) {
        LOGGER.debug("getRouteByNumber()");
        for (final Route route : getAllRoutes()) {
            if (route.getNumber() == number) {
                return route;
            }
        }
        return null;
    }

    @Override
    public void addRouteToGroup(final Route route, final RouteGroup group) {
        LOGGER.debug("addRouteToGroup()");

        group.addRoute(route);
        route.setRouteGroup(group);
        route.setGroupId(group.getId());
        this.routeService.addRoute(route);
        lastProgrammedNumber = route.getNumber();
    }

    @Override
    public void removeRoute(final Route route) {
        LOGGER.debug("removeRoute()");
        if (!route.getRoutedTurnouts().isEmpty()) {
            final SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(
                    route.getRoutedTurnouts());
            for (final RouteItem routeitem : routeItems) {
                removeRouteItem(routeitem);
            }
        }

        this.routeService.removeRoute(route);
        final RouteGroup group = route.getRouteGroup();
        group.getRoutes().remove(route);

        final Set<RouteItem> routeItems = route.getRoutedTurnouts();
        for (final RouteItem ri : routeItems) {
            route.getRoutedTurnouts().remove(ri);
        }
        removeFromCache(route);
    }

    @Override
    public void updateRoute(final Route route) {
        LOGGER.debug("updateRoute()");
        removeFromCache(route);
        this.routeService.updateRoute(route);
    }

    @Override
    public SortedSet<RouteGroup> getAllRouteGroups() {
        LOGGER.debug("getAllRouteGroups()");
        return routeGroups;
    }

    @Override
    public void addRouteGroup(final RouteGroup routeGroup) {
        LOGGER.debug("addRouteGroup()");
        this.routeService.addRouteGroup(routeGroup);
        routeGroups.add(routeGroup);
    }

    @Override
    public void removeRouteGroup(final RouteGroup routeGroup) {
        LOGGER.debug("deleteRouteGroup()");
        if (!routeGroup.getRoutes().isEmpty()) {
            throw new ManagerException(
                    "Cannot delete Route-Group with associated Routes");
        }
        this.routeService.removeRouteGroup(routeGroup);
        routeGroups.remove(routeGroup);
    }

    @Override
    public void updateRouteGroup(final RouteGroup routeGroup) {
        LOGGER.debug("updateRouteGroup()");
        this.routeService.updateRouteGroup(routeGroup);
    }

    @Override
    public void addRouteItemToGroup(final RouteItem item, Route route) {
        LOGGER.debug("addRouteItemToGroup()");

        if (item.getTurnout() == null) {
            throw new ManagerException(
                    "RouteItem has no associated Turnout");
        }
        if (item.getRoute() == null) {
            throw new ManagerException("RouteItem has no associated Route");
        }
        this.routeService.addRouteItem(item);
        item.getRoute().addRouteItem(item);
    }

    @Override
    public void removeRouteItem(final RouteItem item) {
        LOGGER.debug("removeRouteItem()");

        this.routeService.removeRouteItem(item);
        final Turnout turnout = item.getTurnout();
        turnout.getRouteItems().remove(item);

        final Route route = item.getRoute();
        route.removeRouteItem(item);
    }

    @Override
    public void updateRouteItem(final RouteItem item) {
       /* LOGGER.debug("updateRouteItem()");
        this.routeService.updateRouteItem(item);*/

    }

    @Override
    public int getNextFreeRouteNumber() {
        LOGGER.debug("getNextFreeRouteNumber()");
        if (lastProgrammedNumber == 0) {
            final SortedSet<Route> routesNumbers = new TreeSet<Route>(
                    new Comparator<Route>() {

                        @Override
                        public int compare(final Route o1, final Route o2) {
                            return Integer.valueOf(o1.getNumber()).compareTo(
                                    o2.getNumber());
                        }
                    }
            );
            routesNumbers.addAll(getAllRoutes());
            if (routesNumbers.isEmpty()) {
                lastProgrammedNumber = 0;
            } else {
                lastProgrammedNumber = routesNumbers.last().getNumber();
            }
        }
        return lastProgrammedNumber + 1;
    }

    @Override
    public boolean isRouteNumberFree(final int number) {
        return !numberToRouteCache.containsKey(number);
    }

    @Override
    public void setRouteService(final RouteService instance) {
        this.routeService = instance;
    }

    @Override
    public void initialize() {
        clearCache();
        cleanupListeners();

        routeService.init(this);
    }

    @Override
    public void routesUpdated(final SortedSet<RouteGroup> updatedRouteGroups) {
        LOGGER.debug("routesUpdated: " + updatedRouteGroups);
        cleanupListeners();
        clearCache();
        for (final RouteGroup group : updatedRouteGroups) {
            putRouteGroupInCache(group);
            for (final Route route : group.getRoutes()) {
                putInCache(route);
                populateTransientFields(route);
            }
        }
        for (final RouteManagerListener l : listeners) {
            l.routesUpdated(updatedRouteGroups);
        }

    }

    @Override
    public void routeAdded(final Route route) {
        LOGGER.info("routeAdded: " + route);
        cleanupListeners();
        for (RouteGroup routeGroup : routeGroups) {
            if(StringUtils.equals(route.getGroupId(), routeGroup.getId())) {
                routeGroup.addRoute(route);
                route.setRouteGroup(routeGroup);
            }
        }
        putInCache(route);
        populateTransientFields(route);
        for (final RouteManagerListener l : listeners) {
            l.routeAdded(route);
        }
    }

    @Override
    public void routeUpdated(final Route route) {
        LOGGER.info("routeUpdated: " + route);
        cleanupListeners();
        for (RouteGroup routeGroup : routeGroups) {
            if(StringUtils.equals(route.getGroupId(), routeGroup.getId())) {
                routeGroup.addRoute(route);
                route.setRouteGroup(routeGroup);
            }
        }
        putInCache(route);
        populateTransientFields(route);
        for (final RouteManagerListener l : listeners) {
            l.routeUpdated(route);
        }
    }

    private void populateTransientFields(Route route) {

        for (RouteItem routeItem : route.getRoutedTurnouts()) {
            Turnout turnoutByNumber = turnoutManager.getTurnoutByNumber(routeItem.getTurnoutNumber());
            if (turnoutByNumber != null) {
                routeItem.setTurnout(turnoutByNumber);
                turnoutByNumber.addRouteItem(routeItem);
                routeItem.setRoute(route);
            } else {
                LOGGER.warn("turnout with number " + routeItem.getTurnoutNumber() + " not found ");
            }
        }

        turnoutManager.setupLinkedRoutes(route);
        TreeSet<RouteItem> routeItems = new TreeSet<RouteItem>();
        routeItems.addAll(route.getRoutedTurnouts());
        route.setRoutedTurnouts(routeItems);
    }

    @Override
    public void routeRemoved(final Route route) {
        LOGGER.info("routeRemoved: " + route);
        cleanupListeners();
        for (RouteGroup routeGroup : routeGroups) {
            if(StringUtils.equals(route.getGroupId(), routeGroup.getId())) {
                routeGroup.removeRoute(route);
            }
        }
        removeFromCache(route);
        for (final RouteManagerListener l : listeners) {
            l.routeRemoved(route);
        }
    }

    @Override
    public void routeGroupAdded(final RouteGroup routeGroup) {
        LOGGER.info("routeGroupAdded: " + routeGroup);
        cleanupListeners();
        putRouteGroupInCache(routeGroup);
        for (final RouteManagerListener l : listeners) {
            l.routeGroupAdded(routeGroup);
        }
    }

    @Override
    public void routeGroupUpdated(final RouteGroup routeGroup) {
        LOGGER.info("routeGroupUpdated: " + routeGroup);
        cleanupListeners();
        removeRouteGroupInCache(routeGroup);
        putRouteGroupInCache(routeGroup);
        for (final RouteManagerListener l : listeners) {
            l.routeGroupUpdated(routeGroup);
        }
    }

    @Override
    public void routeGroupRemoved(final RouteGroup routeGroup) {
        LOGGER.info("routeGroupRemoved: " + routeGroup);
        cleanupListeners();
        removeRouteGroupInCache(routeGroup);
        for (final RouteManagerListener l : listeners) {
            l.routeGroupRemoved(routeGroup);
        }
    }

    @Override
    public void failure(final AdHocServiceException serviceException) {
        LOGGER.info("failure: " + serviceException);
        cleanupListeners();
        for (final RouteManagerListener l : listeners) {
            l.failure(serviceException);
        }
    }

    @Override
    public void disconnect() {
        cleanupListeners();
        if (routeService != null) {
            routeService.disconnect();
        }
    }


    private void putInCache(final Route route) {
        numberToRouteCache.put(route.getNumber(), route);

    }

    private void putRouteGroupInCache(final RouteGroup group) {
        routeGroups.add(group);
    }

    private void removeFromCache(final Route route) {
        numberToRouteCache.remove(route.getNumber());
    }

    private void removeRouteGroupInCache(final RouteGroup group) {
        routeGroups.remove(group);
    }

    private void clearCache() {
        routeGroups.clear();
        numberToRouteCache.clear();
    }

    @Override
    public RouteService getService() {
        return routeService;
    }
}
