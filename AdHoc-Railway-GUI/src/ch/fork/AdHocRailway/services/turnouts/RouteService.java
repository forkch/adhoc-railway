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

package ch.fork.AdHocRailway.services.turnouts;

import java.util.List;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;

public interface RouteService {

	public abstract void addRoute(Route route) throws RouteManagerException;

	public abstract void removeRoute(Route route) throws RouteManagerException;

	public abstract void updateRoute(Route route) throws RouteManagerException;

	public abstract List<RouteGroup> getAllRouteGroups()
			throws RouteManagerException;

	public abstract void addRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException;

	public abstract void removeRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException;

	public abstract void updateRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException;

	public abstract void addRouteItem(RouteItem item)
			throws RouteManagerException;

	public abstract void removeRouteItem(RouteItem item)
			throws RouteManagerException;

	public abstract void updateRouteItem(RouteItem item)
			throws RouteManagerException;

	public abstract void clear() throws RouteManagerException;

	void init(RouteServiceListener listener);

	void disconnect();

}