/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteManager.java 302 2013-04-16 20:31:37Z fork_ch $
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

package ch.fork.AdHocRailway.manager.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.SortedSet;

public interface RouteManager {

    public abstract Route getRouteByNumber(final int number);

    public abstract void addRouteToGroup(final Route route,
                                         final RouteGroup selectedRouteGroup);

    public abstract void removeRoute(final Route route);

    public abstract void updateRoute(final Route route);

    public abstract SortedSet<RouteGroup> getAllRouteGroups();

    public abstract void addRouteGroup(final RouteGroup routeGroup);

    public abstract void removeRouteGroup(final RouteGroup routeGroup);

    public abstract void updateRouteGroup(final RouteGroup routeGroup);

    public abstract void addRouteItem(final RouteItem item);

    public abstract void removeRouteItem(final RouteItem item);

    public abstract void updateRouteItem(final RouteItem item);

    public abstract int getNextFreeRouteNumber();

    public abstract boolean isRouteNumberFree(final int number);

    public abstract void clear();

    public abstract void clearToService();

    public abstract void initialize(final EventBus eventBus);

    void addRouteManagerListener(final RouteManagerListener listener);

    void removeRouteManagerListenerInNextEvent(
            final RouteManagerListener turnoutAddListener);

    public abstract void setRouteService(final RouteService instance);

    public abstract void disconnect();

    public abstract RouteService getService();

    public abstract List<Route> getAllRoutes();

}