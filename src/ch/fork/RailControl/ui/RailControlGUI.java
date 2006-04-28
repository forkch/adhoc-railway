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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.locomotives.DeltaLocomotive;
import ch.fork.RailControl.domain.locomotives.DigitalLocomotive;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveControl;
import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.ui.locomotives.LocomotiveControlPanel;
import ch.fork.RailControl.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.RailControl.ui.switches.DefaultSwitchCanvas;
import ch.fork.RailControl.ui.switches.DoubleCrossSwitchCanvas;
import ch.fork.RailControl.ui.switches.Segment7;
import ch.fork.RailControl.ui.switches.SwitchCanvas;
import ch.fork.RailControl.ui.switches.SwitchGroupPane;
import ch.fork.RailControl.ui.switches.ThreeWaySwitchCanvas;
import ch.fork.RailControl.ui.switches.configuration.SwitchConfigurationDialog;
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

	private LocomotiveControlPanel locomotiveControlPanel;

	private JPanel statusBarPanel;

	private JComboBox commandHistory;

	private DefaultComboBoxModel commandHistoryModel;

	// Datastructures
	private List<SwitchGroup> switchGroups;

	private JMenuItem daemonConnectItem;

	private JMenuItem daemonDisconnectItem;

	private JMenuItem daemonResetItem;

	private StringBuffer enteredNumberKeys;

	private List<Locomotive> locomotives;

	private Map<Integer, Switch> switches;

	private Segment7 seg1;

	private Segment7 seg2;

	private Segment7 seg3;

	private JPanel selectedSwitchDetails;

	public RailControlGUI() {
		super(NAME);
		initGUI();
		initKeyboardActions();
		initDatastructures();
	}

	private void initDatastructures() {
		locomotives = new ArrayList<Locomotive>();
		switches = new HashMap<Integer, Switch>();
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

		switches.put(switch1.getNumber(), switch1);
		switches.put(switch2.getNumber(), switch2);
		switches.put(switch3.getNumber(), switch3);

		Locomotive ascom = new DeltaLocomotive(session, "Ascom", 1, 24,
				"RE460 \"Ascom\"");
		Locomotive bigBoy = new DigitalLocomotive(session, "Big Boy", 1, 25,
				"UP Klasse 4000 \"Big Boy\"");
		locomotives.add(ascom);
		locomotives.add(bigBoy);
		LocomotiveControl.getInstance().registerLocomotives(locomotives);

		switchGroupPane.update(switchGroups);
		locomotiveControlPanel.update(locomotives);
	}

	private void initGUI() {
		setFont(new Font("Verdana", Font.PLAIN, 19));
		ExceptionProcessor.getInstance(this);
		switchGroupPane = initSwitchGroupPane();
		locomotiveControlPanel = initLocomotiveControl();
		initMenu();
		initToolbar();
		initStatusBar();

		JPanel center = new JPanel(new BorderLayout());
		center.add(switchGroupPane, BorderLayout.CENTER);
		JPanel centerSouth = new JPanel(new BorderLayout());

		centerSouth.add(locomotiveControlPanel, BorderLayout.WEST);

		JPanel segmentPanelNorth = new JPanel(new FlowLayout(
				FlowLayout.TRAILING, 2, 0));
		segmentPanelNorth.setBackground(new Color(127, 0, 0));
		seg1 = new Segment7();
		seg2 = new Segment7();
		seg3 = new Segment7();
		segmentPanelNorth.add(seg3);
		segmentPanelNorth.add(seg2);
		segmentPanelNorth.add(seg1);

		selectedSwitchDetails = new JPanel();

		JPanel segmentPanel = new JPanel(new BorderLayout());
		segmentPanel.add(segmentPanelNorth, BorderLayout.NORTH);
		segmentPanel.add(selectedSwitchDetails, BorderLayout.CENTER);

		centerSouth.add(segmentPanel, BorderLayout.EAST);

		center.add(centerSouth, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		add(statusBarPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000, 700);
		setVisible(true);
		updateCommandHistory("RailControl started");
	}

	private SwitchGroupPane initSwitchGroupPane() {
		SwitchGroupPane pane = new SwitchGroupPane(switchGroups);
		return pane;
	}

	private LocomotiveControlPanel initLocomotiveControl() {
		LocomotiveControlPanel locomotiveControlPanel = new LocomotiveControlPanel();
		return locomotiveControlPanel;
	}

	private void initToolbar() {
		JToolBar toolBar = new JToolBar();

		JButton openButton = new JButton(createImageIcon("icons/fileopen.png",
				"File open...", this));
		JButton saveButton = new JButton(createImageIcon("icons/filesave.png",
				"File open...", this));
		JButton switchesButton = new JButton(createImageIcon(
				"icons/switch.png", "Connect", this));
		JButton locomotivesButton = new JButton(createImageIcon(
				"icons/locomotive.png", "Exit", this));
		JButton preferencesButton = new JButton(createImageIcon(
				"icons/package_settings.png", "Exit", this));

		final JComboBox hostnamesComboBox = new JComboBox();
		for (String host : Preferences.getInstance().getHostnames()) {
			hostnamesComboBox.addItem(host);
		}
		hostnamesComboBox.setSelectedItem(Preferences.getInstance()
				.getHostname());
		hostnamesComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Preferences.getInstance().setHostname(
						hostnamesComboBox.getSelectedItem().toString());
			}

		});
		JButton connectButton = new JButton(createImageIcon(
				"icons/daemonconnect.png", "Connect", this));
		JButton disconnectButton = new JButton(createImageIcon(
				"icons/daemondisconnect.png", "Disconnect", this));

		openButton.addActionListener(new OpenAction());
		saveButton.addActionListener(new SaveAction());
		switchesButton.addActionListener(new SwitchesAction());
		locomotivesButton.addActionListener(new LocomotivesAction());
		preferencesButton.addActionListener(new PreferencesAction());
		connectButton.addActionListener(new ConnectAction());
		disconnectButton.addActionListener(new DisconnectAction());

		toolBar.add(openButton);
		toolBar.add(saveButton);
		toolBar.addSeparator();
		toolBar.add(switchesButton);
		toolBar.add(locomotivesButton);
		toolBar.add(preferencesButton);
		toolBar.addSeparator();
		toolBar.add(hostnamesComboBox);
		toolBar.add(connectButton);
		toolBar.add(disconnectButton);

		JPanel toolbarPanel = new JPanel(new BorderLayout());
		toolbarPanel.add(toolBar, BorderLayout.WEST);
		add(toolbarPanel, BorderLayout.PAGE_START);
	}

	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();

		/* FILE */
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		openItem.setIcon(createImageIcon("icons/fileopen.png", "File open...",
				this));
		openItem.addActionListener(new OpenAction());

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new SaveAction());
		saveItem.setIcon(createImageIcon("icons/filesave.png", "Save file...",
				this));
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setIcon(createImageIcon("icons/exit.png", "Exit", this));
		exitItem.addActionListener(new ExitAction());
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
		locomotivesItem.setIcon(createImageIcon("icons/locomotive.png", "Exit",
				this));
		JMenuItem preferencesItem = new JMenuItem("Preferences");
		preferencesItem.setIcon(createImageIcon("icons/package_settings.png",
				"Exit", this));

		switchesItem.addActionListener(new SwitchesAction());

		preferencesItem.addActionListener(new PreferencesAction());

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
		daemonConnectItem.addActionListener(new ConnectAction());

		daemonDisconnectItem.addActionListener(new DisconnectAction());
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

	private void initKeyboardActions() {
		for (int i = 0; i <= 10; i++) {
			switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(Integer.toString(i)), "numberKey");

			switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("NUMPAD" + Integer.toString(i)),
					"numberKey");
		}
		switchGroupPane.getActionMap().put("numberKey",
				new NumberEnteredAction());

		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("ENTER"), "switchingAction");
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "switchingAction");
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"switchingAction");
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0),
				"switchingAction");
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0),
				"switchingAction");
		switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
				"switchingAction");

		switchGroupPane.getActionMap().put("switchingAction",
				new SwitchingAction());

		for (int i = 1; i <= 12; i++) {
			switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("F" + Integer.toString(i)),
					"f" + Integer.toString(i));

			switchGroupPane.getActionMap().put("f" + Integer.toString(i),
					new SwitchGroupChangeAction(i - 1));
		}
	}

	public void processException(Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), "Error occured",
				JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
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

	public void updateCommandHistory(String text) {
		DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
		String date = df.format(GregorianCalendar.getInstance().getTime());
		String fullText = "[" + date + "]: " + text;
		SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));
	}

	private void resetSelectedSwitchDisplay() {
		enteredNumberKeys = new StringBuffer();
		seg1.setValue(0);
		seg2.setValue(0);
		seg3.setValue(0);
		seg1.repaint();
		seg2.repaint();
		seg3.repaint();
		selectedSwitchDetails.removeAll();
		selectedSwitchDetails.revalidate();
		selectedSwitchDetails.repaint();
	}

	private class CommandHistoryUpdater implements Runnable {

		private String text;

		public CommandHistoryUpdater(String text) {
			this.text = text;
		}

		public void run() {
			commandHistoryModel.insertElementAt(text, 0);
			commandHistory.setSelectedIndex(0);
		}
	}

	private class OpenAction extends AbstractAction {

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

	}

	private class SaveAction extends AbstractAction {

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

	}

	private class ExitAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}

	}

	private class SwitchesAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			SwitchConfigurationDialog switchConfig = new SwitchConfigurationDialog(
					RailControlGUI.this, Preferences.getInstance(),
					switches, switchGroups);
			if (switchConfig.isOkPressed()) {

			}
			System.out.println(switches);
			switchGroupPane.update(switchGroups);
		}
	}

	private class LocomotivesAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			LocomotiveConfigurationDialog locomotiveConfig = new LocomotiveConfigurationDialog(
					RailControlGUI.this, Preferences.getInstance(), locomotives);
			locomotiveControlPanel.update(locomotives);
		}
	}

	private class PreferencesAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

			PreferencesDialog p = new PreferencesDialog(RailControlGUI.this);
			p.editPreferences(Preferences.getInstance());
			if (p.isOkPressed()) {
				updateCommandHistory("Preferences updated");
				locomotiveControlPanel.update(locomotives);
			}
		}
	}

	private class ConnectAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			try {
				session = new SRCPSession(Preferences.getInstance()
						.getHostname(), Preferences.getInstance()
						.getPortnumber(), false);
				SwitchControl.getInstance().setSession(session);
				LocomotiveControl.getInstance().setSession(session);
				session.getCommandChannel().addCommandDataListener(
						RailControlGUI.this);
				session.getInfoChannel().addInfoDataListener(
						RailControlGUI.this);
				session.connect();
				for (SwitchGroup sg : switchGroups) {
					for (Switch s : sg.getSwitches()) {
						s.setSession(session);
					}
				}
				for (Locomotive l : locomotives) {
					l.setSession(session);
				}

				daemonConnectItem.setEnabled(false);
				daemonDisconnectItem.setEnabled(true);
				daemonResetItem.setEnabled(true);
			} catch (SRCPException e1) {
				processException(e1);
			}
		}

	}

	private class DisconnectAction extends AbstractAction {

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

	}

	private class NumberEnteredAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			enteredNumberKeys.append(e.getActionCommand());
			String switchNumberAsString = enteredNumberKeys.toString();
			int switchNumber = Integer.parseInt(switchNumberAsString);
			int origNumber = switchNumber;
			if (switchNumber > 999) {
				resetSelectedSwitchDisplay();
				return;
			}
			int seg1Value = switchNumber % 10;
			seg1.setValue(seg1Value);
			seg1.repaint();
			switchNumber = switchNumber - seg1Value;

			int seg2Value = (switchNumber % 100) / 10;
			seg2.setValue(seg2Value);
			seg2.repaint();
			switchNumber = switchNumber - seg2Value * 10;

			int seg3Value = (switchNumber % 1000) / 100;
			seg3.setValue(seg3Value);
			seg3.repaint();
			switchNumber = switchNumber - seg3Value * 100;

			Switch searchedSwitch = null;
			
			searchedSwitch = switches.get(origNumber);
			if (searchedSwitch == null) {
			//	resetSelectedSwitchDisplay();
				return;
			}
			selectedSwitchDetails.removeAll();
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			selectedSwitchDetails.setLayout(layout);

			gbc.insets = new Insets(5, 5, 5, 5);

			gbc.gridx = 0;
			JLabel numberLabel = new JLabel(Integer.toString(searchedSwitch
					.getNumber()));
			numberLabel.setFont(new Font("Dialog", Font.BOLD, 16));
			layout.setConstraints(numberLabel, gbc);
			selectedSwitchDetails.add(numberLabel);

			gbc.gridx = 1;
			JLabel descLabel = new JLabel(searchedSwitch.getDesc());
			layout.setConstraints(descLabel, gbc);
			selectedSwitchDetails.add(descLabel);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			SwitchCanvas sc = null;
			if (searchedSwitch instanceof DoubleCrossSwitch) {
				sc = new DoubleCrossSwitchCanvas(searchedSwitch);
			} else if (searchedSwitch instanceof DefaultSwitch) {
				sc = new DefaultSwitchCanvas(searchedSwitch);
			} else if (searchedSwitch instanceof ThreeWaySwitch) {
				sc = new ThreeWaySwitchCanvas(searchedSwitch);
			}

			layout.setConstraints(sc, gbc);
			selectedSwitchDetails.add(sc);
			sc.repaint();
			selectedSwitchDetails.revalidate();
			selectedSwitchDetails.repaint();
		}
	}

	private class SwitchingAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (enteredNumberKeys.toString().equals("")) {
				return;
			}
			String switchNumberAsString = enteredNumberKeys.toString();
			int switchNumber = Integer.parseInt(switchNumberAsString);
			Switch searchedSwitch = null;
			
			searchedSwitch = switches.get(switchNumber);
			if (searchedSwitch == null) {

				resetSelectedSwitchDisplay();
				return;
			}
			try {
				if (!searchedSwitch.isInitialized()) {
					searchedSwitch.init();
				}
				if (e.getActionCommand().equals("/")) {
					handleDivide(searchedSwitch);
				} else if (e.getActionCommand().equals("*")) {
					handleMultiply(searchedSwitch);
				} else if (e.getActionCommand().equals("-")) {
					handleMinus(searchedSwitch);
				} else if (e.getActionCommand().equals("+")) {
					if (!(searchedSwitch instanceof ThreeWaySwitch)) {
						handlePlus(searchedSwitch);
					}
				} else if (e.getActionCommand().equals("")) {
					if (!(searchedSwitch instanceof ThreeWaySwitch)) {
						handlePlus(searchedSwitch);
					}
				} else if (e.getActionCommand().equals("\n")) {
					handleEnter(searchedSwitch);
				}

				resetSelectedSwitchDisplay();
			} catch (SwitchException e1) {
				resetSelectedSwitchDisplay();
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void handleDivide(Switch aSwitch) throws SwitchException {
			SwitchControl.getInstance().setCurvedLeft(aSwitch);
		}

		private void handleMultiply(Switch aSwitch) throws SwitchException {
			SwitchControl.getInstance().setStraight(aSwitch);
		}

		private void handleMinus(Switch aSwitch) throws SwitchException {
			SwitchControl.getInstance().setCurvedRight(aSwitch);
		}

		private void handlePlus(Switch aSwitch) throws SwitchException {
			SwitchControl.getInstance().setCurvedLeft(aSwitch);
		}

		private void handleEnter(Switch aSwitch) throws SwitchException {
			SwitchControl.getInstance().setStraight(aSwitch);
		}
	}

	private class SwitchGroupChangeAction extends AbstractAction {

		private int selectedSwitchGroup;

		public SwitchGroupChangeAction(int selectedSwitchGroup) {
			this.selectedSwitchGroup = selectedSwitchGroup;
		}

		public void actionPerformed(ActionEvent e) {
			if (selectedSwitchGroup < switchGroups.size()) {
				switchGroupPane.setSelectedIndex(selectedSwitchGroup);
			}
		}

	}
}
