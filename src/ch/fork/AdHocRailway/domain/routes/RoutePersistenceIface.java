/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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

package ch.fork.AdHocRailway.domain.routes;

import com.jgoodies.binding.list.ArrayListModel;

public interface RoutePersistenceIface {

	public abstract ArrayListModel<Route> getAllRoutes()
			throws RoutePersistenceException;

	public abstract Route getRouteByNumber(int number)
			throws RoutePersistenceException;

	public abstract void addRoute(Route route) throws RoutePersistenceException;

	public abstract void deleteRoute(Route route)
			throws RoutePersistenceException;

	public abstract void updateRoute(Route route)
			throws RoutePersistenceException;

	public abstract ArrayListModel<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException;

	public abstract void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void updateRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void addRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void updateRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract int getNextFreeRouteNumber()
			throws RoutePersistenceException;

	public int getNextFreeRouteNumberOfGroup(RouteGroup routeGroup);
	
	public void enlargeRouteGroups();
	

	public abstract void clear() throws RoutePersistenceException;
	public abstract void flush();

}