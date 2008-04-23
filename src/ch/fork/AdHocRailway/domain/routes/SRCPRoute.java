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

import java.util.ArrayList;
import java.util.List;

public class SRCPRoute {

	private SRCPRouteState		routeState;

	private boolean				routing;

	private List<SRCPRouteItem>	routeItems;

	public SRCPRoute() {
		routeItems = new ArrayList<SRCPRouteItem>();
	}

	public SRCPRouteState getRouteState() {
		return routeState;
	}

	protected void setRouteState(SRCPRouteState routeState) {
		this.routeState = routeState;
	}

	public void setChangingRoute(boolean changingRoute) {
		this.routing = changingRoute;
	}

	public boolean isRouting() {
		return routing;
	}

	public List<SRCPRouteItem> getRouteItems() {
		return routeItems;
	}

	public void setRouteItems(List<SRCPRouteItem> routeItems) {
		this.routeItems = routeItems;
	}
	
	public void addRouteItem(SRCPRouteItem item) {
		this.routeItems.add(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((routeItems == null) ? 0 : routeItems.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SRCPRoute other = (SRCPRoute) obj;
		if (routeItems == null) {
			if (other.routeItems != null)
				return false;
		} else if (!routeItems.equals(other.routeItems))
			return false;
		return true;
	}
}
