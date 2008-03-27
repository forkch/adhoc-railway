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

import java.util.Set;
import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.routes.Route.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class Router extends Thread {

	private Route						route;
	private boolean						enableRoute;
	private int							waitTime;
	private Set<RouteChangeListener>	listener;
	private TurnoutException			switchException;
	private SRCPRoute					sRoute;

	public Router(Route route, SRCPRoute sRoute, boolean enableRoute,
			int waitTime, Set<RouteChangeListener> listener) {
		this.route = route;
		this.sRoute = sRoute;
		this.enableRoute = enableRoute;
		this.waitTime = waitTime;
		this.listener = listener;
	}

	public void run() {
		try {
			sRoute.setChangingRoute(true);
			if (enableRoute) {
				enableRoute();
			} else {
				disableRoute();
			}
			sRoute.setChangingRoute(false);
		} catch (TurnoutException e) {
			this.switchException = e;
			sRoute.setChangingRoute(false);
			ExceptionProcessor.getInstance().processException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void disableRoute() throws TurnoutException, InterruptedException {
		SortedSet<RouteItem> routeItems = route.getRouteItems();
		TurnoutControlIface sc = SRCPTurnoutControl.getInstance();
		for (RouteItem ri : routeItems) {
			Turnout turnoutToRoute = ri.getTurnout();

			sc.setDefaultState(turnoutToRoute);
			for (RouteChangeListener l : listener) {
				l.nextSwitchDerouted();
			}
			Thread.sleep(waitTime);
		}
		sRoute.setRouteState(RouteState.DISABLED);
		for (RouteChangeListener l : listener) {
			l.routeChanged(route);
		}
	}

	private void enableRoute() throws TurnoutException, InterruptedException {
		SortedSet<RouteItem> routeItems = route.getRouteItems();
		TurnoutControlIface sc = SRCPTurnoutControl.getInstance();
		for (RouteItem ri : routeItems) {
			Turnout turnoutToRoute = ri.getTurnout();
			System.out.println(turnoutToRoute);
			switch (ri.getRoutedStateEnum()) {
			case STRAIGHT:
				sc.setStraight(turnoutToRoute);
				break;
			case LEFT:
				sc.setCurvedLeft(turnoutToRoute);
				break;
			case RIGHT:
				sc.setCurvedRight(turnoutToRoute);
				break;
			}
			for (RouteChangeListener l : listener) {
				l.nextSwitchRouted();
			}
			Thread.sleep(waitTime);
		}
		sRoute.setRouteState(RouteState.ENABLED);
		for (RouteChangeListener l : listener) {
			l.routeChanged(route);
		}
	}

	public TurnoutException getSwitchException() {
		return switchException;
	}
}
