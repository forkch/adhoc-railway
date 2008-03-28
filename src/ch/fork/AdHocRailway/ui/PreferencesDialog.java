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

package ch.fork.AdHocRailway.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PreferencesDialog extends JDialog implements PreferencesKeys {
	private JSpinner			defaultActivationTime;
	private JSpinner			defaultRoutingDelay;
	private JSpinner			defaultLockDuration;
	private SpinnerNumberModel	defaultActivationTimeModel;
	private SpinnerNumberModel	defaultRoutingDelayModel;
	private SpinnerNumberModel	defaultLockDurationModel;
	private JSpinner			locomotiveControlNumber;
	private JSpinner			switchControlNumber;
	private JSpinner			routeControlNumber;
	private SpinnerNumberModel	locomotiveControlNumberModel;
	private SpinnerNumberModel	switchControlNumberModel;
	private JTextField			hostnameTextField;
	private JTextField			portnumberTextField;
	private JComboBox			keyBoardLayoutComboBox;
	private JCheckBox			interface6051;
	private JCheckBox			writeLog;
	private JCheckBox			fullscreen;
	private JCheckBox			tabbedTrackCheckBox;
	private JCheckBox			fixedTurnoutGroupSizesCheckBox;

	private boolean				okPressed;
	private boolean				cancelPressed;
	private JCheckBox			autoconnectCheckBox;
	private SpinnerNumberModel	routeControlNumberModel;
	private JTextField			databaseHostField;
	private JTextField			databaseNameField;
	private JTextField			databaseUserField;
	private JTextField			databasePasswordField;
	private JCheckBox			useDatabaseCheckBox;
	private JCheckBox			openLastFileCheckBox;

	public PreferencesDialog(JFrame owner) {
		super(owner, "Preferences", true);
		initGUI();
	}

	private void initGUI() {

		FormLayout layout = new FormLayout("5dlu, pref, 10dlu, pref, 5dlu",
				"5dlu, pref, 3dlu, top:pref,3dlu, pref, 3dlu, top:pref, 3dlu, pref, 5dlu");

		layout.setColumnGroups(new int[][] { { 2, 4 } });

		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xy(2, 2));
		builder.add(createGUISettingsTab(), cc.xy(2, 4));

		builder.addSeparator("Digital", cc.xy(4, 2));
		builder.add(createDigitalDataTab(), cc.xy(4, 4));

		builder.addSeparator("SRCP-Server", cc.xy(2, 6));
		builder.add(createServerTab(), cc.xy(2, 8));

		builder.addSeparator("Database", cc.xy(4, 6));
		builder.add(createDatabaseTab(), cc.xy(4, 8));

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
		builder.add(ButtonBarFactory.buildRightAlignedBar(okButton,
				cancelButton), cc.xyw(2, 10, 3));

		add(builder.getPanel());
		loadPreferences();
		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
		savePreferences();
	}

	private JPanel createGUISettingsTab() {
		locomotiveControlNumberModel = new SpinnerNumberModel(5, 1, 10, 1);
		locomotiveControlNumber = new JSpinner(locomotiveControlNumberModel);

		switchControlNumberModel = new SpinnerNumberModel(7, 1, 10, 1);
		switchControlNumber = new JSpinner(switchControlNumberModel);

		routeControlNumberModel = new SpinnerNumberModel(7, 1, 10, 1);
		routeControlNumber = new JSpinner(routeControlNumberModel);
		
		fixedTurnoutGroupSizesCheckBox = new JCheckBox();
		

		keyBoardLayoutComboBox = new JComboBox();
		keyBoardLayoutComboBox.addItem("Swiss German");
		keyBoardLayoutComboBox.addItem("English");

		writeLog = new JCheckBox();
		fullscreen = new JCheckBox();
		tabbedTrackCheckBox = new JCheckBox();
		openLastFileCheckBox = new JCheckBox();

		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Locomotive Controls", cc.xy(1, 1));
		builder.add(locomotiveControlNumber, cc.xy(3, 1));

		builder.addLabel("Turnout Controls per row", cc.xy(1, 3));
		builder.add(switchControlNumber, cc.xy(3, 3));

		builder.addLabel("Route Controls per row", cc.xy(1, 5));
		builder.add(routeControlNumber, cc.xy(3, 5));

		builder.addLabel("Keyboard-Layout", cc.xy(1, 7));
		builder.add(keyBoardLayoutComboBox, cc.xy(3, 7));

		builder.addLabel("Write Log", cc.xy(1, 9));
		builder.add(writeLog, cc.xy(3, 9));

		builder.addLabel("Fullscreen", cc.xy(1, 11));
		builder.add(fullscreen, cc.xy(3, 11));

		builder.addLabel("Tabbed Track-Control", cc.xy(1, 13));
		builder.add(tabbedTrackCheckBox, cc.xy(3, 13));

		builder.addLabel("Fixed Turnout- and Route-Group sizes", cc.xy(1, 15));
		builder.add(fixedTurnoutGroupSizesCheckBox, cc.xy(3, 15));
		
		builder.addLabel("Open last file", cc.xy(1, 17));
		builder.add(openLastFileCheckBox, cc.xy(3, 17));

		return builder.getPanel();
	}

	private JPanel createDigitalDataTab() {
		defaultActivationTimeModel = new SpinnerNumberModel(50, 50, 1000, 10);
		defaultActivationTime = new JSpinner(defaultActivationTimeModel);

		defaultRoutingDelayModel = new SpinnerNumberModel(250, 100, 1000, 10);
		defaultRoutingDelay = new JSpinner(defaultRoutingDelayModel);

		defaultLockDurationModel = new SpinnerNumberModel(0, 0, 60, 1);
		defaultLockDuration = new JSpinner(defaultLockDurationModel);

		interface6051 = new JCheckBox();

		FormLayout layout = new FormLayout("right:pref, 3dlu, fill:pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.addLabel("Solenoid activation time [ms]", cc.xy(1, 1));
		builder.add(defaultActivationTime, cc.xy(3, 1));

		builder.addLabel("Routing delay [ms]", cc.xy(1, 3));
		builder.add(defaultRoutingDelay, cc.xy(3, 3));

		builder.addLabel("Lock time (0 means forever) [s]", cc.xy(1, 5));
		builder.add(defaultLockDuration, cc.xy(3, 5));

		builder.addLabel("Interface 6051 attached", cc.xy(1, 7));
		builder.add(interface6051, cc.xy(3, 7));

		return builder.getPanel();
	}

	private JPanel createServerTab() {

		hostnameTextField = new JTextField(15);

		portnumberTextField = new JTextField("12345", 15);

		autoconnectCheckBox = new JCheckBox();

		FormLayout layout = new FormLayout("right:pref, 3dlu, fill:pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		builder.addLabel("Hostname (Name or IP)", cc.xy(1, 1));
		builder.add(hostnameTextField, cc.xy(3, 1));

		builder.addLabel("Portnumber (e.g. 12345)", cc.xy(1, 3));
		builder.add(portnumberTextField, cc.xy(3, 3));

		builder.addLabel("Autoconnect", cc.xy(1, 5));
		builder.add(autoconnectCheckBox, cc.xy(3, 5));

		return builder.getPanel();
	}

	private JPanel createDatabaseTab() {

		useDatabaseCheckBox = new JCheckBox();

		databaseHostField = new JTextField(15);

		databaseNameField = new JTextField(15);

		databaseUserField = new JTextField(15);

		databasePasswordField = new JPasswordField(15);

		FormLayout layout = new FormLayout("right:pref, 3dlu, fill:pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Use Database", cc.xy(1, 1));
		builder.add(useDatabaseCheckBox, cc.xy(3, 1));

		builder.addLabel("Host", cc.xy(1, 3));
		builder.add(databaseHostField, cc.xy(3, 3));

		builder.addLabel("Database", cc.xy(1, 5));
		builder.add(databaseNameField, cc.xy(3, 5));

		builder.addLabel("User", cc.xy(1, 7));
		builder.add(databaseUserField, cc.xy(3, 7));

		builder.addLabel("Password", cc.xy(1, 9));
		builder.add(databasePasswordField, cc.xy(3, 9));

		return builder.getPanel();
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
		switchControlNumberModel.setValue(p.getIntValue(TURNOUT_CONTROLES));
		routeControlNumberModel.setValue(p.getIntValue(ROUTE_CONTROLES));
		keyBoardLayoutComboBox.setSelectedItem(p
				.getStringValue(KEYBOARD_LAYOUT));
		writeLog.setSelected(p.getBooleanValue(LOGGING));
		fullscreen.setSelected(p.getBooleanValue(FULLSCREEN));
		tabbedTrackCheckBox.setSelected(p.getBooleanValue(TABBED_TRACK));
		fixedTurnoutGroupSizesCheckBox.setSelected(p.getBooleanValue(USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES));
		openLastFileCheckBox.setSelected(p.getBooleanValue(OPEN_LAST_FILE));

		defaultActivationTimeModel.setValue(p.getIntValue(ACTIVATION_TIME));
		defaultRoutingDelayModel.setValue(p.getIntValue(ROUTING_DELAY));
		defaultLockDurationModel.setValue(p.getIntValue(LOCK_DURATION));
		interface6051.setSelected(p.getBooleanValue(INTERFACE_6051));

		hostnameTextField.setText(p.getStringValue(HOSTNAME));
		portnumberTextField.setText(Integer.toString(p.getIntValue(PORT)));
		autoconnectCheckBox.setSelected(p.getBooleanValue(AUTOCONNECT));

		useDatabaseCheckBox.setSelected(p.getBooleanValue(USE_DATABASE));
		databaseHostField.setText(p.getStringValue(DATABASE_HOST));
		databaseNameField.setText(p.getStringValue(DATABASE_NAME));
		databaseUserField.setText(p.getStringValue(DATABASE_USER));
		databasePasswordField.setText(p.getStringValue(DATABASE_PWD));
	}

	public void savePreferences() {
		Preferences p = Preferences.getInstance();
		p.setIntValue(LOCOMOTIVE_CONTROLES, locomotiveControlNumberModel
				.getNumber().intValue());
		p.setIntValue(TURNOUT_CONTROLES, switchControlNumberModel.getNumber()
				.intValue());
		p.setIntValue(ROUTE_CONTROLES, routeControlNumberModel.getNumber()
				.intValue());
		p.setStringValue(KEYBOARD_LAYOUT, keyBoardLayoutComboBox
				.getSelectedItem().toString());
		p.setBooleanValue(LOGGING, writeLog.isSelected());
		p.setBooleanValue(FULLSCREEN, fullscreen.isSelected());
		p.setBooleanValue(TABBED_TRACK, tabbedTrackCheckBox.isSelected());
		p.setBooleanValue(USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES, fixedTurnoutGroupSizesCheckBox.isSelected());
		p.setBooleanValue(OPEN_LAST_FILE, openLastFileCheckBox.isSelected());

		p.setIntValue(ACTIVATION_TIME, defaultActivationTimeModel.getNumber()
				.intValue());
		p.setIntValue(ROUTING_DELAY, defaultRoutingDelayModel.getNumber()
				.intValue());
		p.setIntValue(LOCK_DURATION, defaultLockDurationModel.getNumber()
				.intValue());
		p.setBooleanValue(INTERFACE_6051, interface6051.isSelected());

		p.setStringValue(HOSTNAME, (String) hostnameTextField.getText());
		p.setIntValue(PORT, Integer.parseInt(portnumberTextField.getText()));
		p.setBooleanValue(AUTOCONNECT, autoconnectCheckBox.isSelected());

		p.setBooleanValue(USE_DATABASE, useDatabaseCheckBox.isSelected());
		p.setStringValue(DATABASE_HOST, (String) databaseHostField.getText());
		p.setStringValue(DATABASE_NAME, (String) databaseNameField.getText());
		p.setStringValue(DATABASE_USER, (String) databaseUserField.getText());
		p
				.setStringValue(DATABASE_PWD, (String) databasePasswordField
						.getText());
		try {
			p.save();
		} catch (FileNotFoundException e) {
			ExceptionProcessor.getInstance().processException(e);
		} catch (IOException e) {
			ExceptionProcessor.getInstance().processException(e);
		}
	}

}
