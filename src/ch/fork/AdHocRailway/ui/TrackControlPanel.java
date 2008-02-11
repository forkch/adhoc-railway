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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.routes.SRCPRouteControl;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.SwitchProgrammer;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;

import com.jgoodies.looks.Options;

public class TrackControlPanel
		extends JPanel implements PreferencesKeys {

	private JTabbedPane					routeGroupsTabbedPane;

	private JTabbedPane					turnoutGroupsTabbedPane;

	private Preferences					preferences			=
																	Preferences
																			.getInstance();				;

	private JTabbedPane					trackControlPane;

	private TurnoutControlIface			turnoutControl		=
																	SRCPTurnoutControl
																			.getInstance();

	private TurnoutPersistenceIface		turnoutPersistence	=
																	AdHocRailway
																			.getInstance()
																			.getTurnoutPersistence();

	private RouteControlIface			routeControl		=
																	SRCPRouteControl
																			.getInstance();

	private RoutePersistenceIface		routePersistence	=
																	AdHocRailway
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
			trackControlPane.putClientProperty(Options.NO_CONTENT_BORDER_KEY,
					Boolean.TRUE);
			trackControlPane.putClientProperty(Options.EMBEDDED_TABS_KEY,
					Boolean.TRUE);

			trackControlPane.add("Switches", turnoutGroupsTabbedPane);
			trackControlPane.add("Routes", routeGroupsTabbedPane);
			controlPanel.add(trackControlPane, BorderLayout.CENTER);
		} else {
			turnoutGroupsTabbedPane.setBorder(new TitledBorder("Turnouts"));
			routeGroupsTabbedPane.setBorder(new TitledBorder("Routes"));
			controlPanel.add(turnoutGroupsTabbedPane, BorderLayout.CENTER);
			controlPanel.add(routeGroupsTabbedPane, BorderLayout.EAST);
		}
		initMenuBar();
		initToolBar();
		add(controlPanel, BorderLayout.CENTER);
	}

	private void initTurnoutPanel() {
		turnoutGroupsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

		turnoutGroupsTabbedPane.putClientProperty(
				Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
		turnoutGroupsTabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY,
				Boolean.TRUE);

		updateTurnouts();
	}

	private void initRoutesPanel() {
		routeGroupsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

		routeGroupsTabbedPane.putClientProperty(Options.NO_CONTENT_BORDER_KEY,
				Boolean.TRUE);
		routeGroupsTabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY,
				Boolean.TRUE);

		updateRoutes();
	}

	private void initKeyboardActions() {
		for (int i = 1; i <= 12; i++) {
			KeyStroke stroke =
					KeyStroke.getKeyStroke("F" + Integer.toString(i));
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
		JButton addTurnoutsButton =
				new SmallToolbarButton(new AddTurnoutsAction());
		JButton setAllSwitchesStraightButton =
				new SmallToolbarButton(new TurnoutsStraightAction());
		JButton switchProgrammerButton =
				new SmallToolbarButton(new TurnoutProgrammerAction());

		turnoutToolsToolBar.add(addTurnoutsButton);
		turnoutToolsToolBar.add(setAllSwitchesStraightButton);
		turnoutToolsToolBar.add(switchProgrammerButton);

		AdHocRailway.getInstance().addToolBar(turnoutToolsToolBar);
	}

	private void initMenuBar() {
		/* TOOLS */
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem addTurnoutsItem = new JMenuItem(new AddTurnoutsAction());
		JMenuItem switchesStraightItem =
				new JMenuItem(new TurnoutsStraightAction());
		JMenuItem switchProgrammerItem =
				new JMenuItem(new TurnoutProgrammerAction());

		toolsMenu.add(addTurnoutsItem);
		toolsMenu.add(switchesStraightItem);
		toolsMenu.add(switchProgrammerItem);

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

	private void updateRoutes() {
		routeGroupsTabbedPane.removeAll();
		int maxRouteCols =
				preferences.getIntValue(PreferencesKeys.ROUTE_CONTROLES);
		int i = 1;
		routeControl.removeAllRouteChangeListeners();
		for (RouteGroup routeGroup : routePersistence.getAllRouteGroups()) {

			WidgetTab routeGroupTab = new WidgetTab(maxRouteCols);
			JScrollPane groupScrollPane = new JScrollPane(routeGroupTab,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
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

	private void updateTurnouts() {
		indexToTurnoutGroup.clear();
		turnoutGroupsTabbedPane.removeAll();
		int maxTurnoutCols =
				preferences.getIntValue(PreferencesKeys.TURNOUT_CONTROLES);
		int i = 1;
		turnoutControl.removeAllTurnoutChangeListener();
		for (TurnoutGroup turnoutGroup : turnoutPersistence
				.getAllTurnoutGroups()) {

			indexToTurnoutGroup.put(i - 1, turnoutGroup);
			WidgetTab switchGroupTab = new WidgetTab(maxTurnoutCols);
			JScrollPane groupScrollPane =
					new JScrollPane(switchGroupTab);
			groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);
			for (Turnout turnout : turnoutGroup.getTurnouts()) {
				TurnoutWidget switchWidget = new TurnoutWidget(turnout);
				switchGroupTab.addWidget(switchWidget);
			}
			turnoutGroupsTabbedPane.add(groupScrollPane, "F" + i + ": "
					+ turnoutGroup.getName());
			switchGroupTab.setPreferredSize(groupScrollPane.getSize());
			i++;
		}
	}

	private class TurnoutGroupChangeAction
			extends AbstractAction {

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

	private class RouteGroupChangeAction
			extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedRouteGroup = Integer.parseInt(e.getActionCommand());
			if (selectedRouteGroup < routePersistence.getAllRouteGroups()
					.size()) {
				routeGroupsTabbedPane.setSelectedIndex(selectedRouteGroup);
			}
		}
	}

	private class TurnoutsStraightAction
			extends AbstractAction {

		public TurnoutsStraightAction() {
			super("Set all turnouts straight", createImageIcon("switch.png"));
		}

		public void actionPerformed(ActionEvent e) {
			TurnoutStraighter s = new TurnoutStraighter();
			s.start();
		}

		private class TurnoutStraighter
				extends Thread {

			public void run() {
				try {

					for (Turnout t : turnoutPersistence.getAllTurnouts()) {
						turnoutControl.setDefaultState(t);
						Thread.sleep(250);
					}
				} catch (ControlException e1) {
					ExceptionProcessor.getInstance().processException(e1);
					return;
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private class AddTurnoutsAction
			extends AbstractAction {
		public AddTurnoutsAction() {
			super("Add Turnouts", createImageIcon("filenew.png"));
		}

		public void actionPerformed(ActionEvent e) {
			TurnoutConfig config = null;
			int selectedGroupPane = turnoutGroupsTabbedPane.getSelectedIndex();
			do {
				TurnoutGroup selectedTurnoutGroup =
						indexToTurnoutGroup.get(selectedGroupPane);

				Turnout newTurnout = new Turnout();
				newTurnout.setNumber(turnoutPersistence
						.getNextFreeTurnoutNumber());
				newTurnout.setTurnoutGroup(selectedTurnoutGroup);
				newTurnout.setDefaultStateEnum(TurnoutState.STRAIGHT);
				newTurnout.setOrientationEnum(TurnoutOrientation.EAST);
				newTurnout.setTurnoutType(turnoutPersistence
						.getTurnoutType(TurnoutTypes.DEFAULT));

				config =
						new TurnoutConfig(AdHocRailway.getInstance(),
								newTurnout);
			} while (!config.isCancelPressed());
			update();
			turnoutGroupsTabbedPane.setSelectedIndex(selectedGroupPane);
		}
	}

	private class TurnoutProgrammerAction
			extends AbstractAction {

		public TurnoutProgrammerAction() {
			super("Turnout Decoder Programmer",
					createImageIcon("switch_programmer.png"));
		}

		public void actionPerformed(ActionEvent e) {
			new SwitchProgrammer(AdHocRailway.getInstance(), AdHocRailway
					.getInstance().getSession());
		}
	}
}