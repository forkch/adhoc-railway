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
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;
import ch.fork.AdHocRailway.ui.utils.ThreeDigitDisplay;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

public class KeyControl extends JPanel {

    private final LinkedList<Object> historyObjectsStack = new LinkedList<Object>();

    private final LinkedList<JPanel> historyWidgetsStack = new LinkedList<JPanel>();
    private final ApplicationContext ctx;
    private KeyControlMode mode = KeyControlMode.TURNOUT_MODE;
    private int locomotiveNumber = -1;
    private StringBuffer enteredNumberKeys;
    private JPanel turnoutsHistoryPanel;
    private JScrollPane historyPane;
    private ThreeDigitDisplay digitDisplay;

    public KeyControl(final ApplicationContext ctx) {
        this.ctx = ctx;
        enteredNumberKeys = new StringBuffer();
        initGUI();
        initKeyboardActions();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        final JPanel segmentPanelNorth = initSegmentPanel();
        turnoutsHistoryPanel = new JPanel(new MigLayout("insets 5"));

        historyPane = new JScrollPane(turnoutsHistoryPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(segmentPanelNorth, BorderLayout.NORTH);
        add(historyPane, BorderLayout.CENTER);
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

        historyObjectsStack.addLast(obj);
        JPanel w = null;

        if (obj instanceof Turnout) {
            final Turnout turnout = (Turnout) obj;
            w = new TurnoutWidget(ctx, turnout, true);
        } else if (obj instanceof Route) {
            w = new RouteWidget(ctx, (Route) obj, false);

        } else {
            return;
        }
        historyWidgetsStack.addLast(w);
        updateHistoryPanel();
    }

    private void updateHistoryPanel() {
        turnoutsHistoryPanel.removeAll();

        for (final JPanel p : historyWidgetsStack) {
            turnoutsHistoryPanel.add(p, "wrap");
        }
        revalidate();
        repaint();
    }

    private void reset() {
        enteredNumberKeys = new StringBuffer();
        mode = KeyControlMode.TURNOUT_MODE;
        locomotiveNumber = -1;
        digitDisplay.reset();
    }

    private enum KeyControlMode {
        TURNOUT_MODE, ROUTE_MODE, LOCOMOTIVE_FUNCTION_MODE;

        public boolean isRouteMode() {
            return this.equals(KeyControlMode.ROUTE_MODE);
        }

        public boolean isLocomotiveFunctionMode() {
            return this.equals(KeyControlMode.LOCOMOTIVE_FUNCTION_MODE);
        }

    }

    private class NumberEnteredAction extends AbstractAction {


        @Override
        public void actionPerformed(final ActionEvent e) {
            enteredNumberKeys.append(e.getActionCommand());
            final String switchNumberAsString = enteredNumberKeys.toString();
            final int switchNumber = Integer.parseInt(switchNumberAsString);
            if (switchNumber > 999) {
                reset();
                return;
            }
            digitDisplay.setNumber(switchNumber);
        }
    }

    private class PeriodEnteredAction extends AbstractAction {


        @Override
        public void actionPerformed(final ActionEvent e) {
            if (enteredNumberKeys.length() == 0 && mode.isRouteMode()) {
                // reset if no number is entered
                reset();
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

        @Override
        public void actionPerformed(final ActionEvent e) {

            final String enteredNumberAsString = enteredNumberKeys
                    .toString();
            if (enteredNumberKeys.toString().equals("")) {
                if (historyObjectsStack.size() == 0) {
                    return;
                }
                final TurnoutController turnoutControl = ctx
                        .getTurnoutControl();
                final RouteController routeControl = ctx
                        .getRouteControl();
                final Object obj = historyObjectsStack.removeFirst();
                if (obj instanceof Turnout) {

                    final Turnout t = (Turnout) obj;
                    if(t.isLinkedToRoute()) {
                        routeControl.disableRoute(ctx.getRouteForNumber(t.getLinkedRouteNumber()));
                    }else {
                        turnoutControl.setDefaultState(t);
                    }
                } else if (obj instanceof Route) {
                    final Route r = (Route) obj;

                    routeControl.disableRoute(r);
                } else {
                    return;
                }
                historyWidgetsStack.removeFirst();
                updateHistoryPanel();
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

            reset();
        }

        private void handleSwitchChange(final ActionEvent e,
                                        final int enteredNumber) {

            Turnout searchedTurnout = null;
            searchedTurnout = ctx.getTurnoutManager().getTurnoutByNumber(enteredNumber);
            if (searchedTurnout == null) {
                return;
            }

            updateHistory(searchedTurnout);
            if (searchedTurnout.isLinkedToRoute()) {
                RouteController routeControl = ctx.getRouteControl();
                Route routeForNumber = ctx.getRouteForNumber(searchedTurnout.getLinkedRouteNumber());
                if (this instanceof EnableRouteAction) {
                    routeControl.enableRoute(routeForNumber);
                } else {
                    routeControl.disableRoute(routeForNumber);
                }
            } else {
                final TurnoutController turnoutControl = ctx.getTurnoutControl();
                if (this instanceof CurvedLeftAction) {
                    turnoutControl.setCurvedLeft(searchedTurnout);
                } else if (this instanceof StraightAction) {
                    turnoutControl.setStraight(searchedTurnout);
                } else if (this instanceof CurvedRightAction) {
                    turnoutControl.setCurvedRight(searchedTurnout);
                } else if (this instanceof EnableRouteAction) {
                    turnoutControl.setNonDefaultState(searchedTurnout);
                } else {
                    turnoutControl.setDefaultState(searchedTurnout);
                }
            }
        }

        private void handleRouteChange(final ActionEvent e,
                                       final int enteredNumber) {
            Route searchedRoute = null;


            searchedRoute = ctx.getRouteManager().getRouteByNumber(
                    enteredNumber);
            if (searchedRoute == null) {
                return;
            }

            updateHistory(searchedRoute);

            final RouteController routeControl = ctx.getRouteControl();
            if (this instanceof EnableRouteAction) {
                routeControl.enableRoute(searchedRoute);
            } else if (this instanceof DisableRouteAction) {
                routeControl.disableRoute(searchedRoute);
            }
        }

        private void handleLocomotiveChange(final ActionEvent e,
                                            final int functionNumber) {

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
