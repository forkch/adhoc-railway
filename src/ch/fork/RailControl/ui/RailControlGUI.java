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

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.ui.switches.SwitchConfigurationDialog;
import ch.fork.RailControl.ui.switches.SwitchGroupPane;
import ch.fork.RailControl.ui.switches.SwitchGroupTab;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class RailControlGUI extends JFrame implements CommandDataListener,
		InfoDataListener {

	private static final long serialVersionUID = 1L;

	private static final String NAME = "RailControl";

	private SRCPSession session;

	private String typedChars = "";

	// GUI-Components
	private SwitchGroupPane switchGroupPane;

	private JPanel locomotiveControlPanel;

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

	private StringBuffer enteredNumberKeys;

	public RailControlGUI() {
		super(NAME);
		initGUI();
		initKeyboardActions();
		preferencesDialog = new PreferencesDialog(this);
		initDatastructures();
	}

	private void initDatastructures() {
		enteredNumberKeys = new StringBuffer();
		switchGroups = new ArrayList<SwitchGroup>();
		SwitchGroup main = new SwitchGroup("Main Line");
		SwitchGroup mountain = new SwitchGroup("Mountain Line");
		switchGroups.add(main);
		switchGroups.add(mountain);
		/*
		 * int i = 1; Random rnd = new Random(); for (; i <= 30; i++) { Switch s =
		 * null; switch (rnd.nextInt(3)) { case 0 : s = new DefaultSwitch(i, "HB " +
		 * i, 1, new Address(i)); break; case 1 : s = new DoubleCrossSwitch(i,
		 * "HB " + i, 1, new Address(i)); break; case 2 : s = new
		 * ThreeWaySwitch(i, "HB " + i, 1, new Address(i, i + 1)); i++; break; }
		 * main.addSwitch(s); } for (; i <= 60; i++) { Switch s = null; switch
		 * (rnd.nextInt(3)) { case 0 : s = new DefaultSwitch(i, "HB " + i, 1,
		 * new Address(i)); break; case 1 : s = new DoubleCrossSwitch(i, "HB " +
		 * i, 1, new Address(i)); break; case 2 : s = new ThreeWaySwitch(i, "HB " +
		 * i, 1, new Address(i, i + 1)); i++; break; } mountain.addSwitch(s); }
		 */

		/*
		 * Switch switch1 = new DefaultSwitch(1, "HB 1", 1, new Address(1));
		 * Switch switch2 = new DefaultSwitch(2, "SW 1", 1, new Address(2));
		 * Switch switch3 = new ThreeWaySwitch(3, "HB 2", 1, new Address(3, 4));
		 * Switch switch4 = new DefaultSwitch(4, "HB 3", 1, new Address(5));
		 * Switch switch5 = new DoubleCrossSwitch(5, "Berg1", 1, new
		 * Address(6)); Switch switch6 = new DefaultSwitch(6, "Berg2", 1, new
		 * Address(7)); main.addSwitch(switch1); main.addSwitch(switch2);
		 * main.addSwitch(switch3); main.addSwitch(switch4);
		 * mountain.addSwitch(switch5); mountain.addSwitch(switch6);
		 */
		Switch switch1 = new DefaultSwitch(1, "HB 1", 1, new Address(1));
		Switch switch2 = new DoubleCrossSwitch(2, "Berg1", 1, new Address(2));
		Switch switch3 = new ThreeWaySwitch(3, "HB 2", 1, new Address(3, 4));
		main.addSwitch(switch1);
		main.addSwitch(switch2);
		main.addSwitch(switch3);

		switchGroupPane.update(switchGroups);
		preferences = new Preferences();
	}

	private void initGUI() {
		setFont(new Font("Verdana", Font.PLAIN, 19));
		ExceptionProcessor.getInstance(this);
		switchGroupPane = initSwitchGroupPane();
		locomotiveControlPanel = initLocomotiveControl();
		initToolbar();
		initMenu();
		initStatusBar();
		preferencesDialog = new PreferencesDialog(RailControlGUI.this);

		BorderLayout centerLayout = new BorderLayout();
		JPanel center = new JPanel();
		center.setLayout(centerLayout);
		center.add(switchGroupPane, BorderLayout.CENTER);
		center.add(locomotiveControlPanel, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		add(statusBarPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		setVisible(true);
		updateCommandHistory("RailControl started");
	}

	private SwitchGroupPane initSwitchGroupPane() {
		SwitchGroupPane pane = new SwitchGroupPane(switchGroups);
		return pane;
	}

	private JPanel initLocomotiveControl() {
		JPanel panel = new JPanel();
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		panel.setLayout(controlPanelLayout);
		return panel;
	}

	private void initToolbar() {

	}

	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();

		/* FILE */
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		openItem.setIcon(createImageIcon("icons/fileopen.png", "File open...",
				this));
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(RailControlGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					// This is where a real application would open the file.
					updateCommandHistory("Opening: " + file.getName());
				} else {
					updateCommandHistory("Open command cancelled by user");
				}
			}
		});

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showSaveDialog(RailControlGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					// This is where a real application would open the file.
					updateCommandHistory("Saving: " + file.getName());
				} else {
					updateCommandHistory("Save command cancelled by user");
				}
			}
		});
		saveItem.setIcon(createImageIcon("icons/filesave.png", "Save file...",
				this));
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setIcon(createImageIcon("icons/exit.png", "Exit", this));
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);

		/* EDIT */
		JMenu edit = new JMenu("Edit");
		JMenuItem switchesItem = new JMenuItem("Switches");
		switchesItem.setIcon(createImageIcon("icons/switch.png", "Connect",
				this));
		JMenuItem locomotivesItem = new JMenuItem("Locomotives");
		JMenuItem preferencesItem = new JMenuItem("Preferences");
		preferencesItem.setIcon(createImageIcon("icons/package_settings.png",
				"Exit", this));

		switchesItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				SwitchConfigurationDialog switchConfig = new SwitchConfigurationDialog(
						RailControlGUI.this, preferences, switchGroups);
				switchGroupPane.update(switchGroups);
			}
		});

		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences = preferencesDialog.editPreferences(preferences);
				if (preferencesDialog.isOkPressed()) {
					updateCommandHistory("Preferences updated");
				}
			}
		});

		edit.add(switchesItem);
		edit.add(locomotivesItem);
		edit.add(new JSeparator());
		edit.add(preferencesItem);

		/* DAEMON */
		JMenu daemonMenu = new JMenu("Daemon");
		daemonConnectItem = new JMenuItem("Connect");

		daemonConnectItem.setIcon(createImageIcon("icons/daemonconnect.png",
				"Connect", this));

		daemonDisconnectItem = new JMenuItem("Disconnect");
		daemonDisconnectItem.setIcon(createImageIcon(
				"icons/daemondisconnect.png", "Disconnect", this));
		daemonResetItem = new JMenuItem("Reset");
		daemonResetItem.setIcon(createImageIcon("icons/daemonreset.png",
				"Connect", this));
		daemonDisconnectItem.setEnabled(false);
		daemonResetItem.setEnabled(false);
		daemonConnectItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					session = new SRCPSession(preferences.getHostname(),
							preferences.getPortnumber(), false);
					SwitchControl.getInstance().setSession(session);
					session.getCommandChannel().addCommandDataListener(
							RailControlGUI.this);
					session.getInfoChannel().addInfoDataListener(
							RailControlGUI.this);
					session.connect();
					for (SwitchGroup sg : switchGroups) {
						for (Switch s : sg.getSwitches().values()) {
							s.setSession(session);
						}
					}

					daemonConnectItem.setEnabled(false);
					daemonDisconnectItem.setEnabled(true);
					daemonResetItem.setEnabled(true);
				} catch (SRCPException e1) {
					processException(e1);
				}
			}
		});

		daemonDisconnectItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					session.disconnect();

					daemonConnectItem.setEnabled(true);
					daemonDisconnectItem.setEnabled(false);
					daemonResetItem.setEnabled(false);
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

	private void initKeyboardActions() {

		for (int i = 0; i <= 10; i++) {
			switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(Integer.toString(i)), "numberKey");

			switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("NUMPAD" + Integer.toString(i)),
					"numberKey");
		}
		switchGroupPane.getActionMap().put("numberKey", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				enteredNumberKeys.append(e.getActionCommand());
			}
		});
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("ENTER"), "enter");
		switchGroupPane.getActionMap().put("enter", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (enteredNumberKeys.toString().equals("")) {
					return;
				}
				String switchNumberAsString = enteredNumberKeys.toString();
				int switchNumber = Integer.parseInt(switchNumberAsString);
				SwitchGroupTab selectedTab = (SwitchGroupTab) (switchGroupPane
						.getSelectedComponent());
				int switchGroupIndex = switchGroups.indexOf(selectedTab
						.getSwitchGroup());
				Switch aSwitch = switchGroups.get(switchGroupIndex)
						.getSwitches().get(switchNumber);
				if (aSwitch == null) {
					return;
				}
				try {
					if (!aSwitch.isInitialized()) {
						aSwitch.init();
					}
					SwitchControl.getInstance().setStraight(aSwitch);
					enteredNumberKeys = new StringBuffer();
				} catch (SwitchException e1) {
					processException(e1);
				}
			}
		});
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "plus");
		switchGroupPane.getActionMap().put("plus", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (enteredNumberKeys.toString().equals("")) {
					return;
				}
				String switchNumberAsString = enteredNumberKeys.toString();
				int switchNumber = Integer.parseInt(switchNumberAsString);
				System.out.println(switchNumber);
				enteredNumberKeys = new StringBuffer();
				SwitchGroupTab selectedTab = (SwitchGroupTab) (switchGroupPane
						.getSelectedComponent());
				int switchGroupIndex = switchGroups.indexOf(selectedTab
						.getSwitchGroup());
				Switch aSwitch = switchGroups.get(switchGroupIndex)
						.getSwitches().get(switchNumber);
				if (aSwitch == null) {
					return;
				}
				try {
					if (!aSwitch.isInitialized()) {
						aSwitch.init();
					}
					SwitchControl.getInstance().setCurvedLeft(aSwitch);
					enteredNumberKeys = new StringBuffer();
				} catch (SwitchException e1) {
					processException(e1);
				}
			}
		});
	}

	public void processException(Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), "Error occured",
				JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}

	public static void main(String[] args) {
		try {
			// UIManager.setLookAndFeel(
			// "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

		} catch (Exception e) {
			e.printStackTrace();
		}
		new RailControlGUI();
	}

	public void commandDataSent(String commandData) {
		updateCommandHistory("To Server: " + commandData);
	}

	public void infoDataReceived(String infoData) {
		updateCommandHistory("From Server: " + infoData);
	}
}
