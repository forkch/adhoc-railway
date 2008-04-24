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

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.SwitchProgrammer;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class TrackControlPanel extends JPanel implements PreferencesKeys {

	private JTabbedPane					routeGroupsTabbedPane;

	private JTabbedPane					turnoutGroupsTabbedPane;

	private Preferences					preferences			= Preferences
																	.getInstance();

	private JTabbedPane					trackControlPane;

	private TurnoutControlIface			turnoutControl		= AdHocRailway.getInstance().getTurnoutControl();

	private TurnoutPersistenceIface		turnoutPersistence	= AdHocRailway
																	.getInstance()
																	.getTurnoutPersistence();

	private RouteControlIface			routeControl		= AdHocRailway.getInstance().getRouteControl();
	
	private RoutePersistenceIface		routePersistence	= AdHocRailway
																	.getInstance()
																	.getRoutePersistence();

	private Map<Integer, TurnoutGroup>	indexToTurnoutGroup;

	public TrackControlPanel() {
		this.indexToTurnoutGroup = new HashMap<Integer, TurnoutGroup>();
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
			// turnoutGroupsTabbedPane.setBorder(new TitledBorder("Turnouts"));
			// routeGroupsTabbedPane.setBorder(new TitledBorder("Routes"));
			SimpleInternalFrame turnoutFrame = new SimpleInternalFrame(
					"Turnouts");
			SimpleInternalFrame routesFrame = new SimpleInternalFrame("Routes");

			turnoutFrame.add(turnoutGroupsTabbedPane, BorderLayout.CENTER);
			routesFrame.add(routeGroupsTabbedPane, BorderLayout.CENTER);

			controlPanel.add(turnoutFrame);
			controlPanel.add(routesFrame);
		}
		initMenuBar();
		initToolBar();
		add(controlPanel, BorderLayout.CENTER);
	}

	private void initTurnoutPanel() {
		turnoutGroupsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

		updateTurnouts();
	}

	private void initRoutesPanel() {
		routeGroupsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

		updateRoutes();
	}

	private void initKeyboardActions() {
		for (int i = 1; i <= 12; i++) {
			KeyStroke stroke = KeyStroke
					.getKeyStroke("F" + Integer.toString(i));
			registerKeyboardAction(new TurnoutGroupChangeAction(), Integer
					.toString(i - 1), stroke, WHEN_IN_FOCUSED_WINDOW);
			registerKeyboardAction(new RouteGroupChangeAction(), Integer
					.toString(i - 1), KeyStroke.getKeyStroke(stroke
					.getKeyCode(), InputEvent.SHIFT_DOWN_MASK),
					WHEN_IN_FOCUSED_WINDOW);

		}
	}

	private void initToolBar() {
		/* Turnout Tools */
		JToolBar turnoutToolsToolBar = new JToolBar();
		JButton addTurnoutsButton = new SmallToolbarButton(
				new AddTurnoutsAction());
		JButton setAllSwitchesStraightButton = new SmallToolbarButton(
				new TurnoutsStraightAction());
		JButton switchProgrammerButton = new SmallToolbarButton(
				new TurnoutProgrammerAction());

		turnoutToolsToolBar.add(addTurnoutsButton);
		turnoutToolsToolBar.add(setAllSwitchesStraightButton);
		turnoutToolsToolBar.add(switchProgrammerButton);

		AdHocRailway.getInstance().addToolBar(turnoutToolsToolBar);
	}

	private void initMenuBar() {
		/* TOOLS */
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem addTurnoutsItem = new JMenuItem(new AddTurnoutsAction());
		JMenuItem turnoutsStraightItem = new JMenuItem(
				new TurnoutsStraightAction());
		JMenuItem refreshTurnoutStateItem = new JMenuItem(
				new RefreshTurnoutStateAction());
		JMenuItem turnoutsProgrammerItem = new JMenuItem(
				new TurnoutProgrammerAction());

		JMenuItem enlargeTurnoutGroupsItem = new JMenuItem(
				new EnlargeTurnoutGroups());

		toolsMenu.add(addTurnoutsItem);
		toolsMenu.add(turnoutsStraightItem);
		toolsMenu.add(refreshTurnoutStateItem);
		toolsMenu.add(turnoutsProgrammerItem);
		toolsMenu.addSeparator();
		toolsMenu.add(enlargeTurnoutGroupsItem);

		AdHocRailway.getInstance().addMenu(toolsMenu);
	}

	public void update() {
		turnoutPersistence = AdHocRailway.getInstance().getTurnoutPersistence();
		routePersistence = AdHocRailway.getInstance().getRoutePersistence();
		updateTurnouts();
		updateRoutes();
		revalidate();
		repaint();
	}

	private void updateTurnouts() {
		indexToTurnoutGroup.clear();
		turnoutGroupsTabbedPane.removeAll();
		int maxTurnoutCols = preferences
				.getIntValue(PreferencesKeys.TURNOUT_CONTROLES);
		int i = 1;
		turnoutControl.removeAllTurnoutChangeListener();
		for (TurnoutGroup turnoutGroup : turnoutPersistence
				.getAllTurnoutGroups()) {

			indexToTurnoutGroup.put(i - 1, turnoutGroup);
			WidgetTab switchGroupTab = new WidgetTab(maxTurnoutCols);
			JScrollPane groupScrollPane = new JScrollPane(switchGroupTab,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			groupScrollPane.setBorder(BorderFactory.createEmptyBorder());
			groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);

			turnoutGroupsTabbedPane.add(groupScrollPane, "F" + i + ": "
					+ turnoutGroup.getName());

			// Dimension dim = new Dimension(groupScrollPane.getSize().width,
			// 4000);
			// switchGroupTab.setPreferredSize(dim);
			for (Turnout turnout : turnoutGroup.getTurnouts()) {
				TurnoutWidget switchWidget = new TurnoutWidget(turnout);
				switchGroupTab.addWidget(switchWidget);
			}
			i++;
		}
	}

	private void updateRoutes() {
		routeGroupsTabbedPane.removeAll();
		int maxRouteCols = preferences
				.getIntValue(PreferencesKeys.ROUTE_CONTROLES);
		int i = 1;
		routeControl.removeAllRouteChangeListeners();
		for (RouteGroup routeGroup : routePersistence.getAllRouteGroups()) {

			WidgetTab routeGroupTab = new WidgetTab(maxRouteCols);
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

	private class TurnoutGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedSwitchGroup = Integer.parseInt(e.getActionCommand());
			if (selectedSwitchGroup == turnoutGroupsTabbedPane
					.getSelectedIndex())
				return;
			if (selectedSwitchGroup < turnoutPersistence.getAllTurnoutGroups()
					.size()) {
				turnoutGroupsTabbedPane.setSelectedIndex(selectedSwitchGroup);
			}
		}
	}

	private class RouteGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedRouteGroup = Integer.parseInt(e.getActionCommand());
			if (selectedRouteGroup < routePersistence.getAllRouteGroups()
					.size()) {
				routeGroupsTabbedPane.setSelectedIndex(selectedRouteGroup);
			}
		}
	}

	private class TurnoutsStraightAction extends AbstractAction {

		public TurnoutsStraightAction() {
			super("Set all turnouts straight\u2026",
					createImageIcon("switch.png"));
		}

		public void actionPerformed(ActionEvent e) {
			TurnoutStraighter s = new TurnoutStraighter();
			s.start();
		}

		private class TurnoutStraighter extends Thread {

			public void run() {
				try {

					for (Turnout t : turnoutPersistence.getAllTurnouts()) {
						turnoutControl.setDefaultState(t);
						Thread.sleep(Preferences.getInstance().getIntValue(
								PreferencesKeys.ROUTING_DELAY));
					}
				} catch (TurnoutException e1) {
					ExceptionProcessor.getInstance().processException(e1);
					return;
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	
	private class RefreshTurnoutStateAction extends AbstractAction {

		public RefreshTurnoutStateAction() {
			super("Determine state of each turnout\u2026",
					createImageIcon("switch.png"));
		}

		public void actionPerformed(ActionEvent e) {
			
			for(Turnout t : turnoutPersistence.getAllTurnouts()) {
				try {
					turnoutControl.refresh(t);
				} catch (TurnoutException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
			}
		}
	}

	private class EnlargeTurnoutGroups extends AbstractAction {
		public EnlargeTurnoutGroups() {
			super("Rearranging Turnout and Route numbers (enlarge groups)");
		}

		public void actionPerformed(ActionEvent arg0) {
			int result = JOptionPane
					.showConfirmDialog(
							AdHocRailway.getInstance(),
							"The numbering of the Turnout- and Route-Groups is now being adjusted\n"
									+ "such that each group has at least 5 free slots for the turnouts\n"
									+ "(each group has a multiple of 10 turnouts)\n\n"
									+ "Do you want to proceed ?",
							"Rearranging Turnout and Route numbers",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							createImageIcon("messagebox_info.png"));
			if (result == JOptionPane.OK_OPTION) {
				turnoutPersistence.enlargeTurnoutGroups();
				routePersistence.enlargeRouteGroups();
				update();
			}
		}
	}

	private class AddTurnoutsAction extends AbstractAction {
		public AddTurnoutsAction() {
			super("Add Turnouts\u2026", createImageIcon("filenew.png"));
		}

		public void actionPerformed(ActionEvent e) {
			if (indexToTurnoutGroup.isEmpty()) {
				JOptionPane.showMessageDialog(AdHocRailway.getInstance(),
						"Please configure a group first", "Add Turnouts",
						JOptionPane.INFORMATION_MESSAGE, createImageIcon("messagebox_info.png"));
				return;
			}
			TurnoutConfig config = null;
			int selectedGroupPane = turnoutGroupsTabbedPane.getSelectedIndex();

			do {
				TurnoutGroup selectedTurnoutGroup = indexToTurnoutGroup
						.get(selectedGroupPane);
				int nextNumber = 0;
				if (Preferences
						.getInstance()
						.getBooleanValue(
								PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
					nextNumber = turnoutPersistence
							.getNextFreeTurnoutNumberOfGroup(selectedTurnoutGroup);
					if (nextNumber == -1) {
						JOptionPane.showMessageDialog(AdHocRailway
								.getInstance(),
								"No more free numbers in this group", "Error",
								JOptionPane.ERROR_MESSAGE);
						update();
						turnoutGroupsTabbedPane
								.setSelectedIndex(selectedGroupPane);
						return;
					}
				} else {
					nextNumber = turnoutPersistence.getNextFreeTurnoutNumber();
				}

				Turnout newTurnout = new Turnout();
				newTurnout.setNumber(nextNumber);

				newTurnout.setBus1(Preferences.getInstance().getIntValue(
						PreferencesKeys.DEFAULT_TURNOUT_BUS));
				newTurnout.setBus2(Preferences.getInstance().getIntValue(
						PreferencesKeys.DEFAULT_TURNOUT_BUS));

				newTurnout.setTurnoutGroup(selectedTurnoutGroup);
				newTurnout.setDefaultStateEnum(SRCPTurnoutState.STRAIGHT);
				newTurnout.setOrientationEnum(TurnoutOrientation.EAST);
				newTurnout.setTurnoutType(turnoutPersistence
						.getTurnoutType(SRCPTurnoutTypes.DEFAULT));

				config = new TurnoutConfig(AdHocRailway.getInstance(),
						newTurnout);
			} while (!config.isCancelPressed());
			update();
			turnoutGroupsTabbedPane.setSelectedIndex(selectedGroupPane);
		}
	}

	private class TurnoutProgrammerAction extends AbstractAction {

		public TurnoutProgrammerAction() {
			super("Turnout Decoder Programmer\u2026",
					createImageIcon("switch_programmer.png"));
		}

		public void actionPerformed(ActionEvent e) {
			new SwitchProgrammer(AdHocRailway.getInstance(), AdHocRailway
					.getInstance().getSession());
		}
	}
}