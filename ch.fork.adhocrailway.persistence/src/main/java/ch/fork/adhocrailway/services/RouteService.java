/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RoutePersistenceIface.java 199 2012-01-14 23:46:24Z fork_ch $
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

package ch.fork.adhocrailway.services;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
import ch.fork.adhocrailway.model.turnouts.RouteItem;

import java.util.SortedSet;

public interface RouteService {

    void addRoute(Route route);

    void removeRoute(Route route);

    void updateRoute(Route route);

    SortedSet<RouteGroup> getAllRouteGroups();

    void addRouteGroup(RouteGroup routeGroup);

    void removeRouteGroup(RouteGroup routeGroup);

    void updateRouteGroup(RouteGroup routeGroup);

    void addRouteItem(RouteItem item);

    void removeRouteItem(RouteItem item);

    void updateRouteItem(RouteItem item);

    void clear();

    void init(RouteServiceListener listener);

    void disconnect();

}
