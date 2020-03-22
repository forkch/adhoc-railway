/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPRouter.java,v 1.3 2008-05-12 18:02:23 fork_ch Exp $
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

import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.turnouts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SRCPRouter extends Thread implements SRCPTurnoutChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SRCPRouter.class);
    private final boolean enableRoute;
    private final int waitTime;
    private final List<SRCPRouteChangeListener> listener;
    private SRCPModelException switchException;
    private final SRCPRoute sRoute;

    public SRCPRouter(final SRCPRoute sRoute, final boolean enableRoute,
                      final int waitTime, final List<SRCPRouteChangeListener> listener) {
        this.sRoute = sRoute;
        this.enableRoute = enableRoute;
        this.waitTime = waitTime;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            sRoute.setRouteState(SRCPRouteState.ROUTING);

            final SRCPTurnoutControl sc = SRCPTurnoutControl.getInstance();
            sc.addTurnoutChangeListener(this);
            if (enableRoute) {
                enableRoute();
            } else {
                disableRoute();
            }
            sc.removeTurnoutChangeListener(this);
        } catch (final SRCPModelException e) {
            this.switchException = e;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disableRoute() throws SRCPTurnoutException,
            SRCPModelException, InterruptedException {
        final List<SRCPRouteItem> routeItems = new ArrayList<SRCPRouteItem>(sRoute.getRouteItems());
        final SRCPTurnoutControl sc = SRCPTurnoutControl.getInstance();
        for (final SRCPRouteItem ri : routeItems) {
            final SRCPTurnout turnoutToRoute = ri.getTurnout();

            sc.setDefaultState(turnoutToRoute);

            Thread.sleep(waitTime);
        }
        sRoute.setRouteState(SRCPRouteState.DISABLED);
        for (final SRCPRouteChangeListener l : listener) {
            l.routeChanged(sRoute);
        }
    }

    private void enableRoute() throws SRCPTurnoutException, SRCPModelException,
            InterruptedException {
        final List<SRCPRouteItem> routeItems = new ArrayList<SRCPRouteItem>(sRoute.getRouteItems());
        final SRCPTurnoutControl sc = SRCPTurnoutControl.getInstance();
        for (final SRCPRouteItem ri : routeItems) {
            final SRCPTurnout turnoutToRoute = ri.getTurnout();
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
                case DEFAULT:
                    sc.setDefaultState(turnoutToRoute);
                    break;
                case NON_DEFAULT:
                    sc.setNonDefaultState(turnoutToRoute);
                    break;
                default:
                    break;
            }
            Thread.sleep(waitTime);
        }
        sRoute.setRouteState(SRCPRouteState.ENABLED);
        for (final SRCPRouteChangeListener l : listener) {
            l.routeChanged(sRoute);
        }
    }

    public SRCPModelException getSwitchException() {
        return switchException;
    }

    @Override
    public void turnoutChanged(final SRCPTurnout changedTurnout,
                               final SRCPTurnoutState newState) {
        for (final SRCPRouteItem item : sRoute.getRouteItems()) {
            if (item.getTurnout().equals(changedTurnout)) {
                for (final SRCPRouteChangeListener l : listener) {
                    if (enableRoute) {
                        l.nextTurnoutRouted(sRoute);
                    } else {
                        l.nextTurnoutDerouted(sRoute);
                    }
                }
            }
        }

    }
}
