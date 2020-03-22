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

package ch.fork.adhocrailway.ui.widgets;

import ch.fork.adhocrailway.technical.configuration.Preferences;
import ch.fork.adhocrailway.technical.configuration.PreferencesKeys;
import ch.fork.adhocrailway.ui.context.RouteContext;
import ch.fork.adhocrailway.ui.context.TrackContext;
import ch.fork.adhocrailway.ui.context.TurnoutContext;
import ch.fork.adhocrailway.ui.routes.RouteGroupsPanel;
import ch.fork.adhocrailway.ui.turnouts.TurnoutGroupsPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

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
        setLayout(new BorderLayout(0, 0));
        initTurnoutPanel();
        initRoutesPanel();
        initShortcuts();

        final JPanel controlPanel = new JPanel(new MigLayout("insets 0, gap 2, fill"));
        if (preferences.getBooleanValue(TABBED_TRACK)) {
            trackControlPane = new JTabbedPane();

            trackControlPane.add("Turnouts", turnoutGroupsTabbedPane);
            trackControlPane.add("Routes", routeGroupsTabbedPane);
            controlPanel.add(trackControlPane);
        } else {
            controlPanel.add(turnoutGroupsTabbedPane, "w 50%, h 100%");
            controlPanel.add(routeGroupsTabbedPane, "w 50%, h 100%");
        }
        add(controlPanel, BorderLayout.CENTER);
    }

    private void initShortcuts() {
        for (int i = 0; i < 10; i++) {
            char c = Character.forDigit(i + 1, 10);
            turnoutCtx.getMainApp().registerKey(KeyEvent.getExtendedKeyCodeForChar(c), InputEvent.CTRL_DOWN_MASK, new SelectGroupAction(i));
        }
    }

    private void initTurnoutPanel() {
        turnoutGroupsTabbedPane = new TurnoutGroupsPanel(turnoutCtx,
                JTabbedPane.BOTTOM);
    }

    private void initRoutesPanel() {
        routeGroupsTabbedPane = new RouteGroupsPanel(routeContext,
                JTabbedPane.BOTTOM);
    }

    private class SelectGroupAction extends AbstractAction {
        private int groupNumber;

        public SelectGroupAction(int groupNumber) {
            this.groupNumber = groupNumber;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (groupNumber < turnoutGroupsTabbedPane.getTabCount()) {
                turnoutGroupsTabbedPane.setSelectedIndex(groupNumber);
            }
            if (groupNumber < routeGroupsTabbedPane.getTabCount()) {
                routeGroupsTabbedPane.setSelectedIndex(groupNumber);
            }

        }
    }
}
