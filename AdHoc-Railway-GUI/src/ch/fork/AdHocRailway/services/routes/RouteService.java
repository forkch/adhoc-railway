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

package ch.fork.AdHocRailway.services.routes;

import java.util.List;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;

public interface RouteService {

	public abstract List<Route> getAllRoutes() throws RoutePersistenceException;

	public abstract void addRoute(Route route) throws RoutePersistenceException;

	public abstract void deleteRoute(Route route)
			throws RoutePersistenceException;

	public abstract void updateRoute(Route route)
			throws RoutePersistenceException;

	public abstract List<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException;

	public abstract void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void updateRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract List<RouteItem> getAllRouteItems()
			throws RoutePersistenceException;

	public abstract void addRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void updateRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void clear() throws RoutePersistenceException;

	public abstract void flush();

}