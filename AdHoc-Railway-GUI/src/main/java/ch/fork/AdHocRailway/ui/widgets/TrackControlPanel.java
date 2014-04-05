/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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

package ch.fork.AdHocRailway.ui.widgets;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.context.TrackContext;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.routes.RouteGroupsPanel;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutGroupsPanel;

import javax.swing.*;
import java.awt.*;

public class TrackControlPanel extends JPanel implements PreferencesKeys {


    private final Preferences preferences = Preferences.getInstance();
    private final TurnoutContext turnoutCtx;
    private final RouteContext routeContext;
    private RouteGroupsPanel routeGroupsTabbedPane;
    private TurnoutGroupsPanel turnoutGroupsTabbedPane;
    private JTabbedPane trackControlPane;

    public TrackControlPanel(final TrackContext ctx) {
        this.turnoutCtx = ctx;
        this.routeContext = ctx;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout(5, 5));
        initTurnoutPanel();
        initRoutesPanel();

        final JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        if (preferences.getBooleanValue(TABBED_TRACK)) {
            trackControlPane = new JTabbedPane();

            trackControlPane.add("Turnouts", turnoutGroupsTabbedPane);
            trackControlPane.add("Routes", routeGroupsTabbedPane);
            final SimpleInternalFrame turnoutRouteFrame = new SimpleInternalFrame(
                    "Turnouts/Routes");
            turnoutRouteFrame.add(trackControlPane, BorderLayout.CENTER);
            controlPanel.add(turnoutRouteFrame);
        } else {
            final SimpleInternalFrame turnoutFrame = new SimpleInternalFrame(
                    "Turnouts");
            final SimpleInternalFrame routesFrame = new SimpleInternalFrame(
                    "Routes");

            turnoutFrame.add(turnoutGroupsTabbedPane, BorderLayout.CENTER);
            routesFrame.add(routeGroupsTabbedPane, BorderLayout.CENTER);

            controlPanel.add(turnoutFrame);
            controlPanel.add(routesFrame);
        }
        add(controlPanel, BorderLayout.CENTER);
    }

    private void initTurnoutPanel() {
        turnoutGroupsTabbedPane = new TurnoutGroupsPanel(turnoutCtx,
                JTabbedPane.BOTTOM);
    }

    private void initRoutesPanel() {
        routeGroupsTabbedPane = new RouteGroupsPanel(routeContext,
                JTabbedPane.BOTTOM);
    }

}