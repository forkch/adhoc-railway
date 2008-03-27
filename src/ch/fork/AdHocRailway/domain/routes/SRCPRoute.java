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

import ch.fork.AdHocRailway.domain.routes.Route.RouteState;

public class SRCPRoute {

	private Route		route;

	private RouteState	routeState;

	private boolean		routing;

	public SRCPRoute(Route route) {
		this.route = route;
	}

	public RouteState getRouteState() {
		return routeState;
	}

	protected void setRouteState(RouteState routeState) {
		this.routeState = routeState;
	}

	public void setChangingRoute(boolean changingRoute) {
		this.routing = changingRoute;
	}

	public boolean isRouting() {
		return routing;
	}
}
