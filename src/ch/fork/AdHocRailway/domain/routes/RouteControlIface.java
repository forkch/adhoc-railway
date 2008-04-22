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

import ch.fork.AdHocRailway.domain.ControlException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;

public interface RouteControlIface {

	public abstract void enableRoute(Route r) throws TurnoutException;

	public abstract void disableRoute(Route r) throws TurnoutException;

	public abstract void addRouteChangeListener(Route r,
			RouteChangeListener listener);

	public abstract void removeAllRouteChangeListeners();

	public abstract void removeRouteChangeListener(Route r,
			RouteChangeListener listener);

	public abstract void undoLastChange() throws ControlException;

	public abstract void previousDeviceToDefault() throws ControlException;

	public abstract void setRoutePersistence(
			RoutePersistenceIface routePersistence);

	public abstract SRCPRouteState getRouteState(Route route);

	public abstract boolean isRouting(Route route);
	
	public abstract void update();

}