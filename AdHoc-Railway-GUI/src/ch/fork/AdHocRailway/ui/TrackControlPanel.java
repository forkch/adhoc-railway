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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RouteGroupsPanel;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
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

		JPanel segmentPanel = new KeyTrackControl();

		add(segmentPanel, BorderLayout.WEST);
		JPanel controlPanel = new JPanel(new GridLayout(1, 2));
		if (preferences.getBooleanValue(TABBED_TRACK)) {
			trackControlPane = new JTabbedPane();

			trackControlPane.add("Turnouts", turnoutGroupsTabbedPane);
			trackControlPane.add("Routes", routeGroupsTabbedPane);
			SimpleInternalFrame turnoutRouteFrame = new SimpleInternalFrame(
					"Turnouts/Routes");
			turnoutRouteFrame.add(trackControlPane);
			controlPanel.add(turnoutRouteFrame);
		} else {
			SimpleInternalFrame turnoutFrame = new SimpleInternalFrame(
					"Turnouts");
			SimpleInternalFrame routesFrame = new SimpleInternalFrame("Routes");

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
			public void actionPerformed(ActionEvent e) {
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

	private void updateRoutes() {
		routeGroupsTabbedPane.removeAll();
		int i = 1;
		RouteControlIface routeControl = AdHocRailway.getInstance()
				.getRouteControl();
		routeControl.removeAllRouteChangeListeners();
		RouteManager routePersistence = AdHocRailway.getInstance()
				.getRoutePersistence();

		for (RouteGroup routeGroup : routePersistence.getAllRouteGroups()) {

			WidgetTab routeGroupTab = new WidgetTab();
			JScrollPane groupScrollPane = new JScrollPane(routeGroupTab,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			groupScrollPane.setBorder(BorderFactory.createEmptyBorder());
			groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);
			for (Route aRoute : routeGroup.getRoutes()) {
				RouteWidget routeWidget = new RouteWidget(aRoute);
				routeGroupTab.addWidget(routeWidget);
				routeControl.addRouteChangeListener(aRoute, routeWidget);
			}
			routeGroupsTabbedPane.add(groupScrollPane, "F" + i + ": "
					+ routeGroup.getName());
			i++;
		}
	}

	private class GroupChangeAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7396916793275894512L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (trackControlPane.getSelectedIndex() == 0) {
				int selectedSwitchGroup = Integer
						.parseInt(e.getActionCommand());
				if (selectedSwitchGroup == turnoutGroupsTabbedPane
						.getSelectedIndex()) {
					return;
				}
				TurnoutManager turnoutPersistence = AdHocRailway.getInstance()
						.getTurnoutPersistence();
				if (selectedSwitchGroup < turnoutPersistence
						.getAllTurnoutGroups().size()) {
					turnoutGroupsTabbedPane
							.setSelectedIndex(selectedSwitchGroup);
				}
			} else if (trackControlPane.getSelectedIndex() == 1) {
				int selectedRouteGroup = Integer.parseInt(e.getActionCommand());
				RouteManager routePersistence = AdHocRailway.getInstance()
						.getRoutePersistence();
				System.out.println("here" + selectedRouteGroup);

				if (selectedRouteGroup < routePersistence.getAllRouteGroups()
						.size()) {
					routeGroupsTabbedPane.setSelectedIndex(selectedRouteGroup);
				}

			}
		}
	}

}