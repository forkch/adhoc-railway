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

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class TrackControlPanel extends JPanel implements PreferencesKeys {

	private JTabbedPane routeGroupsTabbedPane;

	private JTabbedPane switchGroupsTabbedPane;

	private Preferences preferences;

	private JTabbedPane trackControlPane;

	private JFrame frame;

	private SwitchControl switchControl;

	private RouteControl routeControl;

	public TrackControlPanel(JFrame frame) {
		this.frame = frame;
		this.preferences = Preferences.getInstance();
		this.switchControl = SwitchControl.getInstance();
		this.routeControl = RouteControl.getInstance();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		initSwitchPanel();
		initRoutesPanel();

		JPanel segmentPanel = new KeyTrackControl();

		add(segmentPanel, BorderLayout.WEST);
		JPanel controlPanel = new JPanel(new GridLayout(1, 2));
		if (preferences.getBooleanValue(TABBED_TRACK)) {
			trackControlPane = new JTabbedPane();
			trackControlPane.add("Switches", switchGroupsTabbedPane);
			trackControlPane.add("Routes", routeGroupsTabbedPane);
			controlPanel.add(trackControlPane, BorderLayout.CENTER);
		} else {
			switchGroupsTabbedPane.setBorder(new TitledBorder("Switches"));
			routeGroupsTabbedPane.setBorder(new TitledBorder("Routes"));
			controlPanel.add(switchGroupsTabbedPane, BorderLayout.CENTER);
			controlPanel.add(routeGroupsTabbedPane, BorderLayout.EAST);
		}
		add(controlPanel, BorderLayout.CENTER);
	}

	private void initSwitchPanel() {
		switchGroupsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		updateSwitches();
	}

	private void initRoutesPanel() {
		routeGroupsTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		updateRoutes();
	}

	private void initKeyboardActions() {
		for (int i = 1; i <= 12; i++) {
			KeyStroke stroke = KeyStroke
					.getKeyStroke("F" + Integer.toString(i));
			registerKeyboardAction(new SwitchGroupChangeAction(), Integer
					.toString(i - 1), stroke, WHEN_IN_FOCUSED_WINDOW);
			registerKeyboardAction(new RouteGroupChangeAction(), Integer
					.toString(i - 1), KeyStroke.getKeyStroke(stroke
					.getKeyCode(), InputEvent.SHIFT_DOWN_MASK),
					WHEN_IN_FOCUSED_WINDOW);

		}
	}

	public void update() {
		updateSwitches();
		updateRoutes();
		revalidate();
		repaint();
	}

	private void updateRoutes() {
		routeGroupsTabbedPane.removeAll();
		int maxRouteCols = preferences
				.getIntValue(PreferencesKeys.ROUTE_CONTROLES);
		int i = 1;
		for (RouteGroup routeGroup : routeControl.getRouteGroups()) {

			WidgetTab routeGroupTab = new WidgetTab(maxRouteCols);
			JScrollPane groupScrollPane = new JScrollPane(routeGroupTab);
			groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);
			for (Route aRoute : routeGroup.getRoutes()) {
				RouteWidget routeWidget = new RouteWidget(aRoute);
				routeGroupTab.addWidget(routeWidget);
			}
			routeGroupsTabbedPane.add(groupScrollPane, "F" + i + ": "
					+ routeGroup.getName());
			i++;
		}
	}

	private void updateSwitches() {
		switchGroupsTabbedPane.removeAll();
		int maxSwitchCols = preferences
				.getIntValue(PreferencesKeys.SWITCH_CONTROLES);
		int i = 1;
		for (SwitchGroup switchGroup : switchControl.getSwitchGroups()) {

			WidgetTab switchGroupTab = new WidgetTab(maxSwitchCols);
			JScrollPane groupScrollPane = new JScrollPane(switchGroupTab);
			groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
			groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);
			for (Switch aSwitch : switchGroup.getSwitches()) {
				SwitchWidget switchWidget = new SwitchWidget(aSwitch,
						switchGroup, frame);
				switchGroupTab.addWidget(switchWidget);
			}
			switchGroupsTabbedPane.add(groupScrollPane, "F" + i + ": "
					+ switchGroup.getName());
			i++;
		}
	}

	private class SwitchGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedSwitchGroup = Integer.parseInt(e.getActionCommand());
			if (selectedSwitchGroup < switchControl.getSwitchGroups().size()) {
				switchGroupsTabbedPane.setSelectedIndex(selectedSwitchGroup);
			}
		}
	}

	private class RouteGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedRouteGroup = Integer.parseInt(e.getActionCommand());
			if (selectedRouteGroup < routeControl.getRouteGroups().size()) {
				routeGroupsTabbedPane.setSelectedIndex(selectedRouteGroup);
			}
		}
	}

}