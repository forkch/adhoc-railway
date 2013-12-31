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

import java.util.List;

import com.google.common.collect.Lists;

import ch.fork.AdHocRailway.controllers.impl.brain.BrainRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;

public abstract class RouteController {

	private final List<RouteChangeListener> listeners = Lists.newArrayList();

	public abstract void enableRoute(final Route r) throws RouteException;

	public abstract void disableRoute(final Route r) throws RouteException;

	public abstract void toggle(final Route route) throws RouteException;

	public abstract void toggleTest(final Route route) throws RouteException;

	public void addRouteChangeListener(final Route route,
			final RouteChangeListener listener) {

		listeners.add(listener);
	}

	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	public void removeRouteChangeListener(final Route route,
			final RouteChangeListener listener) {
		listeners.remove(listener);
	}

	public void informNextTurnoutDerouted(final Route route) {
		for (final RouteChangeListener listener : listeners) {
			listener.nextTurnoutDerouted(route);
		}
	}

	public void informNextTurnoutRouted(final Route route) {
		for (final RouteChangeListener listener : listeners) {
			listener.nextTurnoutRouted(route);
		}
	}

	public void informRouteChanged(final Route route) {
		for (final RouteChangeListener listener : listeners) {
			listener.routeChanged(route);
		}
	}

	public static RouteController createLocomotiveController(
			final RailwayDevice railwayDevice,
			final TurnoutController turnoutController) {
		if (railwayDevice == null) {
			return new NullRouteController();
		}
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainRouteControlAdapter(turnoutController);
		case SRCP:
			return new SRCPRouteControlAdapter(
					(SRCPTurnoutControlAdapter) turnoutController);
		default:
			return new NullRouteController();

		}
	}

	public abstract void setRoutingDelay(final int intValue);

	static class NullRouteController extends RouteController {

		@Override
		public void enableRoute(final Route r) throws RouteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void disableRoute(final Route r) throws RouteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void toggle(final Route route) throws RouteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void toggleTest(final Route route) throws RouteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setRoutingDelay(final int intValue) {
			// TODO Auto-generated method stub

		}

	}
}