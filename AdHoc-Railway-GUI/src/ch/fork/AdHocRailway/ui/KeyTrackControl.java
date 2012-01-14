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
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteException;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.StaticTurnoutWidget;

public class KeyTrackControl extends SimpleInternalFrame {

	private StringBuffer		enteredNumberKeys;

	private JPanel				switchesHistory;

	private LinkedList<Object>	historyStack;

	private LinkedList<JPanel>	historyWidgets;

	public boolean				routeMode;

	public boolean				changedSwitch	= false;

	public boolean				changedRoute	= false;

	public static final int		HISTORY_LENGTH	= 5;

	private JScrollPane			switchesHistoryPane;

	private ThreeDigitDisplay	digitDisplay;

	public KeyTrackControl() {
		super("Track Control / History");
		this.historyStack = new LinkedList<Object>();
		this.historyWidgets = new LinkedList<JPanel>();
		enteredNumberKeys = new StringBuffer();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		JPanel segmentPanelNorth = initSegmentPanel();
		switchesHistory = new JPanel();
		JPanel sh1 = new JPanel(new BorderLayout());

		switchesHistory.setLayout(new GridLayout(HISTORY_LENGTH, 1));

		switchesHistoryPane = new JScrollPane(switchesHistory,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sh1.add(switchesHistoryPane, BorderLayout.CENTER);

		add(segmentPanelNorth, BorderLayout.NORTH);
		add(sh1, BorderLayout.CENTER);
	}

	private JPanel initSegmentPanel() {
		digitDisplay = new ThreeDigitDisplay();
		JPanel p = new JPanel(new BorderLayout());
		p.add(digitDisplay, BorderLayout.WEST);
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
		KeyBoardLayout kbl = Preferences.getInstance().getKeyBoardLayout();
		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		getActionMap().put("RouteNumberEntered", new PeriodEnteredAction());
		kbl.assignKeys(inputMap, "RouteNumberEntered");
		getActionMap().put("CurvedLeft", new CurvedLeftAction());
		kbl.assignKeys(inputMap, "CurvedLeft");
		getActionMap().put("CurvedRight", new CurvedRightAction());
		kbl.assignKeys(inputMap, "CurvedRight");
		getActionMap().put("Straight", new StraightAction());
		kbl.assignKeys(inputMap, "Straight");
		getActionMap().put("EnableRoute", new EnableRouteAction());
		kbl.assignKeys(inputMap, "EnableRoute");
		getActionMap().put("DisableRoute", new DisableRouteAction());
		kbl.assignKeys(inputMap, "DisableRoute");
	}

	private void updateHistory(Object obj) {
		if (historyStack.size() == HISTORY_LENGTH) {
			historyStack.removeLast();
			historyWidgets.removeLast();
		}
		if(!historyStack.isEmpty() && historyStack.getFirst().equals(obj)) {
			historyStack.removeFirst();
			historyWidgets.removeFirst();
		}
		historyStack.addFirst(obj);
		JPanel w = null;
		TurnoutControlIface turnoutControl = AdHocRailway.getInstance()
				.getTurnoutControl();

		if (obj instanceof Turnout) {
			Turnout turnout = (Turnout) obj;
			w = new StaticTurnoutWidget(turnout, turnoutControl
					.getTurnoutState(turnout));
		} else if (obj instanceof Route) {
			w = new RouteWidget((Route) obj);
		} else {
			return;
		}
		historyWidgets.addFirst(w);
		updateHistory();
	}

	private void updateHistory() {
		switchesHistory.removeAll();

		for (JPanel p : historyWidgets) {
			switchesHistory.add(p);
		}
		revalidate();
		repaint();
	}

    private class NumberEnteredAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            enteredNumberKeys.append(e.getActionCommand());
            String switchNumberAsString = enteredNumberKeys.toString();
            int switchNumber = Integer.parseInt(switchNumberAsString);
            if (switchNumber > 999) {
                digitDisplay.reset();
                enteredNumberKeys = new StringBuffer();
                return;
            }
            digitDisplay.setNumber(switchNumber);
        }
    }

    private class PeriodEnteredAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            routeMode = true;
            digitDisplay.setPeriod(true);
        }
    }

    private abstract class SwitchingAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {

            try {
                RouteControlIface routeControl = AdHocRailway.getInstance()
                        .getRouteControl();
                TurnoutControlIface turnoutControl = AdHocRailway.getInstance()
                        .getTurnoutControl();

                String enteredNumberAsString = enteredNumberKeys.toString();
                if (enteredNumberKeys.toString().equals("")) {
                    if (historyStack.size() == 0)
                        return;
                    Object obj = historyStack.removeFirst();
                    if (obj instanceof Turnout) {
                        Turnout t = (Turnout) obj;
                        turnoutControl.setDefaultState(t);
                    } else if (obj instanceof Route) {
                        Route r = (Route) obj;
                        routeControl.disableRoute(r);
                    } else {
                        return;
                    }
                    historyWidgets.removeFirst();
                    updateHistory();
                } else {
                    int enteredNumber = Integer.parseInt(enteredNumberAsString);
                    if (routeMode) {
                        handleRouteChange(e, enteredNumber);
                    } else {
                        handleSwitchChange(e, enteredNumber);
                    }
                }

            } catch (RouteException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            } catch (TurnoutException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
            enteredNumberKeys = new StringBuffer();
            routeMode = false;
            digitDisplay.reset();
        }

        private void handleSwitchChange(ActionEvent e, int enteredNumber)
                throws TurnoutException {
            TurnoutPersistenceIface turnoutPersistence = AdHocRailway
                    .getInstance().getTurnoutPersistence();
            Turnout searchedTurnout = null;
            searchedTurnout = turnoutPersistence
                    .getTurnoutByNumber(enteredNumber);
            if (searchedTurnout == null) {
                return;
            }
            TurnoutControlIface turnoutControl = AdHocRailway.getInstance()
                    .getTurnoutControl();

            if (this instanceof CurvedLeftAction) {
                turnoutControl.setCurvedLeft(searchedTurnout);
            } else if (this instanceof StraightAction) {
                turnoutControl.setStraight(searchedTurnout);
            } else if (this instanceof CurvedRightAction) {
                turnoutControl.setCurvedRight(searchedTurnout);
            } else if (this instanceof EnableRouteAction) {
                if (!searchedTurnout.isThreeWay()) {
                    turnoutControl.setNonDefaultState(searchedTurnout);
                }
            } else if (this instanceof DisableRouteAction) {
                if (!searchedTurnout.isThreeWay()) {
                    turnoutControl.setDefaultState(searchedTurnout);
                }
            }
            updateHistory(searchedTurnout);
        }

        private void handleRouteChange(ActionEvent e, int enteredNumber)
                throws TurnoutException, RouteException {
            Route searchedRoute = null;

            RouteControlIface routeControl = AdHocRailway.getInstance()
                    .getRouteControl();
            RoutePersistenceIface routePersistence = AdHocRailway.getInstance()
                    .getRoutePersistence();
            searchedRoute = routePersistence.getRouteByNumber(enteredNumber);
            if (searchedRoute == null) {
                return;
            }
            if (this instanceof EnableRouteAction) {
                routeControl.enableRoute(searchedRoute);
            } else if (this instanceof DisableRouteAction) {
                routeControl.disableRoute(searchedRoute);
            }
            updateHistory(searchedRoute);
        }
    }

    private class CurvedLeftAction extends SwitchingAction {
    }
    
    private class StraightAction extends SwitchingAction {
    }
    
    private class CurvedRightAction extends SwitchingAction {
    }
    
    private class EnableRouteAction extends SwitchingAction {
    }
    
    private class DisableRouteAction extends SwitchingAction {
    }
    
}
