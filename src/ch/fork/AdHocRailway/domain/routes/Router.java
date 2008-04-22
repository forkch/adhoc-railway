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

import java.util.List;

import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class Router extends Thread {

	private boolean							enableRoute;
	private int								waitTime;
	private List<SRCPRouteChangeListener>	listener;
	private TurnoutException				switchException;
	private SRCPRoute						sRoute;

	public Router(SRCPRoute sRoute, boolean enableRoute, int waitTime,
			List<SRCPRouteChangeListener> listener) {
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
		List<SRCPRouteItem> routeItems = sRoute.getRouteItems();
		SRCPTurnoutControl sc = SRCPTurnoutControl.getInstance();
		for (SRCPRouteItem ri : routeItems) {
			SRCPTurnout turnoutToRoute = ri.getTurnout();

			sc.setDefaultState(turnoutToRoute);
			for (SRCPRouteChangeListener l : listener) {
				l.nextTurnoutDerouted(sRoute);
			}
			Thread.sleep(waitTime);
		}
		sRoute.setRouteState(SRCPRouteState.DISABLED);
		for (SRCPRouteChangeListener l : listener) {
			l.routeChanged(sRoute);
		}
	}

	private void enableRoute() throws TurnoutException, InterruptedException {
		List<SRCPRouteItem> routeItems = sRoute.getRouteItems();
		SRCPTurnoutControl sc = SRCPTurnoutControl.getInstance();
		for (SRCPRouteItem ri : routeItems) {
			SRCPTurnout turnoutToRoute = ri.getTurnout();
			switch (ri.getRoutedState()) {
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
			for (SRCPRouteChangeListener l : listener) {
				l.nextTurnoutRouted(sRoute);
			}
			Thread.sleep(waitTime);
		}
		sRoute.setRouteState(SRCPRouteState.ENABLED);
		for (SRCPRouteChangeListener l : listener) {
			l.routeChanged(sRoute);
		}
	}

	public TurnoutException getSwitchException() {
		return switchException;
	}
}
