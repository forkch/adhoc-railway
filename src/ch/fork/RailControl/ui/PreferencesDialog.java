/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <Preferences.java>  -  <>
 * 
 * begin     : Apr 10, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
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

package ch.fork.RailControl.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import ch.fork.RailControl.domain.Preferences;

public class PreferencesDialog extends JDialog {

	private JSpinner defaultActivationTime;

	private JSpinner defaultRoutingDelay;

	private SpinnerNumberModel defaultActivationTimeModel;

	private SpinnerNumberModel defaultRoutingDelayModel;

	private JSpinner locomotiveControlNumber;

	private SpinnerNumberModel locomotiveControlNumberModel;

	private JComboBox hostnamesComboBox;

	private JTextField portnumberTextField;

	private JTabbedPane preferencesPane;

	private boolean cancelPressed = false;

	private boolean okPressed = false;

	public PreferencesDialog(JFrame owner) {
		super(owner, "Preferences", true);
	}

	protected void editPreferences(Preferences p) {
		initGUI();
			loadPreferences(p);
		pack();
		setVisible(true);
		if (okPressed) {
			savePreferences(p);
		}
	}

	private void initGUI() {
		preferencesPane = new JTabbedPane();
		preferencesPane.add(createGUISettingsTab(), "General Settings");
		preferencesPane.add(createDigitalDataTab(), "Digital Data");
		preferencesPane.add(createServerTab(), "Server");
		add(preferencesPane, BorderLayout.CENTER);
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
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel createGUISettingsTab() {
		JPanel guiSettingsTab = new JPanel(new SpringLayout());

		JLabel locomotiveControlNumberLabel = new JLabel(
				"Number of Locomotive Controls:");
		locomotiveControlNumberModel = new SpinnerNumberModel(5, 1, 10, 1);
		locomotiveControlNumber = new JSpinner(locomotiveControlNumberModel);
		guiSettingsTab.add(locomotiveControlNumberLabel);
		guiSettingsTab.add(locomotiveControlNumber);
		SpringUtilities.makeCompactGrid(guiSettingsTab, 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		return guiSettingsTab;
	}

	private JPanel createDigitalDataTab() {
		JPanel digitalDataTab = new JPanel(new SpringLayout());

		JLabel defaultActivationTimeLabel = new JLabel(
				"Default activation time for solenoids:");
		defaultActivationTimeModel = new SpinnerNumberModel(50, 50, 1000, 10);
		defaultActivationTime = new JSpinner(defaultActivationTimeModel);

		digitalDataTab.add(defaultActivationTimeLabel);
		digitalDataTab.add(defaultActivationTime);

		JLabel defaultRoutingDelayLabel = new JLabel(
				"Default routing delay for solenoids:");
		defaultRoutingDelayModel = new SpinnerNumberModel(250, 100, 1000, 10);
		defaultRoutingDelay = new JSpinner(defaultRoutingDelayModel);

		digitalDataTab.add(defaultRoutingDelayLabel);
		digitalDataTab.add(defaultRoutingDelay);
		SpringUtilities.makeCompactGrid(digitalDataTab, 2, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		return digitalDataTab;
	}

	private JPanel createServerTab() {
		JPanel serverTab = new JPanel(new SpringLayout());
		JLabel hostnameLabel = new JLabel("Hostname (Name or IP):");
		hostnamesComboBox = new JComboBox();
		serverTab.add(hostnameLabel);
		serverTab.add(hostnamesComboBox);

		JLabel portnumberLabel = new JLabel("Portnumber (e.g. 12345):");
		portnumberTextField = new JTextField("12345", 15);
		serverTab.add(portnumberLabel);
		serverTab.add(portnumberTextField);
		SpringUtilities.makeCompactGrid(serverTab, 2, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		return serverTab;
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

	public boolean isOkPressed() {
		return okPressed;
	}
	
	private void loadPreferences(Preferences p) {
		for (String host : p.getHostnames()) {
			hostnamesComboBox.addItem(host);
		}
		defaultActivationTimeModel.setValue(p.getDefaultActivationTime());
		defaultRoutingDelayModel.setValue(p.getDefaultRoutingDelay());
		hostnamesComboBox.setSelectedItem(p.getHostname());
		portnumberTextField.setText(Integer.toString(p.getPortnumber()));
		locomotiveControlNumberModel.setValue(p.getLocomotiveControlNumber());

	}

	private void savePreferences(Preferences p) {
		p.setDefaultActivationTime(defaultActivationTimeModel.getNumber()
				.intValue());
		p.setDefaultRoutingDelay(defaultRoutingDelayModel.getNumber()
				.intValue());
		p.setHostname((String) hostnamesComboBox.getSelectedItem());
		p.setPortnumber(Integer.parseInt(portnumberTextField.getText()));
		p.setLocomotiveControlNumber(locomotiveControlNumberModel
				.getNumber().intValue());
	}
	
}
