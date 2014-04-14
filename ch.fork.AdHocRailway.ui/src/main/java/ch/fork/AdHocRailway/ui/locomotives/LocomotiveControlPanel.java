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

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.widgets.SimpleInternalFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class LocomotiveControlPanel extends JPanel {

    private final List<LocomotiveWidget> locomotiveWidgets;
    private final LocomotiveContext ctx;
    private JPanel controlPanel;

    public LocomotiveControlPanel(final LocomotiveContext ctx) {
        super();
        this.ctx = ctx;
        ctx.getMainBus().register(this);

        locomotiveWidgets = new ArrayList<LocomotiveWidget>();
        initGUI();
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
            } catch (final ControllerException e3) {
                ctx.getMainApp().handleException(e3);
            }
        }
    }
}
