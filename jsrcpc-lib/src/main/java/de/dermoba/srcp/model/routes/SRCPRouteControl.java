/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPRouteControl.java,v 1.2 2008-04-24 18:37:38 fork_ch Exp $
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

package de.dermoba.srcp.model.routes;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.Constants;
import de.dermoba.srcp.model.NoSessionException;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SRCPRouteControl {
	private static final Logger LOGGER = Logger.getLogger(SRCPRouteControl.class);
	private static SRCPRouteControl instance;

	private final List<SRCPRouteChangeListener> listeners;

	private SRCPRouteState lastRouteState;

	private SRCPRoute lastChangedRoute;

	protected String ERR_TOGGLE_FAILED = "Toggle of switch failed";

	protected SRCPSession session;
	private int routingDelay = Constants.DEFAULT_ROUTING_DELAY;

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(final SRCPSession session) {
		this.session = session;
	}

	private SRCPRouteControl() {
		LOGGER.info("SRCPRouteControl loaded");
		listeners = new ArrayList<SRCPRouteChangeListener>();
	}

	public static SRCPRouteControl getInstance() {
		if (instance == null) {
			instance = new SRCPRouteControl();
		}
		return instance;
	}

	public void toggle(final SRCPRoute route) throws SRCPTurnoutException,
			SRCPRouteException {

        if(route.getRouteState() == SRCPRouteState.ROUTING) {
            LOGGER.warn("route is currently routing therefore ignoring this state change!");
            return;
        }
		if (route.getRouteState().equals(SRCPRouteState.ENABLED)) {
			disableRoute(route);
		} else {
			enableRoute(route);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RouteControlIface#enableRoute(ch.fork
	 * .AdHocRailway.domain.routes.Route)
	 */
	public void enableRoute(final SRCPRoute route) throws SRCPTurnoutException,
			SRCPRouteException {

        if(route.getRouteState() == SRCPRouteState.ROUTING) {
            LOGGER.warn("route is currently routing therefore ignoring this state change!");
            return;
        }
		checkRoute(route);
		LOGGER.debug("enabling route: " + route);

		final SRCPRouter switchRouter = new SRCPRouter(route, true,
				routingDelay, listeners);
		switchRouter.start();
		lastChangedRoute = route;
		lastRouteState = SRCPRouteState.ENABLED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RouteControlIface#disableRoute(ch.
	 * fork.AdHocRailway.domain.routes.Route)
	 */
	public void disableRoute(final SRCPRoute route)
			throws SRCPTurnoutException, SRCPRouteException {

        if(route.getRouteState() == SRCPRouteState.ROUTING) {
            LOGGER.warn("route is currently routing therefore ignoring this state change!");
            return;
        }
		checkRoute(route);
		LOGGER.debug("disabling route: " + route);

		final SRCPRouter switchRouter = new SRCPRouter(route, false,
				routingDelay, listeners);
		switchRouter.start();
		lastChangedRoute = route;
		lastRouteState = SRCPRouteState.DISABLED;
	}

	private void checkRoute(final SRCPRoute r) throws SRCPRouteException {
		if (session == null) {
			throw new SRCPRouteException(Constants.ERR_NOT_CONNECTED,
					new NoSessionException());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RouteControlIface#addRouteChangeListener
	 * (ch.fork.AdHocRailway.domain.routes.Route,
	 * ch.fork.AdHocRailway.domain.routes.RouteChangeListener)
	 */
	public void addRouteChangeListener(final SRCPRouteChangeListener listener) {

		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#
	 * removeAllRouteChangeListeners()
	 */
	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#
	 * removeRouteChangeListener(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void removeRouteChangeListener(final SRCPRouteChangeListener listener) {
		listeners.remove(listener);
	}

	public void undoLastChange() throws SRCPRouteException {
		if (lastChangedRoute == null) {
			return;
		}
		try {
			switch (lastRouteState) {
			case ENABLED:
				disableRoute(lastChangedRoute);
				break;
			case DISABLED:
				enableRoute(lastChangedRoute);
				break;
			case ROUTING:
				break;
			case UNDEF:
				break;
			default:
				break;
			}
			lastChangedRoute = null;
			lastRouteState = null;
		} catch (final SRCPTurnoutException e) {
			throw new SRCPRouteException(e);
		}
	}

	public void previousDeviceToDefault() throws SRCPRouteException {
		if (lastChangedRoute == null) {
			return;
		}
		try {
			disableRoute(lastChangedRoute);
		} catch (final SRCPTurnoutException e) {
			throw new SRCPRouteException(e);
		}
		lastChangedRoute = null;
		lastRouteState = null;
	}

	public void setRoutingDelay(int routingDelay) {
		if (routingDelay < 150) {
			routingDelay = 150;
		}
		this.routingDelay = routingDelay;
	}

}
