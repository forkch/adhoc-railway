/*------------------------------------------------------------------------
 * 
 * <./ui/PreferencesDialog.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:22 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
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


package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class PreferencesDialog extends JDialog implements PreferencesKeys {
    private JSpinner           defaultActivationTime;
    private JSpinner           defaultRoutingDelay;
    private JSpinner           defaultLockDuration;
    private SpinnerNumberModel defaultActivationTimeModel;
    private SpinnerNumberModel defaultRoutingDelayModel;
    private SpinnerNumberModel defaultLockDurationModel;
    private JSpinner           locomotiveControlNumber;
    private JSpinner           switchControlNumber;
    private SpinnerNumberModel locomotiveControlNumberModel;
    private SpinnerNumberModel switchControlNumberModel;
    private JTextField         hostnameTextField;
    private JTextField         portnumberTextField;
    private JTabbedPane        preferencesPane;
    private JComboBox          keyBoardLayoutComboBox;
    private JCheckBox          interface6051;
    private JCheckBox          writeLog;
    private JCheckBox          fullscreen;
    private JCheckBox          tabbedTrackCheckBox;
    private List<String>       hostnames;

    private boolean            okPressed;
    private boolean            cancelPressed;
    private JCheckBox          autoconnectCheckBox;

    public PreferencesDialog(JFrame owner) {
        super(owner, "Preferences", true);
        initGUI();
    }

    private void initGUI() {
        preferencesPane = new JTabbedPane();
        preferencesPane.add(createGUISettingsTab(), "General Settings");
        preferencesPane.add(createDigitalDataTab(), "Digital Data");
        preferencesPane.add(createServerTab(), "Server");
        add(preferencesPane, BorderLayout.NORTH);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                PreferencesDialog.this.setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                PreferencesDialog.this.setVisible(false);
            }
        });
        JPanel mainButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        mainButtonPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                PreferencesDialog.this.setVisible(false);
            }
        }, "", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        mainButtonPanel.add(okButton);
        mainButtonPanel.add(cancelButton);
        add(mainButtonPanel, BorderLayout.SOUTH);
        loadPreferences();
        pack();
        setVisible(true);
        savePreferences();
    }

    private JPanel createGUISettingsTab() {
        JPanel guiSettingsTab = new JPanel(new SpringLayout());
        JLabel locomotiveControlNumberLabel = new JLabel(
            "Number of Locomotive Controls:");
        locomotiveControlNumberModel = new SpinnerNumberModel(5, 1, 10, 1);
        locomotiveControlNumber = new JSpinner(locomotiveControlNumberModel);
        JLabel switchControlNumberLabel = new JLabel(
            "Number of Switch Controls per row:");
        switchControlNumberModel = new SpinnerNumberModel(7, 1, 10, 1);
        switchControlNumber = new JSpinner(switchControlNumberModel);
        JLabel keyBoardLayoutLabel = new JLabel("Keyboard-Layout");
        keyBoardLayoutComboBox = new JComboBox();
        keyBoardLayoutComboBox.addItem("Swiss German");
        keyBoardLayoutComboBox.addItem("English");

        writeLog = new JCheckBox();
        JLabel writeLogLabel = new JLabel("Write Log");
        fullscreen = new JCheckBox();
        JLabel fullscreenLabel = new JLabel("Fullscreen");
        tabbedTrackCheckBox = new JCheckBox();
        JLabel tabbedTrackLabel = new JLabel("Tabbed Track-Control");

        guiSettingsTab.add(locomotiveControlNumberLabel);
        guiSettingsTab.add(locomotiveControlNumber);
        guiSettingsTab.add(switchControlNumberLabel);
        guiSettingsTab.add(switchControlNumber);
        guiSettingsTab.add(keyBoardLayoutLabel);
        guiSettingsTab.add(keyBoardLayoutComboBox);
        guiSettingsTab.add(writeLogLabel);
        guiSettingsTab.add(writeLog);
        guiSettingsTab.add(fullscreenLabel);
        guiSettingsTab.add(fullscreen);
        guiSettingsTab.add(tabbedTrackLabel);
        guiSettingsTab.add(tabbedTrackCheckBox);
        SpringUtilities.makeCompactGrid(guiSettingsTab, 6, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad

        JPanel north = new JPanel(new BorderLayout());
        north.add(guiSettingsTab, BorderLayout.NORTH);
        return north;
    }

    private JPanel createDigitalDataTab() {
        JPanel digitalDataTab = new JPanel(new SpringLayout());

        JLabel defaultActivationTimeLabel = new JLabel(
            "Default activation time for solenoids [ms]:");
        defaultActivationTimeModel = new SpinnerNumberModel(50, 50, 1000, 10);
        defaultActivationTime = new JSpinner(defaultActivationTimeModel);

        JLabel defaultRoutingDelayLabel = new JLabel(
            "Default routing delay for solenoids [ms]:");
        defaultRoutingDelayModel = new SpinnerNumberModel(250, 100, 1000, 10);
        defaultRoutingDelay = new JSpinner(defaultRoutingDelayModel);

        JLabel defaultLockDurationLabel = new JLabel(
            "Default Lock time (0 means forever) [s]:");
        defaultLockDurationModel = new SpinnerNumberModel(0, 0, 60, 1);
        defaultLockDuration = new JSpinner(defaultLockDurationModel);

        interface6051 = new JCheckBox();
        JLabel interface6051Label = new JLabel("Interface 6051 attached");


        digitalDataTab.add(defaultActivationTimeLabel);
        digitalDataTab.add(defaultActivationTime);
        digitalDataTab.add(defaultRoutingDelayLabel);
        digitalDataTab.add(defaultRoutingDelay);
        digitalDataTab.add(defaultLockDurationLabel);
        digitalDataTab.add(defaultLockDuration);
        digitalDataTab.add(interface6051Label);
        digitalDataTab.add(interface6051);
        SpringUtilities.makeCompactGrid(digitalDataTab, 4, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        JPanel north = new JPanel(new BorderLayout());
        north.add(digitalDataTab, BorderLayout.NORTH);
        return north;
    }

    private JPanel createServerTab() {
        JPanel serverTab = new JPanel(new SpringLayout());
        JLabel hostnameLabel = new JLabel("Hostname (Name or IP):");
        hostnameTextField = new JTextField();
        serverTab.add(hostnameLabel);
        serverTab.add(hostnameTextField);
        JLabel portnumberLabel = new JLabel("Portnumber (e.g. 12345):");
        portnumberTextField = new JTextField("12345", 15);
        serverTab.add(portnumberLabel);
        serverTab.add(portnumberTextField);
        JLabel autoconnectLabel = new JLabel("Autoconnect:");
        autoconnectCheckBox = new JCheckBox();
        serverTab.add(autoconnectLabel);
        serverTab.add(autoconnectCheckBox);
        SpringUtilities.makeCompactGrid(serverTab, 3, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        JPanel north = new JPanel(new BorderLayout());
        north.add(serverTab, BorderLayout.NORTH);
        return north;
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    private void loadPreferences() {
        Preferences p = Preferences.getInstance();
        locomotiveControlNumberModel.setValue(p
            .getIntValue(LOCOMOTIVE_CONTROLES));
        switchControlNumberModel.setValue(p.getIntValue(SWITCH_CONTROLES));
        keyBoardLayoutComboBox.setSelectedItem(p
            .getStringValue(KEYBOARD_LAYOUT));

        defaultActivationTimeModel.setValue(p.getIntValue(ACTIVATION_TIME));
        defaultRoutingDelayModel.setValue(p.getIntValue(ROUTING_DELAY));
        defaultLockDurationModel.setValue(p.getIntValue(LOCK_DURATION));

        interface6051.setSelected(p.getBooleanValue(INTERFACE_6051));
        writeLog.setSelected(p.getBooleanValue(LOGGING));
        fullscreen.setSelected(p.getBooleanValue(FULLSCREEN));
        tabbedTrackCheckBox.setSelected(p.getBooleanValue(TABBED_TRACK));
        hostnameTextField.setText(p.getStringValue(HOSTNAME));
        portnumberTextField.setText(Integer.toString(p.getIntValue(PORT)));
        autoconnectCheckBox.setSelected(p.getBooleanValue(AUTOCONNECT));
    }

    public void savePreferences() {
        Preferences p = Preferences.getInstance();
        p.setIntValue(LOCOMOTIVE_CONTROLES, locomotiveControlNumberModel
            .getNumber().intValue());
        p.setIntValue(SWITCH_CONTROLES, switchControlNumberModel.getNumber()
            .intValue());
        p.setStringValue(KEYBOARD_LAYOUT, keyBoardLayoutComboBox
            .getSelectedItem().toString());

        p.setIntValue(ACTIVATION_TIME, defaultActivationTimeModel.getNumber()
            .intValue());
        p.setIntValue(ROUTING_DELAY, defaultRoutingDelayModel.getNumber()
            .intValue());
        p.setIntValue(LOCK_DURATION, defaultLockDurationModel.getNumber()
            .intValue());
        p.setBooleanValue(INTERFACE_6051, interface6051.isSelected());
        p.setBooleanValue(LOGGING, writeLog.isSelected());
        p.setBooleanValue(FULLSCREEN, fullscreen.isSelected());
        p.setBooleanValue(TABBED_TRACK, tabbedTrackCheckBox.isSelected());

        p.setStringValue(HOSTNAME, (String) hostnameTextField.getText());
        p.setIntValue(PORT, Integer.parseInt(portnumberTextField.getText()));
        p.setBooleanValue(AUTOCONNECT, autoconnectCheckBox.isSelected());
    }

}
