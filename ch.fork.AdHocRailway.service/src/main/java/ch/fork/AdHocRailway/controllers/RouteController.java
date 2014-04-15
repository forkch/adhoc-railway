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

import ch.fork.AdHocRailway.model.turnouts.Route;
import com.google.common.collect.Maps;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class RouteController {

    private final Map<Route, List<RouteChangeListener>> listeners = Maps
            .newHashMap();


    public abstract void enableRoute(final Route r);

    public abstract void disableRoute(final Route r);

    public abstract void setRoutingDelay(final int intValue);

    public void toggle(final Route route) {
        if (route.isEnabled()) {
            disableRoute(route);
        } else {
            enableRoute(route);
        }
    }

    public void toggleTest(final Route route) {
        toggle(route);
    }

    public void addRouteChangeListener(final Route route,
                                       final RouteChangeListener listener) {

        List<RouteChangeListener> routeChangeListeners = listeners.get(route);
        if (routeChangeListeners == null) {
            routeChangeListeners = new LinkedList<RouteChangeListener>();
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
}