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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.routes.Route.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class SRCPRouteControl implements RouteControlIface {
	private static Logger							logger				= Logger
																				.getLogger(SRCPRouteControl.class);
	private static RouteControlIface				instance;

	private RoutePersistenceIface					persistence;

	private Map<Route, Set<RouteChangeListener>>	listeners;

	private RouteState								lastRouteState;

	private Route									lastChangedRoute;

	private Map<Route, SRCPRoute>					srcpRoutes;

	protected String								ERR_TOGGLE_FAILED	= "Toggle of switch failed";

	private SRCPRouteControl() {
		logger.info("SRCPRouteControl loaded");
		listeners = new HashMap<Route, Set<RouteChangeListener>>();
		srcpRoutes = new HashMap<Route, SRCPRoute>();
	}

	public static RouteControlIface getInstance() {
		if (instance == null) {
			instance = new SRCPRouteControl();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#enableRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void enableRoute(Route r) throws TurnoutException {
		checkRoute(r);
		SRCPRoute sRoute = srcpRoutes.get(r);
		logger.debug("enabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, sRoute, true, waitTime, listeners
				.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.ENABLED;
	}

	private void checkRoute(Route r) {
		if (srcpRoutes.get(r) == null) {
			srcpRoutes.put(r, new SRCPRoute(r));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#disableRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void disableRoute(Route r) throws TurnoutException {
		checkRoute(r);
		SRCPRoute sRoute = srcpRoutes.get(r);
		logger.debug("disabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, sRoute, false, waitTime, listeners
				.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.DISABLED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#addRouteChangeListener(ch.fork.AdHocRailway.domain.routes.Route,
	 *      ch.fork.AdHocRailway.domain.routes.RouteChangeListener)
	 */
	public void addRouteChangeListener(Route r, RouteChangeListener listener) {
		if (listeners.get(r) == null)
			listeners.put(r, new HashSet<RouteChangeListener>());
		listeners.get(r).add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#removeAllRouteChangeListeners()
	 */
	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#removeRouteChangeListener(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void removeRouteChangeListener(Route r, RouteChangeListener listener) {
		listeners.get(r).remove(listener);
	}

	public void undoLastChange() throws RouteException {
		if (lastChangedRoute == null)
			return;
		try {
			switch (lastRouteState) {
			case ENABLED:

				disableRoute(lastChangedRoute);

				break;
			case DISABLED:
				enableRoute(lastChangedRoute);
				break;
			}
			lastChangedRoute = null;
			lastRouteState = null;
		} catch (TurnoutException e) {
			throw new RouteException(e);
		}
	}

	public void previousDeviceToDefault() throws RouteException {
		if (lastChangedRoute == null)
			return;
		try {
			disableRoute(lastChangedRoute);
		} catch (TurnoutException e) {
			throw new RouteException(e);
		}
		lastChangedRoute = null;
		lastRouteState = null;
	}

	public void setRoutePersistence(RoutePersistenceIface routePersistence) {
		this.persistence = routePersistence;

	}

	public RouteState getRouteState(Route route) {
		checkRoute(route);
		SRCPRoute sRoute = srcpRoutes.get(route);
		return sRoute.getRouteState();
	}

	public boolean isRouting(Route route) {
		checkRoute(route);
		SRCPRoute sRoute = srcpRoutes.get(route);
		return sRoute.isRouting();
	}

}
