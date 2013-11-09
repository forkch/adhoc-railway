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

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

public class KeyControl extends SimpleInternalFrame {
	private static final long serialVersionUID = -3052109699874529256L;

	private enum KeyControlMode {
		TURNOUT_MODE, ROUTE_MODE, LOCOMOTIVE_FUNCTION_MODE;

		public boolean isRouteMode() {
			return this.equals(KeyControlMode.ROUTE_MODE);
		}

		public boolean isLocomotiveFunctionMode() {
			return this.equals(KeyControlMode.LOCOMOTIVE_FUNCTION_MODE);
		}

	};

	private KeyControlMode mode = KeyControlMode.TURNOUT_MODE;
	private int locomotiveNumber = -1;

	private StringBuffer enteredNumberKeys;

	private JPanel turnoutsHistory;

	private final LinkedList<Object> historyStack = new LinkedList<Object>();;

	private final LinkedList<JPanel> historyWidgets = new LinkedList<JPanel>();;

	private JScrollPane historyPane;

	private ThreeDigitDisplay digitDisplay;
	private final RouteController routeControl;
	private final TurnoutController turnoutControl;
	private final TurnoutManager turnoutManager;
	private final ApplicationContext ctx;

	public KeyControl(final ApplicationContext ctx) {
		super("Track Control / History");
		this.ctx = ctx;

		routeControl = ctx.getRouteControl();
		turnoutControl = ctx.getTurnoutControl();
		turnoutManager = ctx.getTurnoutManager();

		enteredNumberKeys = new StringBuffer();
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		final JPanel segmentPanelNorth = initSegmentPanel();
		turnoutsHistory = new JPanel();
		final JPanel sh1 = new JPanel(new BorderLayout());

		turnoutsHistory.setLayout(new MigLayout("insets 5"));

		historyPane = new JScrollPane(turnoutsHistory,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sh1.add(historyPane, BorderLayout.CENTER);

		add(segmentPanelNorth, BorderLayout.NORTH);
		add(sh1, BorderLayout.CENTER);
	}

	private JPanel initSegmentPanel() {
		digitDisplay = new ThreeDigitDisplay();
		final JPanel p = new JPanel(new BorderLayout());
		p.add(digitDisplay, BorderLayout.WEST);
		return p;
	}

	private void initKeyboardActions() {
		for (int i = 0; i <= 10; i++) {
			registerKeyboardAction(new NumberEnteredAction(),
					Integer.toString(i),
					KeyStroke.getKeyStroke(Integer.toString(i)),
					WHEN_IN_FOCUSED_WINDOW);
			registerKeyboardAction(new NumberEnteredAction(),
					Integer.toString(i),
					KeyStroke.getKeyStroke("NUMPAD" + Integer.toString(i)),
					WHEN_IN_FOCUSED_WINDOW);

		}
		final KeyBoardLayout kbl = Preferences.getInstance()
				.getKeyBoardLayout();
		final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
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

	private void updateHistory(final Object obj) {
		if (historyStack.size() == UIConstants.HISTORY_LENGTH) {
			historyStack.removeFirst();
			historyWidgets.removeFirst();
		}
		if (!historyStack.isEmpty() && historyStack.getFirst().equals(obj)) {
			historyStack.removeLast();
			historyWidgets.removeLast();
		}
		historyStack.addLast(obj);
		JPanel w = null;

		if (obj instanceof Turnout) {
			final Turnout turnout = (Turnout) obj;
			w = new TurnoutWidget(ctx, turnout, true);
		} else if (obj instanceof Route) {
			w = new RouteWidget(ctx, (Route) obj, false);
		} else {
			return;
		}
		historyWidgets.addLast(w);
		updateHistory();
	}

	private void updateHistory() {
		turnoutsHistory.removeAll();

		for (final JPanel p : historyWidgets) {
			turnoutsHistory.add(p, "wrap");
		}
		revalidate();
		repaint();
	}

	private class NumberEnteredAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4818198896180938380L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			enteredNumberKeys.append(e.getActionCommand());
			final String switchNumberAsString = enteredNumberKeys.toString();
			final int switchNumber = Integer.parseInt(switchNumberAsString);
			if (switchNumber > 999) {
				digitDisplay.reset();
				enteredNumberKeys = new StringBuffer();
				mode = KeyControlMode.TURNOUT_MODE;
				locomotiveNumber = -1;
				return;
			}
			digitDisplay.setNumber(switchNumber);
		}
	}

	private class PeriodEnteredAction extends AbstractAction {

		private static final long serialVersionUID = 6709249386564202875L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (enteredNumberKeys.length() == 0 && mode.isRouteMode()) {
				// reset if no number is entered
				mode = KeyControlMode.TURNOUT_MODE;
				locomotiveNumber = -1;
				digitDisplay.setPeriod(false);
			} else if (enteredNumberKeys.length() != 0 && mode.isRouteMode()) {
				// someone entered a number followed by a period
				mode = KeyControlMode.LOCOMOTIVE_FUNCTION_MODE;
				locomotiveNumber = Integer.parseInt(enteredNumberKeys
						.toString());
				enteredNumberKeys = new StringBuffer();
				digitDisplay.setNumber(-1);
			} else {
				mode = KeyControlMode.ROUTE_MODE;
				digitDisplay.setPeriod(true);
			}
		}
	}

	private abstract class SwitchingAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8783785027663679688L;

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				final String enteredNumberAsString = enteredNumberKeys
						.toString();
				if (enteredNumberKeys.toString().equals("")) {
					if (historyStack.size() == 0) {
						return;
					}
					final Object obj = historyStack.removeFirst();
					if (obj instanceof Turnout) {
						final Turnout t = (Turnout) obj;
						turnoutControl.setDefaultState(t);
					} else if (obj instanceof Route) {
						final Route r = (Route) obj;
						routeControl.disableRoute(r);
					} else {
						return;
					}
					historyWidgets.removeFirst();
					updateHistory();
				} else {
					final int enteredNumber = Integer
							.parseInt(enteredNumberAsString);
					if (mode.isRouteMode()) {
						handleRouteChange(e, enteredNumber);
					} else if (mode.isLocomotiveFunctionMode()) {
						handleLocomotiveChange(e, enteredNumber);
					} else {
						handleSwitchChange(e, enteredNumber);
					}
				}

			} catch (final LocomotiveException | TurnoutException
					| RouteException e1) {
				ctx.getMainApp().handleException(e1);
			}
			enteredNumberKeys = new StringBuffer();
			mode = KeyControlMode.TURNOUT_MODE;
			locomotiveNumber = -1;
			digitDisplay.reset();
		}

		private void handleSwitchChange(final ActionEvent e,
				final int enteredNumber) throws TurnoutException {
			Turnout searchedTurnout = null;
			searchedTurnout = turnoutManager.getTurnoutByNumber(enteredNumber);
			if (searchedTurnout == null) {
				return;
			}

			if (this instanceof CurvedLeftAction) {
				turnoutControl.setCurvedLeft(searchedTurnout);
			} else if (this instanceof StraightAction) {
				turnoutControl.setStraight(searchedTurnout);
			} else if (this instanceof CurvedRightAction) {
				turnoutControl.setCurvedRight(searchedTurnout);
			}
			updateHistory(searchedTurnout);
		}

		private void handleRouteChange(final ActionEvent e,
				final int enteredNumber) throws TurnoutException,
				RouteException {
			Route searchedRoute = null;

			searchedRoute = ctx.getRouteManager().getRouteByNumber(
					enteredNumber);
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

		private void handleLocomotiveChange(final ActionEvent e,
				final int functionNumber) throws LocomotiveException {
			Locomotive searchedLocomotive = null;

			if (functionNumber > 16) {
				return;
			}
			searchedLocomotive = ctx.getLocomotiveManager()
					.getActiveLocomotive(locomotiveNumber - 1);
			if (searchedLocomotive == null) {
				return;
			}

			final LocomotiveController locomotiveControl = ctx
					.getLocomotiveControl();
			final boolean[] functions = searchedLocomotive
					.getCurrentFunctions();
			if (functionNumber >= functions.length) {
				return;
			}

			boolean state = false;
			if (this instanceof EnableRouteAction) {
				state = true;
			} else if (this instanceof DisableRouteAction) {
				state = false;
			}
			final LocomotiveFunction locomotiveFunction = searchedLocomotive
					.getFunction(functionNumber);
			final int deactivationDelay = locomotiveFunction != null ? locomotiveFunction
					.getDeactivationDelay() : -1;
			locomotiveControl.setFunction(searchedLocomotive, functionNumber,
					state, deactivationDelay);
		}
	}

	private class CurvedLeftAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5676902063321467852L;
	}

	private class StraightAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2339006950893044415L;
	}

	private class CurvedRightAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7285117051054231241L;
	}

	private class EnableRouteAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5376121297997351343L;
	}

	private class DisableRouteAction extends SwitchingAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4179628128437613997L;
	}

}
