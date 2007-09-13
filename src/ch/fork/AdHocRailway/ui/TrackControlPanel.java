package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import ch.fork.AdHocRailway.domain.routes.HibernateRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.switches.TurnoutWidget;

public class TrackControlPanel extends JPanel implements PreferencesKeys {

	private JTabbedPane routeGroupsTabbedPane;

	private JTabbedPane turnoutGroupsTabbedPane;

	private Preferences preferences = Preferences.getInstance();;

	private JTabbedPane trackControlPane;

	private JFrame frame;

	private TurnoutControl turnoutControl = TurnoutControl.getInstance();

	private TurnoutPersistenceIface turnoutPersistence = HibernateTurnoutPersistence
			.getInstance();

	private RouteControl routeControl = RouteControl.getInstance();
	
	private HibernateRoutePersistence routePersistence  = HibernateRoutePersistence.getInstance();

	public TrackControlPanel(JFrame frame) {
		this.frame = frame;
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		initTurnoutPanel();
		initRoutesPanel();

		JPanel segmentPanel = new KeyTrackControl();

		add(segmentPanel, BorderLayout.WEST);
		JPanel controlPanel = new JPanel(new GridLayout(1, 2));
		if (preferences.getBooleanValue(TABBED_TRACK)) {
			trackControlPane = new JTabbedPane();
			trackControlPane.add("Switches", turnoutGroupsTabbedPane);
			trackControlPane.add("Routes", routeGroupsTabbedPane);
			controlPanel.add(trackControlPane, BorderLayout.CENTER);
		} else {
			turnoutGroupsTabbedPane.setBorder(new TitledBorder("Switches"));
			routeGroupsTabbedPane.setBorder(new TitledBorder("Routes"));
			controlPanel.add(turnoutGroupsTabbedPane, BorderLayout.CENTER);
			controlPanel.add(routeGroupsTabbedPane, BorderLayout.EAST);
		}
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

	public void update() {
		updateTurnouts();
		updateRoutes();
		revalidate();
		repaint();
	}

	private void updateRoutes() {
		routeGroupsTabbedPane.removeAll();
		int maxRouteCols = preferences
				.getIntValue(PreferencesKeys.ROUTE_CONTROLES);
		int i = 1;
		routeControl.removeAllRouteChangeListeners();
		for (RouteGroup routeGroup : routePersistence.getAllRouteGroups()) {

			WidgetTab routeGroupTab = new WidgetTab(maxRouteCols);
			JScrollPane groupScrollPane = new JScrollPane(routeGroupTab);
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
		turnoutGroupsTabbedPane.removeAll();
		int maxTurnoutCols = preferences
				.getIntValue(PreferencesKeys.TURNOUT_CONTROLES);
		int i = 1;
		turnoutControl.removeAllTurnoutChangeListener();
		for (TurnoutGroup turnoutGroup : turnoutPersistence.getAllTurnoutGroups()) {

			WidgetTab switchGroupTab = new WidgetTab(maxTurnoutCols);
			JScrollPane groupScrollPane = new JScrollPane(switchGroupTab);
			groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);
			for (Turnout turnout : turnoutGroup.getTurnouts()) {
				TurnoutWidget switchWidget = new TurnoutWidget(turnout,
						frame);
				switchGroupTab.addWidget(switchWidget);
			}
			turnoutGroupsTabbedPane.add(groupScrollPane, "F" + i + ": "
					+ turnoutGroup.getName());
			i++;
		}
	}

	private class TurnoutGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedSwitchGroup = Integer.parseInt(e.getActionCommand());
			if (selectedSwitchGroup < turnoutPersistence.getAllTurnoutGroups().size()) {
				turnoutGroupsTabbedPane.setSelectedIndex(selectedSwitchGroup);
			}
		}
	}

	private class RouteGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedRouteGroup = Integer.parseInt(e.getActionCommand());
			if (selectedRouteGroup < routePersistence.getAllRouteGroups().size()) {
				routeGroupsTabbedPane.setSelectedIndex(selectedRouteGroup);
			}
		}
	}

}