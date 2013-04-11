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

import java.util.SortedSet;

import ch.fork.AdHocRailway.services.turnouts.RouteService;

public interface RouteManager {

	public abstract Route getRouteByNumber(int number);

	public abstract void addRouteToGroup(Route route,
			RouteGroup selectedRouteGroup);

	public abstract void removeRoute(Route route);

	public abstract void updateRoute(Route route);

	public abstract SortedSet<RouteGroup> getAllRouteGroups();

	public abstract void addRouteGroup(RouteGroup routeGroup);

	public abstract void removeRouteGroup(RouteGroup routeGroup);

	public abstract void updateRouteGroup(RouteGroup routeGroup);

	public abstract void addRouteItem(RouteItem item);

	public abstract void removeRouteItem(RouteItem item);

	public abstract void updateRouteItem(RouteItem item);

	public abstract int getNextFreeRouteNumber();

	public abstract int getNextFreeRouteNumberOfGroup(RouteGroup routeGroup);

	public abstract void enlargeRouteGroups();

	public abstract void clear();

	public abstract void clearToService();

	public abstract void initialize();

	public abstract void setRouteControl(RouteControlIface routeControl);

	void addRouteManagerListener(RouteManagerListener listener);

	void removeRouteManagerListenerInNextEvent(
			RouteManagerListener turnoutAddListener);

	public abstract void setRouteService(RouteService instance);

	public abstract void disconnect();

	public abstract RouteService getService();

}