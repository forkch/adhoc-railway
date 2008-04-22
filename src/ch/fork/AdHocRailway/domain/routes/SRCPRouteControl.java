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

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.NoSessionException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;

public class SRCPRouteControl {
	private static Logger					logger				= Logger
																		.getLogger(SRCPRouteControl.class);
	private static SRCPRouteControl			instance;

	private List<SRCPRouteChangeListener>	listeners;

	private SRCPRouteState					lastRouteState;

	private SRCPRoute						lastChangedRoute;

	protected String						ERR_TOGGLE_FAILED	= "Toggle of switch failed";

	protected SRCPSession					session;

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
	}

	private SRCPRouteControl() {
		logger.info("SRCPRouteControl loaded");
		listeners = new ArrayList<SRCPRouteChangeListener>();
	}

	public static SRCPRouteControl getInstance() {
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
	public void enableRoute(SRCPRoute route) throws TurnoutException {
		checkRoute(route);
		logger.debug("enabling route: " + route);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(route, true, waitTime, listeners);
		switchRouter.start();
		lastChangedRoute = route;
		lastRouteState = SRCPRouteState.ENABLED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#disableRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void disableRoute(SRCPRoute route) throws TurnoutException {
		checkRoute(route);
		logger.debug("disabling route: " + route);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(route, false, waitTime, listeners);
		switchRouter.start();
		lastChangedRoute = route;
		lastRouteState = SRCPRouteState.DISABLED;
	}

	private void checkRoute(SRCPRoute r) {
		if (session == null)
			throw new RouteException(Constants.ERR_NOT_CONNECTED,
					new NoSessionException());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#addRouteChangeListener(ch.fork.AdHocRailway.domain.routes.Route,
	 *      ch.fork.AdHocRailway.domain.routes.RouteChangeListener)
	 */
	public void addRouteChangeListener(SRCPRouteChangeListener listener) {
		
		listeners.add(listener);
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
	public void removeRouteChangeListener(SRCPRouteChangeListener listener) {
		listeners.remove(listener);
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

}
