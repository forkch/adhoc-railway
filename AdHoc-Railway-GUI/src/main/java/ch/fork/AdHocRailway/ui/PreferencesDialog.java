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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.controllers.RailwayDevice;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainController;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PreferencesDialog extends JDialog implements PreferencesKeys {
	/**
     *
     */
	private static final long serialVersionUID = 6559383494970215298L;
	private JSpinner defaultRoutingDelay;
	private SpinnerNumberModel defaultActivationTimeModel;
	private SpinnerNumberModel defaultRoutingDelayModel;
	private JSpinner locomotiveControlNumber;
	private JSpinner defaultTurnoutBus;
	private JSpinner defaultLocomotiveBus;
	private SpinnerNumberModel locomotiveControlNumberModel;
	private JTextField srcpHostnameTextField;
	private JTextField srcpPortnumberTextField;
	private JComboBox<String> keyBoardLayoutComboBox;
	private JCheckBox writeLog;
	private JCheckBox fullscreen;
	private JCheckBox tabbedTrackCheckBox;

	private boolean okPressed;
	private boolean cancelPressed;
	private JCheckBox srcpAutoconnectCheckBox;
	private JTextField adHocServerHostField;
	private JTextField adHocServerCollectionField;
	private JCheckBox useAdHocServerCheckBox;
	private JCheckBox openLastFileCheckBox;
	private SpinnerNumberModel defaultTurnoutBusModel;
	private SpinnerNumberModel defaultLocomotiveBusModel;
	private SpinnerNumberModel numberOfBoostersModel;
	private JSpinner numberOfBoosters;
	private JSpinner adHocServerPortField;
	private SpinnerNumberModel adHocServerPortModel;
	private JCheckBox autoDiscoverAndConnectServersCheckBox;
	private final ApplicationContext ctx;
	private JComboBox<RailwayDevice> adhocDeviceComboBox;
	private JComboBox<String> adHocBrainPort;

	public PreferencesDialog(final JFrame owner, final ApplicationContext ctx) {
		super(owner, "Preferences", true);
		this.ctx = ctx;
		initGUI();
	}

	private void initGUI() {

		final FormLayout layout = new FormLayout(
				"5dlu, pref, 10dlu, pref, 5dlu",
				"5dlu, pref, 3dlu, top:pref,3dlu, pref, 3dlu, top:pref, 3dlu, pref, 5dlu");

		layout.setColumnGroups(new int[][] { { 2, 4 } });

		final PanelBuilder builder = new PanelBuilder(layout);
		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xy(2, 2));
		builder.add(createGUISettingsTab(), cc.xy(2, 4));

		builder.addSeparator("Digital", cc.xy(4, 2));
		builder.add(createDigitalDataTab(), cc.xy(4, 4));

		builder.addSeparator("SRCP-Server", cc.xy(2, 6));
		builder.add(createSrcpServerTab(), cc.xy(2, 8));

		builder.addSeparator("AdHoc-Server", cc.xy(4, 6));
		builder.add(createAdHocServerTab(), cc.xy(4, 8));

		final JButton okButton = new JButton("OK",
				ImageTools.createImageIconFromIconSet("dialog-ok-apply.png"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				okPressed = true;

				JOptionPane
						.showMessageDialog(
								PreferencesDialog.this,
								"Please restart application for the changes to have effect",
								"Please restart",
								JOptionPane.INFORMATION_MESSAGE);
				PreferencesDialog.this.setVisible(false);

				savePreferences();
			}
		});
		final JButton cancelButton = new JButton("Cancel",
				ImageTools.createImageIconFromIconSet("dialog-cancel.png"));
		cancelPressed = false;
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				cancelPressed = true;
				PreferencesDialog.this.setVisible(false);
			}
		});

		builder.add(
				ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton),
				cc.xyw(2, 10, 3));

		add(builder.getPanel());

		loadPreferences();
		pack();
		setLocationRelativeTo(getParent());
		SwingUtils.addEscapeListener(this);
		setVisible(true);
	}

	private JPanel createGUISettingsTab() {

		adhocDeviceComboBox = new JComboBox<RailwayDevice>();
		for (final RailwayDevice device : RailwayDevice.values()) {
			adhocDeviceComboBox.addItem(device);
		}

		adHocBrainPort = new JComboBox<String>();

		if (ctx.getRailwayDeviceManager().isBrainAvailable()) {
			for (final String availablePort : BrainController.getInstance()
					.getAvailableSerialPortsAsString()) {
				adHocBrainPort.addItem(availablePort);
			}
		}

		locomotiveControlNumberModel = new SpinnerNumberModel(5, 1, 10, 1);
		locomotiveControlNumber = new JSpinner(locomotiveControlNumberModel);

		keyBoardLayoutComboBox = new JComboBox<String>();
		final Set<String> sortedLayoutNames = new TreeSet<String>(Preferences
				.getInstance().getKeyBoardLayoutNames());
		for (final String name : sortedLayoutNames) {
			keyBoardLayoutComboBox.addItem(name);
		}

		writeLog = new JCheckBox();
		fullscreen = new JCheckBox();
		tabbedTrackCheckBox = new JCheckBox();
		openLastFileCheckBox = new JCheckBox();

		autoDiscoverAndConnectServersCheckBox = new JCheckBox();
		autoDiscoverAndConnectServersCheckBox
				.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						final boolean en = autoDiscoverAndConnectServersCheckBox
								.isSelected();
						enableDisableNetworking(en);
					}
				});

		final JPanel p = new JPanel(new MigLayout("wrap 2"));
		p.add(new JLabel("Railway device"));
		p.add(adhocDeviceComboBox);
		p.add(new JLabel("AdHoc-Brain port"));
		p.add(adHocBrainPort, "growx");
		p.add(new JLabel("Locomotive Controls"));
		p.add(locomotiveControlNumber);
		p.add(new JLabel("Keyboard-Layout"));
		p.add(keyBoardLayoutComboBox);
		p.add(new JLabel("Write Log"));
		p.add(writeLog);
		p.add(new JLabel("Tabbed Track-Control"));
		p.add(tabbedTrackCheckBox);
		p.add(new JLabel("Open last file"));
		p.add(openLastFileCheckBox);
		p.add(new JLabel("Auto Discover and Connect"));
		p.add(autoDiscoverAndConnectServersCheckBox);
		return p;
	}

	private JPanel createDigitalDataTab() {

		numberOfBoostersModel = new SpinnerNumberModel(1, 0, 32, 1);
		numberOfBoosters = new JSpinner(numberOfBoostersModel);

		defaultActivationTimeModel = new SpinnerNumberModel(50, 50, 1000, 10);

		defaultRoutingDelayModel = new SpinnerNumberModel(250, 10, 10000000, 10);
		defaultRoutingDelay = new JSpinner(defaultRoutingDelayModel);

		defaultTurnoutBusModel = new SpinnerNumberModel(0, 0, 60, 1);
		defaultTurnoutBus = new JSpinner(defaultTurnoutBusModel);
		defaultLocomotiveBusModel = new SpinnerNumberModel(0, 0, 60, 1);
		defaultLocomotiveBus = new JSpinner(defaultLocomotiveBusModel);

		final JPanel p = new JPanel(new MigLayout("wrap 2"));
		p.add(new JLabel("Number of Boosters"));
		p.add(numberOfBoosters);

		p.add(new JLabel("Default Turnout Bus"));
		p.add(defaultTurnoutBus);

		p.add(new JLabel("Default Locomotive Bus"));
		p.add(defaultLocomotiveBus);

		p.add(new JLabel("Routing delay [ms]"));
		p.add(defaultRoutingDelay);

		return p;
	}

	private JPanel createSrcpServerTab() {

		srcpHostnameTextField = new JTextField(15);

		srcpPortnumberTextField = new JTextField("12345", 15);

		srcpAutoconnectCheckBox = new JCheckBox();

		final JPanel p = new JPanel(new MigLayout("wrap 2"));
		p.add(new JLabel("Autoconnect"));
		p.add(srcpAutoconnectCheckBox);
		p.add(new JLabel("Hostname (Name or IP)"));
		p.add(srcpHostnameTextField);
		p.add(new JLabel("Portnumber (e.g. 4303)"));
		p.add(srcpPortnumberTextField);

		return p;
	}

	private JPanel createAdHocServerTab() {

		useAdHocServerCheckBox = new JCheckBox();

		adHocServerHostField = new JTextField(15);

		adHocServerPortModel = new SpinnerNumberModel(3000, 1025, 65535, 1);
		adHocServerPortField = new JSpinner(adHocServerPortModel);

		adHocServerCollectionField = new JTextField(15);

		final JPanel p = new JPanel(new MigLayout("wrap 2"));

		p.add(new JLabel("Use AdHoc-Server"));
		p.add(useAdHocServerCheckBox);
		p.add(new JLabel("Host"));
		p.add(adHocServerHostField);
		p.add(new JLabel("Port"));
		p.add(adHocServerPortField);
		p.add(new JLabel("Collection"));
		p.add(adHocServerCollectionField);

		return p;
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	private void loadPreferences() {
		final Preferences p = Preferences.getInstance();
		adhocDeviceComboBox.setSelectedItem(RailwayDevice.fromString(p
				.getStringValue(RAILWAY_DEVICE)));

		adHocBrainPort.setSelectedItem(p.getStringValue(ADHOC_BRAIN_PORT));
		locomotiveControlNumberModel.setValue(p
				.getIntValue(LOCOMOTIVE_CONTROLES));
		keyBoardLayoutComboBox.setSelectedItem(p
				.getStringValue(KEYBOARD_LAYOUT));
		writeLog.setSelected(p.getBooleanValue(LOGGING));
		fullscreen.setSelected(p.getBooleanValue(FULLSCREEN));
		tabbedTrackCheckBox.setSelected(p.getBooleanValue(TABBED_TRACK));
		openLastFileCheckBox.setSelected(p.getBooleanValue(OPEN_LAST_FILE));

		autoDiscoverAndConnectServersCheckBox.setSelected(p
				.getBooleanValue(AUTO_DISCOVER_AND_CONNECT_SERVERS));

		numberOfBoostersModel.setValue(p.getIntValue(NUMBER_OF_BOOSTERS));
		defaultTurnoutBusModel.setValue(p.getIntValue(DEFAULT_TURNOUT_BUS));
		defaultLocomotiveBusModel.setValue(p
				.getIntValue(DEFAULT_LOCOMOTIVE_BUS));
		defaultActivationTimeModel.setValue(p.getIntValue(ACTIVATION_TIME));
		defaultRoutingDelayModel.setValue(p.getIntValue(ROUTING_DELAY));

		srcpHostnameTextField.setText(p.getStringValue(SRCP_HOSTNAME));
		srcpPortnumberTextField.setText(Integer.toString(p
				.getIntValue(SRCP_PORT)));
		srcpAutoconnectCheckBox
				.setSelected(p.getBooleanValue(SRCP_AUTOCONNECT));

		useAdHocServerCheckBox.setSelected(p.getBooleanValue(USE_ADHOC_SERVER));
		adHocServerHostField.setText(p.getStringValue(ADHOC_SERVER_HOSTNAME));
		adHocServerPortField.setValue(p.getIntValue(ADHOC_SERVER_PORT));
		adHocServerCollectionField.setText(p
				.getStringValue(ADHOC_SERVER_COLLECTION));

		enableDisableNetworking(autoDiscoverAndConnectServersCheckBox
				.isSelected());

		if (ctx.getRailwayDeviceManager().isBrainAvailable()) {
			adHocBrainPort.setEnabled(true);
		} else {
			adhocDeviceComboBox.setSelectedItem(RailwayDevice.SRCP);
			adHocBrainPort.setEnabled(false);
		}

	}

	public void savePreferences() {
		final Preferences p = Preferences.getInstance();
		p.setStringValue(RAILWAY_DEVICE, adhocDeviceComboBox.getSelectedItem()
				.toString());

		p.setStringValue(ADHOC_BRAIN_PORT,
				(String) adHocBrainPort.getSelectedItem());
		p.setIntValue(LOCOMOTIVE_CONTROLES, locomotiveControlNumberModel
				.getNumber().intValue());
		p.setStringValue(KEYBOARD_LAYOUT, keyBoardLayoutComboBox
				.getSelectedItem().toString());
		p.setBooleanValue(LOGGING, writeLog.isSelected());
		p.setBooleanValue(FULLSCREEN, fullscreen.isSelected());
		p.setBooleanValue(TABBED_TRACK, tabbedTrackCheckBox.isSelected());
		p.setBooleanValue(OPEN_LAST_FILE, openLastFileCheckBox.isSelected());
		p.setBooleanValue(AUTO_DISCOVER_AND_CONNECT_SERVERS,
				autoDiscoverAndConnectServersCheckBox.isSelected());

		p.setIntValue(NUMBER_OF_BOOSTERS, numberOfBoostersModel.getNumber()
				.intValue());
		p.setIntValue(DEFAULT_TURNOUT_BUS, defaultTurnoutBusModel.getNumber()
				.intValue());
		p.setIntValue(DEFAULT_LOCOMOTIVE_BUS, defaultLocomotiveBusModel
				.getNumber().intValue());
		p.setIntValue(ACTIVATION_TIME, defaultActivationTimeModel.getNumber()
				.intValue());
		p.setIntValue(ROUTING_DELAY, defaultRoutingDelayModel.getNumber()
				.intValue());

		p.setStringValue(SRCP_HOSTNAME, srcpHostnameTextField.getText());
		p.setIntValue(SRCP_PORT,
				Integer.parseInt(srcpPortnumberTextField.getText()));
		p.setBooleanValue(SRCP_AUTOCONNECT,
				srcpAutoconnectCheckBox.isSelected());

		p.setBooleanValue(USE_ADHOC_SERVER, useAdHocServerCheckBox.isSelected());
		p.setStringValue(ADHOC_SERVER_HOSTNAME, adHocServerHostField.getText());
		p.setIntValue(ADHOC_SERVER_PORT, adHocServerPortModel.getNumber()
				.intValue());
		p.setStringValue(ADHOC_SERVER_COLLECTION,
				adHocServerCollectionField.getText());
		try {
			p.save();
		} catch (final FileNotFoundException e) {
			ctx.getMainApp().handleException(e);
		} catch (final IOException e) {
			ctx.getMainApp().handleException(e);
		}
	}

	private void enableDisableNetworking(final boolean en) {
		srcpAutoconnectCheckBox.setEnabled(!en);
		srcpHostnameTextField.setEnabled(!en);
		srcpPortnumberTextField.setEnabled(!en);

		useAdHocServerCheckBox.setEnabled(!en);
		adHocServerHostField.setEnabled(!en);
		adHocServerPortField.setEnabled(!en);
	}
}
