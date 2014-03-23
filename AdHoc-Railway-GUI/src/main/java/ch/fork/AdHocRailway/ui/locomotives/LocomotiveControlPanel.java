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

package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.controllers.LocomotiveException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerListener;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.bus.events.StartImportEvent;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.widgets.SimpleInternalFrame;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class LocomotiveControlPanel extends JPanel implements
        LocomotiveManagerListener {

    private final List<LocomotiveWidget> locomotiveWidgets;
    private JPanel controlPanel;
    private final LocomotiveContext ctx;
    private boolean disableListener;

    public LocomotiveControlPanel(final LocomotiveContext ctx) {
        super();
        this.ctx = ctx;
        ctx.getMainBus().register(this);

        locomotiveWidgets = new ArrayList<LocomotiveWidget>();
        initGUI();
    }

    @Subscribe
    public void startImport(final StartImportEvent event) {
        disableListener = true;
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        disableListener = false;
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        controlPanel = new JPanel(new MigLayout());

        final SimpleInternalFrame locomotivesFrame = new SimpleInternalFrame(
                "Trains");
        locomotivesFrame.add(controlPanel, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(locomotivesFrame, BorderLayout.NORTH);

        ctx.getMainApp().registerSpaceKey(new LocomotiveStopAction());
        // getActionMap().put("LocomotiveStop", new LocomotiveStopAction());
        // Preferences
        // .getInstance()
        // .getKeyBoardLayout()
        // .assignKeys(getInputMap(WHEN_IN_FOCUSED_WINDOW),
        // "LocomotiveStop");
        update();
    }

    public void update() {
        final LocomotiveController locomotiveControl = ctx
                .getLocomotiveControl();
        locomotiveControl.removeAllLocomotiveChangeListener();

        controlPanel.removeAll();
        locomotiveWidgets.clear();

        for (int i = 0; i < Preferences.getInstance().getIntValue(
                PreferencesKeys.LOCOMOTIVE_CONTROLES); i++) {
            final LocomotiveWidget w = new LocomotiveWidget(ctx, i,
                    ctx.getMainFrame());
            controlPanel.add(w);
            locomotiveWidgets.add(w);
        }
        revalidate();
        repaint();
    }

    private class LocomotiveStopAction extends AbstractAction implements
            Runnable {

        @Override
        public void actionPerformed(final ActionEvent e) {
            final Thread t = new Thread(this);
            t.start();
        }

        @Override
        public void run() {
            try {
                for (final LocomotiveWidget widget : locomotiveWidgets) {
                    final Locomotive myLocomotive = widget.getMyLocomotive();
                    if (myLocomotive == null) {
                        continue;
                    }
                    final LocomotiveController locomotiveControl = ctx
                            .getLocomotiveControl();
                    if (locomotiveControl.isLocked(myLocomotive)
                            && !locomotiveControl.isLockedByMe(myLocomotive)) {
                        continue;
                    }
                    locomotiveControl.emergencyStop(myLocomotive);
                }
            } catch (final LocomotiveException e3) {
                ctx.getMainApp().handleException(e3);
            }
        }
    }

    @Override
    public void locomotiveAdded(final Locomotive locomotive) {

    }

    @Override
    public void locomotiveUpdated(final Locomotive locomotive) {

    }

    @Override
    public void locomotiveGroupAdded(final LocomotiveGroup group) {

    }

    @Override
    public void locomotiveRemoved(final Locomotive locomotive) {

    }

    @Override
    public void locomotiveGroupRemoved(final LocomotiveGroup group) {

    }

    @Override
    public void locomotiveGroupUpdated(final LocomotiveGroup group) {

    }

    @Override
    public void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> locomotiveGroups) {

    }

    @Override
    public void failure(
            final LocomotiveManagerException locomotiveManagerException) {

    }
}
