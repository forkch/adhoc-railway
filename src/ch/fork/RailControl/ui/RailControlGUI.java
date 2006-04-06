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

import javax.swing.*;

import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchException;
import ch.fork.RailControl.ui.locomotives.LocomotiveControl;
import ch.fork.RailControl.ui.switches.SwitchTab;
import ch.fork.RailControl.ui.switches.SwitchWidget;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.common.exception.SRCPServerException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RailControlGUI extends JFrame implements InfoDataListener {

	private static final long serialVersionUID = 1L;

	private static final String NAME = "RailControl";

	private SRCPSession session;

	private String typedChars = "";

	// GUI-Components
	private JTabbedPane trackSwitchPane;

	private JPanel controlPanel;

	private SwitchTab mainLine;

	private SwitchTab mountainLine;

	private JLabel statusBar;

	// Menu
	private JMenuBar menuBar;

	private JMenu fileMenu;

	private JMenu helpMenu;

	private JMenuItem quitItem;

	public RailControlGUI() {
		super(NAME);
		try {
			session = new SRCPSession("titan", 12345);
		} catch (SRCPException e) {
			e.printStackTrace();
			System.exit(1);
		}
		initGUI();
	}

	private void initGUI() {
		setFont(new Font("Verdana", Font.PLAIN, 19));
		initTrackSwitchPanel();
		initControlPanel();
		initMenu();

		BorderLayout centerLayout = new BorderLayout();
		JPanel center = new JPanel();
		center.setLayout(centerLayout);
		center.add(trackSwitchPane, BorderLayout.CENTER);
		center.add(controlPanel, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		statusBar = new JLabel("Statusbar...");
		add(statusBar, BorderLayout.SOUTH);
		setSize(800, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	private void initTrackSwitchPanel() {
		mainLine = new SwitchTab("Main Line");
		Switch switch1 = null;
		try {
			switch1 = new DefaultSwitch(session, "Switch1", "Some desc", 1, 1);
		} catch (SRCPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SwitchWidget switchWidget1 = new SwitchWidget(switch1);
		mainLine.addSwitchWidget(switchWidget1);
		mountainLine = new SwitchTab("Mountain Line");
		trackSwitchPane = new JTabbedPane(JTabbedPane.BOTTOM);
		trackSwitchPane.addTab("Main Line", new JScrollPane(mainLine));
		trackSwitchPane.addTab("Mountain Line", new JScrollPane(mountainLine));
		this.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
				char typed = e.getKeyChar();
				typedChars += typed;
			}

			public void keyPressed(KeyEvent e) {

			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
					System.out.println(typedChars);
					try {
						SwitchControl.getInstance().toggle(
								new DefaultSwitch(session, "", "", Integer
										.parseInt(typedChars), 1));
					} catch (SwitchException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SRCPException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					typedChars = "";

				}
			}

		});
	}

	private void initControlPanel() {
		controlPanel = new JPanel();
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		controlPanel.setLayout(controlPanelLayout);
		LocomotiveControl loc1 = new LocomotiveControl("Big Boy");
		LocomotiveControl loc2 = new LocomotiveControl("Ascom");
		LocomotiveControl loc3 = new LocomotiveControl("Santa Fe");
		LocomotiveControl loc4 = new LocomotiveControl("Doppelschnauz");
		controlPanel.add(loc1);
		controlPanel.add(loc2);
		controlPanel.add(loc3);
		controlPanel.add(loc4);
	}

	private void initMenu() {
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(quitItem);
		helpMenu = new JMenu("Help");
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		new RailControlGUI();
	}

	public void infoDataReceived(String arg0) {
		System.out.println(arg0);

	}
}
