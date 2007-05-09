package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.routes.RoutesControlPanel;
import ch.fork.AdHocRailway.ui.switches.SwitchGroupPane;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;
import ch.fork.AdHocRailway.ui.switches.canvas.Segment7;

public class TrackControlPanel extends JPanel implements PreferencesKeys {

	private RoutesControlPanel routesControlPanel;

	private SwitchGroupPane switchGroupPane;

	private Preferences preferences;

	private JTabbedPane trackControlPane;

	private JFrame frame;

	private Segment7 seg1;

	private Segment7 seg2;

	private Segment7 seg3;

	private StringBuffer enteredNumberKeys;

	private JPanel switchesHistory;

	private SwitchControl switchControl;

	private RouteControl routeControl;

	public boolean routeMode;

	public TrackControlPanel(JFrame frame) {
		this.frame = frame;
		this.preferences = Preferences.getInstance();
		this.switchControl = SwitchControl.getInstance();
		this.routeControl = RouteControl.getInstance();
		enteredNumberKeys = new StringBuffer();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		switchGroupPane = initSwitchPanel();
		routesControlPanel = initRoutesPanel();

		JPanel segmentPanelNorth = initSegmentPanel();
		switchesHistory = new JPanel();
		JPanel sh1 = new JPanel(new BorderLayout());
		sh1.add(switchesHistory, BorderLayout.NORTH);
		BoxLayout boxLayout = new BoxLayout(switchesHistory, BoxLayout.Y_AXIS);
		switchesHistory.setLayout(boxLayout);

		JPanel segmentPanel = new JPanel(new BorderLayout());
		segmentPanel.add(segmentPanelNorth, BorderLayout.NORTH);
		segmentPanel.add(sh1, BorderLayout.CENTER);
		add(segmentPanel, BorderLayout.WEST);
		JPanel controlPanel = new JPanel(new GridLayout(1, 2));
		if (preferences.getBooleanValue(TABBED_TRACK)) {
			trackControlPane = new JTabbedPane();
			trackControlPane.add("Switches", switchGroupPane);
			trackControlPane.add("Routes", routesControlPanel);
			controlPanel.add(trackControlPane, BorderLayout.CENTER);
		} else {
			controlPanel.add(switchGroupPane, BorderLayout.CENTER);
			controlPanel.add(routesControlPanel, BorderLayout.EAST);
		}
		add(controlPanel, BorderLayout.CENTER);
	}

	private SwitchGroupPane initSwitchPanel() {
		SwitchGroupPane mySwitchGroupPane = new SwitchGroupPane(frame);
		mySwitchGroupPane.setBorder(new TitledBorder("Switches"));
		return mySwitchGroupPane;
	}

	private RoutesControlPanel initRoutesPanel() {
		RoutesControlPanel routesControlPanel = new RoutesControlPanel();
		routesControlPanel.setBorder(new TitledBorder("Routes"));
		return routesControlPanel;
	}

	private void initKeyboardActions() {
		for (int i = 0; i <= 10; i++) {
			registerKeyboardAction(new NumberEnteredAction(), Integer
					.toString(i), KeyStroke.getKeyStroke(Integer.toString(i)),
					WHEN_IN_FOCUSED_WINDOW);
			registerKeyboardAction(new NumberEnteredAction(), Integer
					.toString(i), KeyStroke.getKeyStroke("NUMPAD"
					+ Integer.toString(i)), WHEN_IN_FOCUSED_WINDOW);

		}
		registerKeyboardAction(new NumberEnteredAction(), ".", KeyStroke
				.getKeyStroke(KeyEvent.VK_PERIOD, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new NumberEnteredAction(), ".", KeyStroke
				.getKeyStroke(110, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new SwitchingAction(), "\n", KeyStroke
				.getKeyStroke("ENTER"), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "+", KeyStroke
				.getKeyStroke(KeyEvent.VK_ADD, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "", KeyStroke
				.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "/", KeyStroke
				.getKeyStroke(KeyEvent.VK_DIVIDE, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "*", KeyStroke
				.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "-", KeyStroke
				.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), WHEN_IN_FOCUSED_WINDOW);

		for (int i = 1; i <= 12; i++) {
			registerKeyboardAction(new SwitchGroupChangeAction(), Integer
					.toString(i - 1), KeyStroke.getKeyStroke("F"
					+ Integer.toString(i)), WHEN_IN_FOCUSED_WINDOW);
		}
	}

	private void resetSelectedSwitchDisplay() {
		enteredNumberKeys = new StringBuffer();
		seg1.setValue(0);
		seg2.setValue(0);
		seg3.setValue(0);
		seg1.repaint();
		seg2.repaint();
		seg3.repaint();
	}

	private class NumberEnteredAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() == ".") {
				routeMode = true;
				System.out.println("routeMode");
			} else {
				enteredNumberKeys.append(e.getActionCommand());
				String switchNumberAsString = enteredNumberKeys.toString();
				int switchNumber = Integer.parseInt(switchNumberAsString);
				if (switchNumber > 999) {
					resetSelectedSwitchDisplay();
					return;
				}
				int seg1Value = switchNumber % 10;
				seg1.setValue(seg1Value);
				seg1.repaint();
				switchNumber = switchNumber - seg1Value;
				int seg2Value = (switchNumber % 100) / 10;
				seg2.setValue(seg2Value);
				seg2.repaint();
				switchNumber = switchNumber - seg2Value * 10;
				int seg3Value = (switchNumber % 1000) / 100;
				seg3.setValue(seg3Value);
				seg3.repaint();
				switchNumber = switchNumber - seg3Value * 100;
			}
		}
	}

	private class SwitchingAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {

			try {
				if (enteredNumberKeys.toString().equals("")) {
					if (!routeMode) {
						switchControl.previousDeviceToDefault();
					} else {
						routeControl.previousDeviceToDefault();
					}
					return;
				}
				String switchNumberAsString = enteredNumberKeys.toString();
				int switchNumber = Integer.parseInt(switchNumberAsString);
				Switch searchedSwitch = null;

				searchedSwitch = switchControl.getNumberToSwitch().get(
						switchNumber);
				if (searchedSwitch == null) {
					resetSelectedSwitchDisplay();
					return;
				}

				if (e.getActionCommand().equals("/")) {
					handleDivide(searchedSwitch);
				} else if (e.getActionCommand().equals("*")) {
					handleMultiply(searchedSwitch);
				} else if (e.getActionCommand().equals("-")) {
					handleMinus(searchedSwitch);
				} else if (e.getActionCommand().equals("+")) {
					if (!(searchedSwitch instanceof ThreeWaySwitch)) {
						handlePlus(searchedSwitch);
					}
				} else if (e.getActionCommand().equals("")) {
					if (!(searchedSwitch instanceof ThreeWaySwitch)) {
						handlePlus(searchedSwitch);
					}
				} else if (e.getActionCommand().equals("\n")) {
					handleEnter(searchedSwitch);
				}
				resetSelectedSwitchDisplay();
				SwitchWidget sw = new SwitchWidget(searchedSwitch, null, true,
						frame);
				Component[] oldWidgets = switchesHistory.getComponents();
				switchesHistory.removeAll();
				switchesHistory.add(sw);
				if (oldWidgets.length > 0
						&& sw.getMySwitch() != ((SwitchWidget) oldWidgets[0])
								.getMySwitch())
					switchesHistory.add(oldWidgets[0]);
				if (oldWidgets.length > 1
						&& sw.getMySwitch() != ((SwitchWidget) oldWidgets[1])
								.getMySwitch())
					switchesHistory.add(oldWidgets[1]);
				repaint();
				revalidate();
			} catch (ControlException e1) {
				resetSelectedSwitchDisplay();
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void handleDivide(Switch aSwitch) throws SwitchException {
			switchControl.setCurvedLeft(aSwitch);
		}

		private void handleMultiply(Switch aSwitch) throws SwitchException {
			switchControl.setStraight(aSwitch);
		}

		private void handleMinus(Switch aSwitch) throws SwitchException {
			switchControl.setCurvedRight(aSwitch);
		}

		private void handlePlus(Switch aSwitch) throws SwitchException {
			switchControl.setCurvedLeft(aSwitch);
		}

		private void handleEnter(Switch aSwitch) throws SwitchException {
			switchControl.setStraight(aSwitch);
		}
	}

	private class SwitchGroupChangeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			int selectedSwitchGroup = Integer.parseInt(e.getActionCommand());
			if (selectedSwitchGroup < switchControl.getSwitchGroups().size()) {
				switchGroupPane.setSelectedIndex(selectedSwitchGroup);
			}
		}
	}

	private JPanel initSegmentPanel() {
		JPanel segmentPanelNorth = new JPanel(new FlowLayout(
				FlowLayout.TRAILING, 5, 0));
		segmentPanelNorth.setBackground(new Color(0, 0, 0));
		seg1 = new Segment7();
		seg2 = new Segment7();
		seg3 = new Segment7();
		segmentPanelNorth.add(seg3);
		segmentPanelNorth.add(seg2);
		segmentPanelNorth.add(seg1);
		JPanel p = new JPanel(new BorderLayout());
		p.add(segmentPanelNorth, BorderLayout.WEST);
		return p;
	}

	public void update() {
		switchGroupPane.update(switchControl.getSwitchGroups());
		routesControlPanel.update(routeControl.getRoutes());
		revalidate();
        repaint();
        
	}
}