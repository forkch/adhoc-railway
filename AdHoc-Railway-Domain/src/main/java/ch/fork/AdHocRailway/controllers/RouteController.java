/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteControlIface.java 279 2013-04-02 20:46:41Z fork_ch $
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

package ch.fork.AdHocRailway.controllers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.controllers.impl.RouteChangingThread;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;

import com.google.common.collect.Maps;

public abstract class RouteController {

	private final Map<Route, List<RouteChangeListener>> listeners = Maps
			.newHashMap();

	public abstract void enableRoute(final Route r) throws RouteException;

	public abstract void disableRoute(final Route r) throws RouteException;

	public void toggle(final Route route) throws RouteException {
		if (route.isEnabled()) {
			disableRoute(route);
		} else {
			enableRoute(route);
		}
	}

	public void toggleTest(final Route route) throws RouteException {
		toggle(route);
	}

	;

	public void addRouteChangeListener(final Route route,
			final RouteChangeListener listener) {

		List<RouteChangeListener> routeChangeListeners = listeners.get(route);
		if (routeChangeListeners == null) {
			routeChangeListeners = new LinkedList<>();
			listeners.put(route, routeChangeListeners);
		}
		routeChangeListeners.add(listener);
	}

	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	public void removeRouteChangeListener(final Route route,
			final RouteChangeListener listener) {
		final List<RouteChangeListener> listenersForRoute = listeners
				.get(route);
		if (listenersForRoute != null) {
			listenersForRoute.remove(listener);
		}
	}

	public void informNextTurnoutDerouted(final Route route) {
		final List<RouteChangeListener> routeChangeListeners = listeners
				.get(route);
		if (routeChangeListeners != null) {
			for (final RouteChangeListener listener : routeChangeListeners) {
				listener.nextTurnoutDerouted(route);
			}
		}
	}

	public void informNextTurnoutRouted(final Route route) {
		final List<RouteChangeListener> routeChangeListeners = listeners
				.get(route);
		if (routeChangeListeners != null) {
			for (final RouteChangeListener listener : listeners.get(route)) {
				listener.nextTurnoutRouted(route);
			}
		}
	}

	public void informRouteChanged(final Route route) {
		final List<RouteChangeListener> routeChangeListeners = listeners
				.get(route);
		if (routeChangeListeners != null) {
			for (final RouteChangeListener listener : listeners.get(route)) {
				listener.routeChanged(route);
			}
		}
	}

	public static RouteController createLocomotiveController(
			final RailwayDevice railwayDevice,
			final TurnoutController turnoutController) {
		if (railwayDevice == null) {
			return new NullRouteController(turnoutController);
		}
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainRouteControlAdapter(turnoutController);
		case SRCP:
			return new SRCPRouteControlAdapter(
					(SRCPTurnoutControlAdapter) turnoutController);
		default:
			return new NullRouteController(turnoutController);

		}
	}

	public abstract void setRoutingDelay(final int intValue);

	static class NullRouteController extends RouteController {

		private final RouteChangingThread.RouteChangingListener routeChangingListener;
		private final TurnoutController turnoutControl;
		private int routingDelay;

		public NullRouteController(final TurnoutController turnoutControl) {
			this.turnoutControl = turnoutControl;
			routeChangingListener = new RouteChangingThread.RouteChangingListener() {
				@Override
				public void informNextTurnoutRouted(final Route route) {
					NullRouteController.this.informNextTurnoutRouted(route);
				}

				@Override
				public void informNextTurnoutDerouted(final Route route) {
					NullRouteController.this.informNextTurnoutDerouted(route);

				}

				@Override
				public void informRouteChanged(final Route route) {
					NullRouteController.this.informRouteChanged(route);
				}
			};
		}

		@Override
		public void enableRoute(final Route r) throws RouteException {
			final Thread brainRouterThread = new Thread(
					new RouteChangingThread(turnoutControl, r, true,
							routingDelay, routeChangingListener));
			brainRouterThread.start();
		}

		@Override
		public void disableRoute(final Route r) throws RouteException {

			final Thread brainRouterThread = new Thread(
					new RouteChangingThread(turnoutControl, r, false,
							routingDelay, routeChangingListener));
			brainRouterThread.start();
		}

		@Override
		public void setRoutingDelay(final int routingDelay) {

			this.routingDelay = routingDelay;
		}

	}
}