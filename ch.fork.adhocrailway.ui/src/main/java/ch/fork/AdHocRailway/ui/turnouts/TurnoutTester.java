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
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.widgets.ConfigurationDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TurnoutTester extends ConfigurationDialog {
    private final TurnoutManager turnoutPersistence;
    private final TurnoutController turnoutControl;
    private final TurnoutContext ctx;
    private final Preferences preferences;
    private JSpinner turnoutNumberField;
    private JToggleButton testButton;
    private TurnoutTesterThread t;
    private JSpinner turnoutAddressField;
    private JRadioButton turnoutNumberButton;
    private JRadioButton turnoutAddressButton;
    private SpinnerNumberModel turnoutAddressSpinnerModel;
    private SpinnerNumberModel turnoutNumberSpinnerModel;

    public TurnoutTester(final JFrame owner, final TurnoutContext ctx) {
        super(owner, "Turnout Programmer", false);
        this.ctx = ctx;

        preferences = ctx.getPreferences();
        turnoutPersistence = ctx.getTurnoutManager();
        turnoutControl = ctx.getTurnoutControl();
        initGUI();
    }

    private void initGUI() {
        final JPanel mainPanel = new JPanel(new MigLayout("insets 5, gap 5"));

        turnoutNumberSpinnerModel = new SpinnerNumberModel(1, 1,
                1000, 1);
        turnoutAddressSpinnerModel = new SpinnerNumberModel(1, 1,
                1000, 1);
        turnoutNumberButton = new JRadioButton("Turnout number");
        turnoutNumberField = new JSpinner(turnoutNumberSpinnerModel);
        turnoutAddressButton = new JRadioButton("Turnout address");
        turnoutAddressField = new JSpinner(turnoutAddressSpinnerModel);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(turnoutNumberButton);
        buttonGroup.add(turnoutAddressButton);
        testButton = new JToggleButton(new WarmupAction());

        mainPanel.add(turnoutNumberButton);
        mainPanel.add(turnoutNumberField, "wrap");
        mainPanel.add(turnoutAddressButton);
        mainPanel.add(turnoutAddressField, "wrap");
        mainPanel.add(testButton, "span 2, grow");

        addMainComponent(mainPanel);

        setSelectedButtonBasedOnPreferences();
        setInitialValues();

        turnoutNumberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTurnoutNumberField(true);
            }
        });

        turnoutAddressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableTurnoutNumberField(false);
            }
        });

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (t != null) {
                    t.stopTesting();
                }
            }

        });
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (t != null) {
                    t.stopTesting();
                }
            }

        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                if (t != null) {
                    t.stopTesting();
                }
            }

        });

        Font biggerFont = turnoutAddressButton.getFont().deriveFont(30);
        setFontOnComponents(new Font("Dialog", Font.PLAIN, 25), turnoutAddressButton, turnoutNumberButton, testButton, turnoutAddressField, turnoutNumberField);
        SwingUtils.addEscapeListener(this);
        pack();
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void setInitialValues() {
        turnoutNumberSpinnerModel.setValue(preferences.getIntValue(PreferencesKeys.LAST_TESTED_NUMBER, 1));
        turnoutAddressSpinnerModel.setValue(preferences.getIntValue(PreferencesKeys.LAST_TESTED_ADDRESS, 1));
    }

    private void setSelectedButtonBasedOnPreferences() {
        boolean turnoutTesterByNumber = preferences.getBooleanValue(PreferencesKeys.TURNOUT_TESTER_BY_NUMBER, true);
        turnoutNumberButton.setSelected(turnoutTesterByNumber);
        turnoutAddressButton.setSelected(!turnoutTesterByNumber);
        enableTurnoutNumberField(turnoutTesterByNumber);
    }

    private void enableTurnoutNumberField(boolean turnoutNumberEnabled) {
        turnoutNumberField.setEnabled(turnoutNumberEnabled);
        turnoutAddressField.setEnabled(!turnoutNumberEnabled);
    }

    class WarmupAction extends AbstractAction {


        public WarmupAction() {
            super("Start");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (testButton.isSelected()) {
                testButton.setText("Stop");
                t = new TurnoutTesterThread();
                t.start();

            } else {
                t.stopTesting();
                testButton.setText("Start");
            }
        }
    }

    private static void setFontOnComponents(Font font, JComponent... components) {
        for (JComponent component : components) {
            component.setFont(font);
        }
    }

    private class TurnoutTesterThread extends Thread {

        boolean enabled = true;

        public void stopTesting() {
            enabled = false;
        }

        @Override
        public void run() {
            try {
                if (turnoutNumberButton.isSelected()) {
                    testTurnoutByNumber();
                } else {
                    testTurnoutByAddress();
                }
            } catch (final InterruptedException e2) {
                e2.printStackTrace();
            }
        }

        private void testTurnoutByAddress() throws InterruptedException {
            TurnoutState nextTurnoutState = TurnoutState.LEFT;
            int address = turnoutAddressSpinnerModel.getNumber().intValue();
            preferences.setIntValue(PreferencesKeys.LAST_TESTED_ADDRESS, address);
            while (enabled) {
                turnoutControl.setTurnoutWithAddress(address, nextTurnoutState);
                if (TurnoutState.LEFT.equals(nextTurnoutState)) {
                    nextTurnoutState = TurnoutState.STRAIGHT;
                } else {
                    nextTurnoutState = TurnoutState.LEFT;
                }
                waitTime();
            }
        }

        private void testTurnoutByNumber() throws InterruptedException {
            int number = turnoutNumberSpinnerModel.getNumber().intValue();
            final Turnout turnout = turnoutPersistence
                    .getTurnoutByNumber(number);

            if (turnout == null) {
                ctx.getMainApp().displayMessage("no valid turnout selected");
                return;
            }

            preferences.setIntValue(PreferencesKeys.LAST_TESTED_NUMBER, number);
            while (enabled) {
                turnoutControl.toggle(turnout);
                waitTime();
            }
        }

        private void waitTime() throws InterruptedException {
            Thread.sleep(4 * Preferences.getInstance().getIntValue(
                    PreferencesKeys.ROUTING_DELAY));
        }
    }
}
