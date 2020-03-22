/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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

package ch.fork.adhocrailway.ui.turnouts;

import ch.fork.adhocrailway.controllers.RouteChangeListener;
import ch.fork.adhocrailway.controllers.RouteController;
import ch.fork.adhocrailway.controllers.TurnoutChangeListener;
import ch.fork.adhocrailway.controllers.TurnoutController;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutState;
import ch.fork.adhocrailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.adhocrailway.ui.context.TurnoutContext;
import ch.fork.adhocrailway.ui.turnouts.configuration.TurnoutConfig;
import ch.fork.adhocrailway.ui.turnouts.configuration.TurnoutHelper;
import ch.fork.adhocrailway.ui.utils.UIConstants;
import com.google.common.eventbus.Subscribe;
import com.jgoodies.common.base.SystemUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TurnoutWidget extends JPanel implements TurnoutChangeListener, RouteChangeListener {

    private final boolean testMode;
    private final boolean forHistory;
    private final TurnoutManager turnoutManager;
    private final TurnoutContext ctx;
    private Turnout turnout;
    private JLabel numberLabel;
    private TurnoutCanvas turnoutCanvas;
    private TurnoutState actualTurnoutState = TurnoutState.UNDEF;
    private JPanel statePanel;

    public TurnoutWidget(final TurnoutContext ctx, final Turnout turnout,
                         final boolean forHistory) {
        this(ctx, turnout, forHistory, false);
    }

    public TurnoutWidget(final TurnoutContext ctx, final Turnout turnout,
                         final boolean forHistory, final boolean testMode) {
        this.ctx = ctx;
        this.turnout = turnout;
        this.forHistory = forHistory;
        this.testMode = testMode;

        turnoutManager = ctx.getTurnoutManager();

        ctx.getMainBus().register(this);

        initGUI();
        updateTurnout();
        TurnoutHelper.validateTurnout(turnoutManager, turnout, this);
        final boolean connected = ctx.getRailwayDeviceManager().isConnected();
        if (connected) {
            connectedToRailwayDevice(new ConnectedToRailwayEvent(connected));
        }
    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        if (event.isConnected()) {
            ctx.getTurnoutControl().addTurnoutChangeListener(turnout, this);

            linkRouteAndAddListener();

            TurnoutState turnoutState = ctx.getTurnoutControl().getStateFromDevice(turnout);
            turnout.setActualState(turnoutState);
            turnoutChanged(turnout);
        } else {
            ctx.getTurnoutControl().removeTurnoutChangeListener(turnout, this);
            if (turnout.isLinkedToRoute() && turnout.getLinkedRoute() != null) {
                ctx.getRouteControl().removeRouteChangeListener(turnout.getLinkedRoute(), this);
            }
        }
    }


    private void initGUI() {
        turnoutCanvas = new TurnoutCanvas(turnout);
        turnoutCanvas.addMouseListener(new MouseAction());
        addMouseListener(new MouseAction());

        if (SystemUtils.IS_OS_MAC) {
            Border aquaBorder = UIManager.getBorder("InsetBorder.aquaVariant");
            setBorder(aquaBorder);
        } else {
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        }

        numberLabel = new JLabel();
        numberLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        statePanel = new JPanel();


        if (SystemUtils.IS_OS_MAC) {
            setLayout(new MigLayout("insets 2, gap 5"));
        } else {
            setLayout(new MigLayout("insets 5, gap 2"));
        }

        if (forHistory) {
            add(numberLabel);
            add(turnoutCanvas);
        } else {
            add(numberLabel, "");
            add(statePanel, "wrap, w 20!, h 7!, align right");
            add(turnoutCanvas, "span 2");
        }

    }

    public void updateTurnout() {
        numberLabel.setText(Integer.toString(turnout.getNumber()));

        final String turnoutDescription = TurnoutHelper
                .getTurnoutDescription(turnout);
        //setToolTipText(turnoutDescription);
        //turnoutCanvas.setToolTipText(turnoutDescription);

        linkRouteAndAddListener();

    }

    private void linkRouteAndAddListener() {
        ctx.getRouteControl().removeRouteChangeListener(turnout.getLinkedRoute(), this);
        if (turnout.isLinkedToRoute() && turnout.getLinkedRoute() == null) {
            final Route routeForNumber = ctx.getRouteForNumber(turnout.getLinkedRouteNumber());
            if (routeForNumber != null) {
                turnout.setLinkedRoute(routeForNumber);
            }
        }
        ctx.getRouteControl().addRouteChangeListener(turnout.getLinkedRoute(), this);
    }

    public Turnout getTurnout() {
        return turnout;
    }

    public void setTurnout(final Turnout turnout) {
        this.turnout = turnout;
        updateTurnout();
    }

    @Override
    public void turnoutChanged(final Turnout changedTurnout) {

        if (turnout.equals(changedTurnout)) {
            actualTurnoutState = changedTurnout.getActualState();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    numberLabel.setText(Integer.toString(turnout.getNumber()));
                    if (actualTurnoutState == null) {
                        actualTurnoutState = TurnoutState.UNDEF;
                    }
                    turnoutCanvas.setTurnoutState(actualTurnoutState);
                    switch (actualTurnoutState) {
                        case LEFT:
                        case RIGHT:
                            if (turnout.getDefaultState().equals(
                                    TurnoutState.STRAIGHT)) {
                                statePanel.setBackground(UIConstants.STATE_RED);
                            } else {
                                statePanel.setBackground(UIConstants.STATE_GREEN);
                            }

                            break;
                        case STRAIGHT:
                            if (turnout.getDefaultState().equals(
                                    TurnoutState.STRAIGHT)) {
                                statePanel.setBackground(UIConstants.STATE_GREEN);
                            } else {
                                statePanel.setBackground(UIConstants.STATE_RED);
                            }

                            break;
                        case UNDEF:
                        default:
                            statePanel.setBackground(Color.GRAY);
                            break;

                    }

                }
            });
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            setBackground(new Color(255, 177, 177));
        }
        turnoutCanvas.setTurnoutState(TurnoutState.UNDEF);
    }

    public void revalidateTurnout() {
        TurnoutHelper.validateTurnout(turnoutManager, turnout, this);
    }

    @Override
    public void nextTurnoutRouted(Route changedRoute) {

    }

    @Override
    public void nextTurnoutDerouted(Route changedRoute) {

    }

    @Override
    public void routeChanged(Route changedRoute) {

        if (turnout.isLinkedToRoute() && changedRoute.equals(turnout.getLinkedRoute())) {
            turnout.setActualState(changedRoute.isEnabled() ? TurnoutState.LEFT : TurnoutState.STRAIGHT);
            turnoutChanged(turnout);
        }
    }

    private class MouseAction extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {
            linkRouteAndAddListener();
            if (e.getButton() == MouseEvent.BUTTON1) {
                handleLeftClick();
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                handleRightClick();
            }
        }

        private void handleRightClick() {
            if (ctx.isEditingMode()) {
                displaySwitchConfig();
            }
        }

        private void handleLeftClick() {
            if (!ctx.getRailwayDeviceManager().isConnected()) {
                return;
            }

            if (turnout.isLinkedToRoute()) {
                RouteController routeControl = ctx.getRouteControl();
                Route routeForNumber = ctx.getRouteForNumber(turnout.getLinkedRouteNumber());
                if (!testMode) {
                    routeControl.toggle(routeForNumber);
                } else {
                    routeControl.toggleTest(routeForNumber);
                }
            } else {
                final TurnoutController turnoutControl = ctx
                        .getTurnoutControl();
                if (!testMode) {
                    turnoutControl.toggle(turnout);
                } else {
                    turnoutControl.toggleTest(turnout);
                }
            }
        }

        private void displaySwitchConfig() {
            if (testMode) {
                return;
            }

            final TurnoutController turnoutControl = ctx.getTurnoutControl();
            turnoutControl.removeTurnoutChangeListener(turnout, TurnoutWidget.this);
            new TurnoutConfig(ctx.getMainFrame(), ctx, turnout,
                    turnout.getTurnoutGroup(), false);
            TurnoutHelper.validateTurnout(turnoutManager, turnout,
                    TurnoutWidget.this);

            turnoutControl.addTurnoutChangeListener(turnout, TurnoutWidget.this);
            turnoutChanged(turnout);
        }
    }
}
