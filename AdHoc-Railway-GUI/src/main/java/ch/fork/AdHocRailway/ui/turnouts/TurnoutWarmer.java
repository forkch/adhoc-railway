/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SwitchProgrammer.java 153 2008-03-27 17:44:48Z fork_ch $
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

package ch.fork.AdHocRailway.ui.turnouts;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.widgets.ConfigurationDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TurnoutWarmer extends ConfigurationDialog {
    private final TurnoutManager turnoutPersistence;
    private final TurnoutController turnoutControl;
    private final TurnoutContext ctx;
    private JSpinner turnoutNumberField;
    private JToggleButton warmButton;
    private TurnoutWarmupThread t;

    public TurnoutWarmer(final JFrame owner, final TurnoutContext ctx) {
        super(owner, "Turnout Programmer");
        this.ctx = ctx;
        turnoutPersistence = ctx.getTurnoutManager();
        turnoutControl = ctx.getTurnoutControl();
        initGUI();
    }

    private void initGUI() {
        final JPanel mainPanel = new JPanel(new FlowLayout());

        final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1,
                1000, 1);
        turnoutNumberField = new JSpinner(spinnerModel);
        warmButton = new JToggleButton(new WarmupAction());
        mainPanel.add(turnoutNumberField);
        mainPanel.add(warmButton);
        addMainComponent(mainPanel);

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (t != null) {
                    t.stopWarmup();
                }
            }

        });
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (t != null) {
                    t.stopWarmup();
                }
            }

        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                if (t != null) {
                    t.stopWarmup();
                }
            }

        });

        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    class WarmupAction extends AbstractAction {


        public WarmupAction() {
            super("Start");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (warmButton.isSelected()) {
                warmButton.setText("Stop");
                t = new TurnoutWarmupThread();
                t.start();

            } else {
                t.stopWarmup();
                warmButton.setText("Start");
            }
        }
    }

    private class TurnoutWarmupThread extends Thread {

        boolean enabled = true;

        public void stopWarmup() {
            enabled = false;
        }

        @Override
        public void run() {
            try {

                while (enabled) {
                    final Turnout turnout = turnoutPersistence
                            .getTurnoutByNumber((Integer) turnoutNumberField
                                    .getValue());
                    if (turnout == null) {
                        ctx.getMainApp().displayMessage("no valid turnout selected");
                        return;
                    }
                    turnoutControl.toggle(turnout);
                    Thread.sleep(4 * Preferences.getInstance().getIntValue(
                            PreferencesKeys.ROUTING_DELAY));
                }
            } catch (final InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }
}
