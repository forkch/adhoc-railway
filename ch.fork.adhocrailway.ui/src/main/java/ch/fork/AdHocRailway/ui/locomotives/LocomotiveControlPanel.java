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
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToPersistenceEvent;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.widgets.SimpleInternalFrame;
import com.google.common.eventbus.Subscribe;
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
        setLayout(new MigLayout("fill, insets 0"));
        controlPanel = new JPanel(new MigLayout("insets 0"));
        JScrollPane controlPanelScrollPane = new JScrollPane(controlPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        final SimpleInternalFrame locomotivesFrame = new SimpleInternalFrame(
                "Trains");
        locomotivesFrame.add(controlPanelScrollPane, BorderLayout.CENTER);
        add(locomotivesFrame, "grow");

        ctx.getMainApp().registerSpaceKey(new LocomotiveStopAction());
        update();
    }

    private void update() {


        controlPanel.removeAll();
        locomotiveWidgets.clear();

        for (int i = 0; i < Preferences.getInstance().getIntValue(
                PreferencesKeys.LOCOMOTIVE_CONTROLES); i++) {
            final LocomotiveWidget w = new LocomotiveWidget(ctx, i,
                    ctx.getMainFrame());
            controlPanel.add(w);
            locomotiveWidgets.add(w);
        }
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

                    locomotiveControl.emergencyStop(myLocomotive);
                    widget.emergencyStop();
                }
            } catch (final ControllerException e3) {
                ctx.getMainApp().handleException(e3);
            }
        }
    }
}
