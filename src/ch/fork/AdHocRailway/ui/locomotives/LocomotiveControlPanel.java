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

package ch.fork.AdHocRailway.ui.locomotives;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.locking.SRCPLockControl;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.SimpleInternalFrame;

public class LocomotiveControlPanel extends JPanel {

	private LocomotiveControlface	locomotiveControl = AdHocRailway.getInstance().getLocomotiveControl();
	private int[][]					keyBindingsUS	= new int[][] {
			{ KeyEvent.VK_A, KeyEvent.VK_Z, KeyEvent.VK_Q },
			{ KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_W },
			{ KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_E },
			{ KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_R },
			{ KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_T },
			{ KeyEvent.VK_H, KeyEvent.VK_N, KeyEvent.VK_Y },
			{ KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_U },
			{ KeyEvent.VK_K, KeyEvent.VK_COMMA, KeyEvent.VK_I },
			{ KeyEvent.VK_L, KeyEvent.VK_DECIMAL, KeyEvent.VK_O },
			{ KeyEvent.VK_COLON, KeyEvent.VK_MINUS, KeyEvent.VK_P } };
	private int[][]					keyBindingsDE	= new int[][] {
			{ KeyEvent.VK_A, KeyEvent.VK_Y, KeyEvent.VK_Q },
			{ KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_W },
			{ KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_E },
			{ KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_R },
			{ KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_T },
			{ KeyEvent.VK_H, KeyEvent.VK_N, KeyEvent.VK_Z },
			{ KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_U },
			{ KeyEvent.VK_K, KeyEvent.VK_COMMA, KeyEvent.VK_I },
			{ KeyEvent.VK_L, KeyEvent.VK_DECIMAL, KeyEvent.VK_O },
			{ KeyEvent.VK_COLON, KeyEvent.VK_MINUS, KeyEvent.VK_P } };
	private int[][]					keyBindings		= keyBindingsDE;
	private List<LocomotiveWidget>	locomotiveWidgets;
	private JPanel					controlPanel;

	public LocomotiveControlPanel() {
		super();
		locomotiveWidgets = new ArrayList<LocomotiveWidget>();
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.CENTER, 10, 0);
		controlPanel = new JPanel(controlPanelLayout);
		controlPanel.setLayout(controlPanelLayout);
		SimpleInternalFrame locomotivesFrame = new SimpleInternalFrame("Trains");
		locomotivesFrame.add(controlPanel, BorderLayout.CENTER);
		add(locomotivesFrame, BorderLayout.NORTH);
		registerKeyboardAction(new LocomotiveStopAction(), "", KeyStroke
				.getKeyStroke(KeyEvent.VK_SPACE, 0), WHEN_IN_FOCUSED_WINDOW);
	}

	public void update() {
		SRCPLockControl lockc = SRCPLockControl.getInstance();
		locomotiveControl.removeAllLocomotiveChangeListener();
		lockc.removeAllLockChangeListener();

		controlPanel.removeAll();
		locomotiveWidgets.clear();
		if (Preferences.getInstance().getStringValue(
				PreferencesKeys.KEYBOARD_LAYOUT).equals("Swiss German")) {
			keyBindings = keyBindingsDE;
		} else {
			keyBindings = keyBindingsUS;
		}
		for (int i = 0; i < Preferences.getInstance().getIntValue(
				PreferencesKeys.LOCOMOTIVE_CONTROLES); i++) {
			LocomotiveWidget w = new LocomotiveWidget(keyBindings[i][0],
					keyBindings[i][1], keyBindings[i][2], AdHocRailway
							.getInstance());
			SRCPLockControl.getInstance().addLockChangeListener(w);
			w.updateLocomotiveGroups();
			controlPanel.add(w);
			locomotiveWidgets.add(w);
		}
		revalidate();
		repaint();
	}

	private class LocomotiveStopAction extends AbstractAction implements
			Runnable {
		public void actionPerformed(ActionEvent e) {
			Thread t = new Thread(this);
			t.start();
		}

		public void run() {
			try {
				for (LocomotiveWidget widget : locomotiveWidgets) {
					Locomotive myLocomotive = widget.getMyLocomotive();
					if (myLocomotive == null)
						continue;
					locomotiveControl.setSpeed(myLocomotive,
							0, null);
					widget.updateWidget();
					Thread.sleep(200);
				}
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			} catch (InterruptedException e) {
				ExceptionProcessor.getInstance().processException(e);
			}
		}
	}
}
