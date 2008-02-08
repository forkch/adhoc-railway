package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.routes.HibernateRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.routes.SRCPRouteControl;
import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;

public class KeyTrackControl extends SimpleInternalFrame {

	private Segment7 seg1;

	private Segment7 seg2;

	private Segment7 seg3;

	private StringBuffer enteredNumberKeys;

	private JPanel switchesHistory;
	
	private Stack<Object> historyStack;

	private SRCPTurnoutControl turnoutControl = SRCPTurnoutControl.getInstance();

	private TurnoutPersistenceIface turnoutPersistence = AdHocRailway.getInstance().getTurnoutPersistence();

	private RouteControlIface routeControl = SRCPRouteControl.getInstance();

	private RoutePersistenceIface routePersistence = AdHocRailway.getInstance().getRoutePersistence();

	public boolean routeMode;

	public boolean changedSwitch = false;

	public boolean changedRoute = false;

	public KeyTrackControl() {
		super("Track Control / History");
		this.historyStack = new Stack<Object>();
		enteredNumberKeys = new StringBuffer();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		JPanel segmentPanelNorth = initSegmentPanel();
		switchesHistory = new JPanel();
		JPanel sh1 = new JPanel(new BorderLayout());

		BoxLayout boxLayout = new BoxLayout(switchesHistory, BoxLayout.Y_AXIS);
		switchesHistory.setLayout(boxLayout);
		
		sh1.add(new JScrollPane(switchesHistory), BorderLayout.NORTH);

		add(segmentPanelNorth, BorderLayout.NORTH);
		add(sh1, BorderLayout.CENTER);
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
		registerKeyboardAction(new SwitchingAction(), "\\", KeyStroke
				.getKeyStroke(92, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new NumberEnteredAction(), ".", KeyStroke
				.getKeyStroke(110, 0), WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(new SwitchingAction(), "\n", KeyStroke
				.getKeyStroke("ENTER"), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "+", KeyStroke
				.getKeyStroke(KeyEvent.VK_ADD, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "bs", KeyStroke
				.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "/", KeyStroke
				.getKeyStroke(KeyEvent.VK_DIVIDE, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "*", KeyStroke
				.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), WHEN_IN_FOCUSED_WINDOW);

		registerKeyboardAction(new SwitchingAction(), "-", KeyStroke
				.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), WHEN_IN_FOCUSED_WINDOW);

	}

	private void updateHistory(Object obj) {
		System.out.println(obj);
		if(historyStack.size() > 3) {
			historyStack.remove(3);
		}
		historyStack.push(obj);
		switchesHistory.removeAll();
		for(int i = 0 ; i < historyStack.size(); i++) {
			Object routeTurnout = historyStack.get(i);
			JPanel w = null;
			if(routeTurnout instanceof Turnout) {
				w = new TurnoutWidget((Turnout)routeTurnout);
			} else if(routeTurnout instanceof Route) {
				w = new RouteWidget((Route)routeTurnout);
			} else {
				return;
			}
			System.out.println("here");
			switchesHistory.add(w);
		}
		revalidate();
		repaint();
	}
	
	private void resetSegmentDisplay() {
		enteredNumberKeys = new StringBuffer();
		seg1.setValue(-1);
		seg2.setValue(-1);
		seg3.setValue(-1);
		seg3.setDisplayPeriod(false);

		seg1.repaint();
		seg2.repaint();
		seg3.repaint();
	}

	private class NumberEnteredAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() == ".") {
				routeMode = true;
				seg3.setDisplayPeriod(true);
				seg3.repaint();
			} else {
				enteredNumberKeys.append(e.getActionCommand());
				String switchNumberAsString = enteredNumberKeys.toString();
				int switchNumber = Integer.parseInt(switchNumberAsString);
				if (switchNumber > 999) {
					resetSegmentDisplay();
					return;
				}
				int seg1Value = switchNumber % 10;
				seg1.setValue(seg1Value);
				seg1.repaint();
				switchNumber = switchNumber - seg1Value;
				int seg2Value = 0;
				if (switchNumber != 0) {
					seg2Value = (switchNumber % 100) / 10;
					seg2.setValue(seg2Value);
				} else {
					seg2.setValue(-1);
				}
				seg2.repaint();
				switchNumber = switchNumber - seg2Value * 10;
				int seg3Value = 0;
				if (switchNumber != 0) {
					seg3Value = (switchNumber % 1000) / 100;
					seg3.setValue(seg3Value);
				} else {
					seg3.setValue(-1);
				}
				seg3.repaint();
				switchNumber = switchNumber - seg3Value * 100;
			}
		}
	}

	private class SwitchingAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {

			try {
				if (enteredNumberKeys.toString().equals("")) {
					if (changedSwitch) {
						turnoutControl.previousDeviceToDefault();
					} else if (changedRoute) {
						routeControl.previousDeviceToDefault();
					}
					changedSwitch = false;
					changedRoute = false;

					return;
				}
				String enteredNumberAsString = enteredNumberKeys.toString();
				int enteredNumber = Integer.parseInt(enteredNumberAsString);
				if (routeMode) {
					handleRouteChange(e, enteredNumber);
				} else {
					handleSwitchChange(e, enteredNumber);
				}

				routeMode = false;
			} catch (ControlException e1) {
				resetSegmentDisplay();
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void handleSwitchChange(ActionEvent e, int enteredNumber)
				throws TurnoutException {
			Turnout searchedTurnout = null;

			searchedTurnout = turnoutPersistence
					.getTurnoutByNumber(enteredNumber);
			if (searchedTurnout == null) {
				resetSegmentDisplay();
				return;
			}

			if (e.getActionCommand().equals("/")) {
				turnoutControl.setCurvedLeft(searchedTurnout);
			} else if (e.getActionCommand().equals("*")) {
				turnoutControl.setStraight(searchedTurnout);
			} else if (e.getActionCommand().equals("-")) {
				turnoutControl.setCurvedRight(searchedTurnout);
			} else if (e.getActionCommand().equals("+")) {
				if (!searchedTurnout.isThreeWay()) {
					turnoutControl.setNonDefaultState(searchedTurnout);
				}
			} else if (e.getActionCommand().equals("bs")) {
				if (!searchedTurnout.isThreeWay()) {
					turnoutControl.setNonDefaultState(searchedTurnout);
				}
			} else if (e.getActionCommand().equals("\n")) {
				turnoutControl.setDefaultState(searchedTurnout);
			}
			changedRoute = false;
			changedSwitch = true;
			updateHistory(searchedTurnout);
			resetSegmentDisplay();
		}

		private void handleRouteChange(ActionEvent e, int enteredNumber)
				throws TurnoutException {
			Route searchedRoute = null;

			searchedRoute = routePersistence.getRouteByNumber(enteredNumber);
			if (searchedRoute == null) {
				resetSegmentDisplay();
				return;
			}
			if (e.getActionCommand().equals("+") || e.getActionCommand().equals("bs")) {
				routeControl.enableRoute(searchedRoute);
			} else if (e.getActionCommand().equals("\n")) {
				routeControl.disableRoute(searchedRoute);
			} else if (e.getActionCommand().equals("\\")) {
				routeControl.enableRoute(searchedRoute);
			}

			changedRoute = true;
			changedSwitch = false;
			
			updateHistory(searchedRoute);
			resetSegmentDisplay();
		}
	}

}
