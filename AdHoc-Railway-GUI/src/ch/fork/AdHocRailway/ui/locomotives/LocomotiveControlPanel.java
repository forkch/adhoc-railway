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
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerListener;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.SimpleInternalFrame;

public class LocomotiveControlPanel extends JPanel implements
		LocomotiveManagerListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -149795300932888094L;
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
		final FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.CENTER,
				5, 0);
		controlPanel = new JPanel(controlPanelLayout);
		controlPanel.setLayout(controlPanelLayout);

		final SimpleInternalFrame locomotivesFrame = new SimpleInternalFrame(
				"Trains");
		locomotivesFrame.add(controlPanel, BorderLayout.CENTER);
		add(locomotivesFrame, BorderLayout.NORTH);
		getActionMap().put("LocomotiveStop", new LocomotiveStopAction());
		Preferences
				.getInstance()
				.getKeyBoardLayout()
				.assignKeys(getInputMap(WHEN_IN_FOCUSED_WINDOW),
						"LocomotiveStop");
		update();
	}

	public void update() {
		final LocomotiveControlface locomotiveControl = AdHocRailway
				.getInstance().getLocomotiveControl();
		locomotiveControl.removeAllLocomotiveChangeListener();

		controlPanel.removeAll();
		locomotiveWidgets.clear();

		for (int i = 0; i < Preferences.getInstance().getIntValue(
				PreferencesKeys.LOCOMOTIVE_CONTROLES); i++) {
			final LocomotiveWidget w = new LocomotiveWidget(i,
					AdHocRailway.getInstance());
			controlPanel.add(w);
			locomotiveWidgets.add(w);
		}
		revalidate();
		repaint();
	}

	private class LocomotiveStopAction extends AbstractAction implements
			Runnable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5935980511796588692L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Thread t = new Thread(this);
			t.start();
		}

		@Override
		public void run() {
			try {
				final LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				for (final LocomotiveWidget widget : locomotiveWidgets) {
					final Locomotive myLocomotive = widget.getMyLocomotive();
					if (myLocomotive == null) {
						continue;
					}
					if (locomotiveControl.isLocked(myLocomotive)
							&& !locomotiveControl.isLockedByMe(myLocomotive)) {
						continue;
					}
					locomotiveControl.emergencyStop(myLocomotive);
				}
			} catch (final LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {

	}

	@Override
	public void locomotiveUpdated(final Locomotive locomotive) {

	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {

	}

	@Override
	public void locomotiveRemoved(final Locomotive locomotive) {

	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {

	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {

	}

	@Override
	public void locomotivesUpdated(
			final SortedSet<LocomotiveGroup> locomotiveGroups) {

	}

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {

	}
}
