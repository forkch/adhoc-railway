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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.SimpleInternalFrame;

public class LocomotiveControlPanel extends JPanel {

	private final List<LocomotiveWidget> locomotiveWidgets;
	private JPanel controlPanel;

	public LocomotiveControlPanel() {
		super();
		locomotiveWidgets = new ArrayList<LocomotiveWidget>();
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);
		controlPanel = new JPanel(controlPanelLayout);
		controlPanel.setLayout(controlPanelLayout);

		SimpleInternalFrame locomotivesFrame = new SimpleInternalFrame("Trains");
		locomotivesFrame.add(controlPanel, BorderLayout.CENTER);
		add(locomotivesFrame, BorderLayout.NORTH);
		getActionMap().put("LocomotiveStop", new LocomotiveStopAction());
		Preferences
				.getInstance()
				.getKeyBoardLayout()
				.assignKeys(getInputMap(WHEN_IN_FOCUSED_WINDOW),
						"LocomotiveStop");
	}

	public void update() {
		LocomotiveControlface locomotiveControl = AdHocRailway.getInstance()
				.getLocomotiveControl();
		locomotiveControl.removeAllLocomotiveChangeListener();

		controlPanel.removeAll();
		locomotiveWidgets.clear();
		/*
		 * if (Preferences.getInstance().getStringValue(
		 * PreferencesKeys.KEYBOARD_LAYOUT).equals("Swiss German")) {
		 * keyBindings = keyBindingsDE; } else { keyBindings = keyBindingsUS; }
		 */
		for (int i = 0; i < Preferences.getInstance().getIntValue(
				PreferencesKeys.LOCOMOTIVE_CONTROLES); i++) {
			LocomotiveWidget w = new LocomotiveWidget(i,
					AdHocRailway.getInstance());
			w.updateLocomotiveGroups();
			controlPanel.add(w);
			locomotiveWidgets.add(w);
		}
		revalidate();
		repaint();
	}

	private class LocomotiveStopAction extends AbstractAction implements
			Runnable {
		@Override
		public void actionPerformed(ActionEvent e) {
			Thread t = new Thread(this);
			t.start();
		}

		@Override
		public void run() {
			try {
				LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				for (LocomotiveWidget widget : locomotiveWidgets) {
					Locomotive myLocomotive = widget.getMyLocomotive();
					if (myLocomotive == null) {
						continue;
					}
					if (locomotiveControl.isLocked(myLocomotive)
							&& !locomotiveControl.isLockedByMe(myLocomotive)) {
						continue;
					}
					locomotiveControl.emergencyStop(myLocomotive);
					// locomotiveControl.setSpeed(myLocomotive, 0, null);
					widget.updateWidget();
				}
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}
}
