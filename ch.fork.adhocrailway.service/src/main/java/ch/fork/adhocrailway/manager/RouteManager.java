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

package ch.fork.adhocrailway.manager;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
import ch.fork.adhocrailway.model.turnouts.RouteItem;
import ch.fork.adhocrailway.services.RouteService;

import java.util.List;
import java.util.SortedSet;

public interface RouteManager {

    Route getRouteByNumber(final int number);

    void addRouteToGroup(final Route route, final RouteGroup selectedRouteGroup);

    void removeRoute(final Route route);

    void updateRoute(final Route route);

    SortedSet<RouteGroup> getAllRouteGroups();

    void addRouteGroup(final RouteGroup routeGroup);

    void removeRouteGroup(final RouteGroup routeGroup);

    void updateRouteGroup(final RouteGroup routeGroup);

    void addRouteItemToGroup(final RouteItem item, Route route);

    void removeRouteItem(final RouteItem item);

    void updateRouteItem(final RouteItem item);

    int getNextFreeRouteNumber();

    boolean isRouteNumberFree(final int number);

    void clear();

    void clearToService();

    void initialize();

    void addRouteManagerListener(final RouteManagerListener listener);

    void removeRouteManagerListenerInNextEvent(
            final RouteManagerListener turnoutAddListener);

    @Deprecated
    void setRouteService(final RouteService instance);

    void disconnect();

    RouteService getService();

    List<Route> getAllRoutes();

}
