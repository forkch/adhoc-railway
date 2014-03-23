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

package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;

import java.util.SortedSet;

public interface RouteService {

    public abstract void addRoute(Route route) ;

    public abstract void removeRoute(Route route) ;

    public abstract void updateRoute(Route route) ;

    public abstract SortedSet<RouteGroup> getAllRouteGroups()
            ;

    public abstract void addRouteGroup(RouteGroup routeGroup)
            ;

    public abstract void removeRouteGroup(RouteGroup routeGroup)
            ;

    public abstract void updateRouteGroup(RouteGroup routeGroup)
            ;

    public abstract void addRouteItem(RouteItem item)
            ;

    public abstract void removeRouteItem(RouteItem item)
            ;

    public abstract void updateRouteItem(RouteItem item)
            ;

    public abstract void clear() ;

    void init(RouteServiceListener listener);

    void disconnect();

}