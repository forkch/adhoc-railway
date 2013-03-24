/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RouteGroupsPanel;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutGroupsPanel;

public class TrackControlPanel extends JPanel implements PreferencesKeys {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3165655530717661123L;

	private JTabbedPane routeGroupsTabbedPane;

	private JTabbedPane turnoutGroupsTabbedPane;

	private final Preferences preferences = Preferences.getInstance();

	private JTabbedPane trackControlPane;

	public TrackControlPanel() {
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		initTurnoutPanel();
		initRoutesPanel();

		final JPanel segmentPanel = new KeyTrackControl();

		add(segmentPanel, BorderLayout.WEST);
		final JPanel controlPanel = new JPanel(new GridLayout(1, 2));
		if (preferences.getBooleanValue(TABBED_TRACK)) {
			trackControlPane = new JTabbedPane();

			trackControlPane.add("Turnouts", turnoutGroupsTabbedPane);
			trackControlPane.add("Routes", routeGroupsTabbedPane);
			final SimpleInternalFrame turnoutRouteFrame = new SimpleInternalFrame(
					"Turnouts/Routes");
			turnoutRouteFrame.add(trackControlPane);
			controlPanel.add(turnoutRouteFrame);
		} else {
			final SimpleInternalFrame turnoutFrame = new SimpleInternalFrame(
					"Turnouts");
			final SimpleInternalFrame routesFrame = new SimpleInternalFrame(
					"Routes");

			turnoutFrame.add(turnoutGroupsTabbedPane, BorderLayout.CENTER);
			routesFrame.add(routeGroupsTabbedPane, BorderLayout.CENTER);

			controlPanel.add(turnoutFrame);
			controlPanel.add(routesFrame);
		}
		add(controlPanel, BorderLayout.CENTER);
	}

	private void initTurnoutPanel() {
		turnoutGroupsTabbedPane = new TurnoutGroupsPanel(JTabbedPane.BOTTOM);
	}

	private void initRoutesPanel() {
		routeGroupsTabbedPane = new RouteGroupsPanel(JTabbedPane.BOTTOM);
	}

	private void initKeyboardActions() {
		getActionMap().put("NextSelected", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6347728432292797201L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (trackControlPane.getSelectedIndex() == 0) {
					trackControlPane.setSelectedIndex(1);
				} else {
					trackControlPane.setSelectedIndex(0);
				}
			}
		});
		Preferences
				.getInstance()
				.getKeyBoardLayout()
				.assignKeys(getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
						"NextSelected");
	}

}