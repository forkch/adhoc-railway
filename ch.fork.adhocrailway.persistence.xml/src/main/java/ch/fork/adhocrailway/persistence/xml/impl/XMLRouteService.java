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

package ch.fork.adhocrailway.persistence.xml.impl;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
import ch.fork.adhocrailway.model.turnouts.RouteItem;
import ch.fork.adhocrailway.services.RouteService;
import ch.fork.adhocrailway.services.RouteServiceListener;

public class XMLRouteService implements RouteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLRouteService.class);
    private final SortedSet<Route> routes = new TreeSet<Route>();
    private final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();
    private RouteServiceListener listener;

    public XMLRouteService() {
        LOGGER.info("XMLRoutePersistence loaded");
    }

    @Override
    public void addRoute(final Route route) {
        routes.add(route);
        listener.routeAdded(route);
    }

    @Override
    public void removeRoute(final Route route) {
        routes.remove(route);
        listener.routeRemoved(route);
    }

    @Override
    public void updateRoute(final Route route) {
        routes.remove(route);
        routes.add(route);
        listener.routeUpdated(route);

    }

    @Override
    public SortedSet<RouteGroup> getAllRouteGroups() {
        return routeGroups;
    }

    @Override
    public void addRouteGroup(final RouteGroup routeGroup) {
        routeGroups.add(routeGroup);
        listener.routeGroupAdded(routeGroup);
    }

    @Override
    public void removeRouteGroup(final RouteGroup routeGroup) {
        routeGroups.remove(routeGroup);
        listener.routeGroupRemoved(routeGroup);

    }

    @Override
    public void updateRouteGroup(final RouteGroup routeGroup) {
        routeGroups.remove(routeGroup);
        routeGroups.add(routeGroup);
        listener.routeGroupUpdated(routeGroup);

    }

    @Override
    public void addRouteItem(final RouteItem item) {

    }

    @Override
    public void removeRouteItem(final RouteItem item) {

    }

    @Override
    public void updateRouteItem(final RouteItem item) {

    }

    @Override
    public void clear() {
        routes.clear();
        routeGroups.clear();
    }

    @Override
    public void init(final RouteServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public void disconnect() {

    }

    public void loadRouteGroupsFromXML(final SortedSet<RouteGroup> groups) {
        routeGroups.clear();
        routes.clear();
        if (groups != null) {
            for (final RouteGroup routeGroup : groups) {
                routeGroup.init();
                routeGroups.add(routeGroup);
                if (routeGroup.getRoutes() == null
                        || routeGroup.getRoutes().isEmpty()) {
                    routeGroup.setRoutes(new TreeSet<Route>());
                }
                for (final Route route : routeGroup.getRoutes()) {
                        route.init();
                    routes.add(route);
                    route.setRouteGroup(routeGroup);
                }
            }
        }
        listener.routesUpdated(routeGroups);
    }

}
