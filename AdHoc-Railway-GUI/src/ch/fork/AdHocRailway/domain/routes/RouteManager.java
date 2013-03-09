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

import java.util.List;

public interface RouteManager {

	public abstract Route getRouteByNumber(int number)
			throws RouteManagerException;

	public abstract void addRoute(Route route) throws RouteManagerException;

	public abstract void removeRoute(Route route) throws RouteManagerException;

	public abstract void updateRoute(Route route) throws RouteManagerException;

	public abstract List<RouteGroup> getAllRouteGroups()
			throws RouteManagerException;

	public abstract void addRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException;

	public abstract void deleteRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException;

	public abstract void updateRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException;

	public abstract void addRouteItem(RouteItem item)
			throws RouteManagerException;

	public abstract void removeRouteItem(RouteItem item)
			throws RouteManagerException;

	public abstract void updateRouteItem(RouteItem item)
			throws RouteManagerException;

	public abstract int getNextFreeRouteNumber() throws RouteManagerException;

	public abstract int getNextFreeRouteNumberOfGroup(RouteGroup routeGroup);

	public abstract void enlargeRouteGroups();

	public abstract void clear() throws RouteManagerException;

	public abstract void initialize();

	public abstract void setRouteControl(RouteControlIface routeControl);

	void addRouteManagerListener(RouteManagerListener listener);

	void removeRouteManagerListenerInNextEvent(
			RouteManagerListener turnoutAddListener);

}