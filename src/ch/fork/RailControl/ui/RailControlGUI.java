package ch.fork.RailControl.ui;

/*------------------------------------------------------------------------
 * 
 * <src/RailControlGUI.java>  -  <desc>
 * 
 * begin     : Sun May 15 13:16:56 CEST 2005
 * copyright : (C)  by Benjamin Mueller 
 * email     : akula@akula.ch
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.ui.switches.SwitchConfigurationDialog;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class RailControlGUI extends JFrame
		implements
			CommandDataListener,
			InfoDataListener {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "RailControl";

	private SRCPSession session;
	private String typedChars = "";

	// GUI-Components
	private JTabbedPane trackSwitchPane;
	private JPanel controlPanel;

	private JPanel statusBarPanel;
	private JComboBox commandHistory;
	private DefaultComboBoxModel commandHistoryModel;
	private PreferencesDialog preferencesDialog;
	private Preferences preferences;

	// Datastructures
	private List<SwitchGroup> switchGroups;
	private JMenuItem daemonConnectItem;
	private JMenuItem daemonDisconnectItem;
	private JMenuItem daemonResetItem;

	public RailControlGUI() {
		super(NAME);
		initGUI();
		preferencesDialog = new PreferencesDialog(this);
		initDatastructures();
	}

	private void initDatastructures() {
		switchGroups = new ArrayList<SwitchGroup>();
		switchGroups.add(new SwitchGroup("Main Line"));
		switchGroups.add(new SwitchGroup("Mountain"));
		preferences = new Preferences();
	}

	private void initGUI() {
		setFont(new Font("Verdana", Font.PLAIN, 19));
		initSwitchPanel();
		initLocomotiveControl();
		initMenu();
		initStatusBar();
		preferencesDialog = new PreferencesDialog(RailControlGUI.this);

		BorderLayout centerLayout = new BorderLayout();
		JPanel center = new JPanel();
		center.setLayout(centerLayout);
		center.add(trackSwitchPane, BorderLayout.CENTER);
		center.add(controlPanel, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		add(statusBarPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		setVisible(true);
		updateCommandHistory("RailControl started");
	}

	private void initSwitchPanel() {
		trackSwitchPane = new JTabbedPane(JTabbedPane.BOTTOM);
	}

	private void initLocomotiveControl() {
		controlPanel = new JPanel();
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		controlPanel.setLayout(controlPanelLayout);
	}

	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();

		/* FILE */
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		JMenuItem saveItem = new JMenuItem("Save");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(quitItem);

		/* EDIT */
		JMenu edit = new JMenu("Edit");
		JMenuItem switchesItem = new JMenuItem("Switches");
		JMenuItem locomotivesItem = new JMenuItem("Locomotives");
		JMenuItem preferencesItem = new JMenuItem("Preferences");

		switchesItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				SwitchConfigurationDialog switchConfig = new SwitchConfigurationDialog(
						RailControlGUI.this, preferences, switchGroups);
			}
		});

		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences = preferencesDialog.editPreferences(preferences);
				updateCommandHistory("Preferences updated");
			}
		});

		edit.add(switchesItem);
		edit.add(locomotivesItem);
		edit.add(new JSeparator());
		edit.add(preferencesItem);

		/* DAEMON */
		JMenu daemonMenu = new JMenu("Daemon");
		daemonConnectItem = new JMenuItem("Connect");
		daemonDisconnectItem = new JMenuItem("Disconnect");
		daemonResetItem = new JMenuItem("Reset");
		daemonDisconnectItem.setEnabled(false);
		daemonResetItem.setEnabled(false);
		daemonConnectItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					session = new SRCPSession(preferences.getHostname(),
							preferences.getPortnumber());
					session.getCommandChannel().addCommandDataListener(
							RailControlGUI.this);
					session.getInfoChannel().addInfoDataListener(
							RailControlGUI.this);
					session.connect();
					daemonConnectItem.setEnabled(false);
					daemonDisconnectItem.setEnabled(true);
					daemonResetItem.setEnabled(true);
				} catch (SRCPException e1) {
					processException(e1);
				}
			}

		});
		daemonMenu.add(daemonConnectItem);
		daemonMenu.add(daemonDisconnectItem);
		daemonMenu.add(new JSeparator());
		daemonMenu.add(daemonResetItem);

		/* HELP */
		JMenu helpMenu = new JMenu("Help");

		menuBar.add(fileMenu);
		menuBar.add(edit);
		menuBar.add(daemonMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
	}

	private void initStatusBar() {
		statusBarPanel = new JPanel();
		commandHistoryModel = new DefaultComboBoxModel();
		commandHistory = new JComboBox(commandHistoryModel);
		commandHistory.setEditable(false);
		statusBarPanel.setLayout(new BorderLayout());
		statusBarPanel.add(commandHistory, BorderLayout.SOUTH);

	}

	public void updateCommandHistory(String text) {
		DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
		String date = df.format(GregorianCalendar.getInstance().getTime());
		commandHistoryModel.insertElementAt("[" + date + "]: " + text, 0);
		commandHistory.setSelectedIndex(0);
	}

	private void processException(Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), "Error occured",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		new RailControlGUI();
	}

	public void commandDataSent(String commandData) {
		updateCommandHistory("To Server: " + commandData);
	}

	public void infoDataReceived(String infoData) {
		updateCommandHistory("From Server: " + infoData);

	}

}
