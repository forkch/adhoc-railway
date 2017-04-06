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

package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.manager.LocomotiveManagerListener;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToPersistenceEvent;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.bus.events.StartImportEvent;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;
import ch.fork.AdHocRailway.ui.utils.UIConstants;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static ch.fork.AdHocRailway.ui.utils.ImageTools.createImageIcon;
import static ch.fork.AdHocRailway.ui.utils.ImageTools.createImageIconFromIconSet;

public class LocomotiveWidget extends JPanel implements
        LocomotiveChangeListener, LocomotiveManagerListener, LocomotiveSelectionPanel.OnLocomotiveSelectionListener {

    public static final int INCREASE_STEPS_AFTER_MS = 500;
    private static final Logger LOGGER = Logger.getLogger(LocomotiveWidget.class);
    private static final long DISABLE_STEPS_AFTER_MS = 600;
    private final int number;
    private final List<FunctionToggleButton> functionToggleButtons = new ArrayList<FunctionToggleButton>();
    private final JFrame frame;
    private final LocomotiveContext ctx;
    public boolean directionToggeled;
    private JProgressBar speedBar;
    private JButton increaseSpeed;
    private JButton decreaseSpeed;
    private JButton stopButton;
    private JButton directionButton;
    private LockToggleButton lockButton;
    private Locomotive myLocomotive;
    private JPanel functionsPanel;
    private boolean disableListener;
    private LocomotiveSelectionPanel locomotiveSelectionPanel;

    public LocomotiveWidget(final LocomotiveContext ctx, final int number,
                            final JFrame frame) {
        super();
        this.ctx = ctx;
        this.number = number;
        this.frame = frame;

        ctx.getMainBus().register(this);
        initGUI();
        initKeyboardActions();

        if (ctx.getRailwayDeviceManager() != null) {
            connectedToRailwayDevice(new ConnectedToRailwayEvent(ctx.getRailwayDeviceManager().isConnected()));
        }
    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        if (event.isConnected()) {
            if (myLocomotive != null) {
                ctx.getLocomotiveControl().addLocomotiveChangeListener(
                        myLocomotive, this);
            }
        } else {
            ctx.getLocomotiveControl().removeLocomotiveChangeListener(myLocomotive, this);
        }
    }

    @Subscribe
    public void connectedToPersistence(final ConnectedToPersistenceEvent event) {
        ctx.getLocomotiveManager().addLocomotiveManagerListener(this);
    }

    @Subscribe
    public void startImport(final StartImportEvent event) {
        disableListener = true;
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        disableListener = false;
        locomotiveSelectionPanel.updateLocomotiveGroups();
    }


    public Locomotive getMyLocomotive() {
        return myLocomotive;
    }

    private void initGUI() {

        setLayout(new MigLayout("wrap 3, insets 5, gap 5"));

        locomotiveSelectionPanel = new LocomotiveSelectionPanel(this, ctx);

        final JPanel controlPanel = initControlPanel();

        addMouseListener(new MouseAction());
        add(locomotiveSelectionPanel, "span 3, grow, wrap");
        add(controlPanel, "span 3, grow");

        addMouseWheelListener(new WheelControl());

    }


    private JPanel initControlPanel() {
        final JPanel controlPanel = new JPanel(new MigLayout("insets 0, fill"));

        speedBar = new JProgressBar(SwingConstants.VERTICAL);

        speedBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                processMouseMovement(e);
            }
        });

        speedBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseMoved(e);
                processMouseMovement(e);
            }
        });

        final JPanel functionsPanel = initFunctionsControl();
        final JPanel speedControlPanel = initSpeedControl();

        controlPanel.add(functionsPanel, "west, grow");
        controlPanel.add(speedControlPanel, "gap 5, grow");
        controlPanel.add(speedBar, "east, gap 5, width 40");

        return controlPanel;
    }

    private JPanel initFunctionsControl() {
        functionsPanel = new JPanel();
        functionsPanel.setLayout(new MigLayout("insets 0, wrap, fill"));
        if (myLocomotive == null) {
            for (int i = 0; i < 6; i++) {
                final FunctionToggleButton functionButton = new FunctionToggleButton(
                        "F" + i);
                functionToggleButtons.add(functionButton);
                functionButton.setFocusable(false);
                functionsPanel.add(functionButton, getFunctionButtonParams());
                functionButton.setEnabled(false);
            }
        }

        return functionsPanel;
    }

    private JPanel initSpeedControl() {

        final JPanel speedControlPanel = new JPanel();
        speedControlPanel.setLayout(new MigLayout("insets 0, wrap 1, fill"));

        increaseSpeed = new JButton("+");
        decreaseSpeed = new JButton("-");
        stopButton = new JButton("Stop");
        directionButton = new JButton(
                createImageIcon("crystal/forward.png"));

        lockButton = new LockToggleButton("");

        lockButton.setVisible(false);

        increaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        decreaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        directionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        increaseSpeed.addActionListener(new LocomotiveAccelerateAction());
        decreaseSpeed.addActionListener(new LocomotiveDeccelerateAction());
        stopButton.addActionListener(new StopAction());
        directionButton.addActionListener(new ToggleDirectionAction());
        lockButton.addActionListener(new LockAction());

        increaseSpeed.setFocusable(false);
        decreaseSpeed.setFocusable(false);
        stopButton.setFocusable(false);
        directionButton.setFocusable(false);
        lockButton.setFocusable(false);

        String params = getFunctionButtonParams();
        speedControlPanel.add(increaseSpeed, params);
        speedControlPanel.add(decreaseSpeed, params);
        speedControlPanel.add(stopButton, params);
        speedControlPanel.add(directionButton, params);
        speedControlPanel.add(lockButton, params);
        return speedControlPanel;
    }

    private void initKeyboardActions() {
        final KeyBoardLayout kbl = Preferences.getInstance()
                .getKeyBoardLayout();
        final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        getActionMap().put("Accelerate" + number,
                new LocomotiveAccelerateAction());
        kbl.assignKeys(inputMap, "Accelerate" + number);
        getActionMap().put("Deccelerate" + number,
                new LocomotiveDeccelerateAction());
        kbl.assignKeys(inputMap, "Deccelerate" + number);
        getActionMap().put("ToggleDirection" + number,
                new LocomotiveToggleDirectionAction());
        kbl.assignKeys(inputMap, "ToggleDirection" + number);
    }

    private void updateFunctionButtons() {
        functionToggleButtons.clear();
        functionsPanel.removeAll();
        if (myLocomotive.getFunctions().size() <= 5) {
            functionsPanel.setLayout(new MigLayout("insets 0, wrap, fill")); // wrap after each function button
        } else if (myLocomotive.getFunctions().size() <= 10) {
            functionsPanel.setLayout(new MigLayout("insets 0, wrap 2, fill")); // wrap after every second function button
        } else {
            functionsPanel.setLayout(new MigLayout("insets 0, wrap 3, fill")); // wrap after every third function button
        }


        String params = getFunctionButtonParams();

        for (final LocomotiveFunction fn : myLocomotive.getFunctions()) {
            final FunctionToggleButton functionButton = new FunctionToggleButton(
                    fn.getShortDescription(myLocomotive.getType() == LocomotiveType.DCC));
            functionToggleButtons.add(functionButton);
            final int i = functionToggleButtons.indexOf(functionButton);
            functionButton.addActionListener(new LocomotiveFunctionAction(i));
            functionButton.setToolTipText(fn.getDescription());

            functionButton.setFocusable(false);
            functionsPanel.add(functionButton, params);
        }
        revalidate();
        repaint();
    }

    private String getFunctionButtonParams() {
        String params = "height 30, growx";
        if (Preferences.getInstance().getBooleanValue(PreferencesKeys.TABLET_MODE)) {
            params = "height " + UIConstants.SIZE_TABLET + ", growx";
        }
        return params;
    }

    private void updateWidget() {
        if (myLocomotive == null) {

            return;
        }
        final LocomotiveController locomotiveControl = ctx
                .getLocomotiveControl();

        if (ctx.getRailwayDeviceManager().isConnected()) {
            locomotiveControl.activateLoco(myLocomotive);
        }
        final int currentSpeed = myLocomotive.getCurrentSpeed();

        updateSpeed(currentSpeed);
        LOGGER.info("speed: " + currentSpeed);

        updateFunctions();

        updateDirection();

        if (isFree()) {
            locomotiveSelectionPanel.locomotiveFree();
        } else {
            UIManager.put("ComboBox.disabledForeground", new ColorUIResource(
                    Color.BLACK));
            locomotiveSelectionPanel.locomotiveInUse();
        }
        speedBar.requestFocus();

    }

    private void updateSpeed(int currentSpeed) {
        final float speedInPercent = ((float) currentSpeed)
                / ((float) myLocomotive.getType().getDrivingSteps());

        final float hue = (1.0f - speedInPercent) * 0.3f;
        final Color speedColor = Color.getHSBColor(hue, 1.0f, 1.0f);

        speedBar.setForeground(speedColor);
        speedBar.setMinimum(0);
        speedBar.setMaximum(myLocomotive.getType().getDrivingSteps());
        speedBar.setValue(currentSpeed);
    }

    private void updateDirection() {
        switch (myLocomotive.getCurrentDirection()) {
            case FORWARD:
                directionButton.setIcon(createImageIconFromIconSet("forward.png"));
                break;
            case REVERSE:
                directionButton.setIcon(createImageIconFromIconSet("back.png"));
                break;
            default:
                directionButton.setIcon(createImageIconFromIconSet("forward.png"));
        }
    }

    private void updateFunctions() {
        final boolean[] functions = myLocomotive.getCurrentFunctions();
        for (int i = 0; i < functions.length; i++) {
            functionToggleButtons.get(i).setSelected(functions[i]);
        }

        if (myLocomotive.getType().getFunctionCount() == 0) {
            for (final FunctionToggleButton b : functionToggleButtons) {
                b.setEnabled(false);
            }
        } else {
            for (final FunctionToggleButton b : functionToggleButtons) {
                b.setEnabled(true);
            }
        }
    }

    private boolean isFree() {
        return myLocomotive == null || myLocomotive.getCurrentSpeed() == 0;
    }

    @Override
    public void locomotiveChanged(final Locomotive changedLocomotive) {
        if (myLocomotive == null) {
            return;
        }

        if (myLocomotive.equals(changedLocomotive)) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateWidget();
                }
            });
        }
    }

    @Override
    public void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> locomotiveGroups) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();

    }

    private boolean isCurrentlyInUse() {
        return !isFree() || myLocomotive != null;
    }

    @Override
    public void locomotiveAdded(final Locomotive locomotive) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();
    }

    @Override
    public void locomotiveUpdated(final Locomotive locomotive) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();

    }

    @Override
    public void locomotiveGroupAdded(final LocomotiveGroup group) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();

    }

    @Override
    public void locomotiveRemoved(final Locomotive locomotive) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();
    }

    @Override
    public void locomotiveGroupRemoved(final LocomotiveGroup group) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();

    }

    @Override
    public void locomotiveGroupUpdated(final LocomotiveGroup group) {
        if (disableListener || isCurrentlyInUse()) {
            return;
        }
        locomotiveSelectionPanel.updateLocomotiveGroups();

    }

    private void resetLoco() {
        if (myLocomotive == null) {
            return;
        }
        final LocomotiveController locomotiveControl = ctx
                .getLocomotiveControl();
        locomotiveControl.terminateLocomotive(myLocomotive);
        locomotiveControl.removeLocomotiveChangeListener(myLocomotive, this);
        myLocomotive = null;
        updateWidget();

    }

    private void processMouseMovement(MouseEvent e) {
        if (myLocomotive == null) {
            return;
        }
        double i = (double) e.getY() / speedBar.getHeight();
        int drivingSteps = myLocomotive.getType().getDrivingSteps();
        int newSpeed = (int) ((1 - i) * (drivingSteps + 1));
        newSpeed = Math.max(0, Math.min(drivingSteps, newSpeed));
        if (newSpeed != myLocomotive.getCurrentSpeed()) {
            ctx.getLocomotiveControl().setSpeed(myLocomotive, newSpeed, myLocomotive.getCurrentFunctions());
        }
    }

    @Override
    public void failure(
            final AdHocServiceException serviceException) {
    }

    @Override
    public void onLocomotiveGroupSelected(LocomotiveGroup selectedLocomotiveGroup) {
        resetLoco();
    }

    @Override
    public void onLocomotiveSelected(Locomotive selectedLocomotive) {
        try {
            if (selectedLocomotive == null) {
                resetLoco();
                return;
            }

            if (myLocomotive != null && ctx.getRailwayDeviceManager().isConnected()
                    ) {
                resetLoco();
            }

            myLocomotive = selectedLocomotive;

            ctx.getLocomotiveManager().setActiveLocomotive(number, myLocomotive);
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();
            locomotiveControl.addLocomotiveChangeListener(myLocomotive,
                    LocomotiveWidget.this);
            lockButton.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
            updateFunctionButtons();
            updateWidget();

        } catch (final ControllerException e1) {
            ctx.getMainApp().handleException(e1);
        }
    }

    public void emergencyStop() {
        updateWidget();
    }

    private abstract class LocomotiveControlAction extends AbstractAction {

        private long time = 0;

        @Override
        public void actionPerformed(final ActionEvent e) {

            if (myLocomotive == null || !ctx.getRailwayDeviceManager().isConnected()) {
                return;
            }
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();

            doPerformAction(locomotiveControl, myLocomotive);


            speedBar.requestFocus();
        }

        protected abstract void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive);
    }

    private class LocomotiveFunctionAction extends LocomotiveControlAction {

        private final int function;

        public LocomotiveFunctionAction(final int function) {
            this.function = function;
        }

        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {
            final boolean state = functionToggleButtons.get(function)
                    .isSelected();

            final LocomotiveFunction locomotiveFunction = myLocomotive
                    .getFunction(function);
            final int deactivationDelay = locomotiveFunction != null ? locomotiveFunction
                    .getDeactivationDelay() : -1;
            locomotiveControl.setFunction(myLocomotive, function, state,
                    deactivationDelay);

            speedBar.requestFocus();
        }
    }

    private class LocomotiveAccelerateAction extends LocomotiveControlAction {

        private long lastReset = 0;
        private long disableFastIn;

        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {

            if (lastReset == 0 || System.currentTimeMillis() > disableFastIn) {
                lastReset = System.currentTimeMillis();
                disableFastIn = (System.currentTimeMillis() + DISABLE_STEPS_AFTER_MS);
            }

            long sinceLastReset = System.currentTimeMillis() - lastReset;

            if (sinceLastReset > INCREASE_STEPS_AFTER_MS && System.currentTimeMillis() < disableFastIn) {
                locomotiveControl.increaseSpeed(myLocomotive, 5);
                disableFastIn = (System.currentTimeMillis() + DISABLE_STEPS_AFTER_MS);
            } else {
                locomotiveControl.increaseSpeed(myLocomotive, 1);
            }
            updateSpeed(myLocomotive.getCurrentOrTargetSpeed());
        }
    }

    private class LocomotiveDeccelerateAction extends LocomotiveControlAction {


        private long lastReset = 0;
        private long disableFastIn;

        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {

            if (lastReset == 0 || System.currentTimeMillis() > disableFastIn) {
                lastReset = System.currentTimeMillis();
                disableFastIn = (System.currentTimeMillis() + DISABLE_STEPS_AFTER_MS);
            }

            long sinceLastReset = System.currentTimeMillis() - lastReset;

            if (sinceLastReset > INCREASE_STEPS_AFTER_MS && System.currentTimeMillis() < disableFastIn) {
                locomotiveControl.decreaseSpeed(myLocomotive, 5);
                disableFastIn = System.currentTimeMillis() + DISABLE_STEPS_AFTER_MS;
            } else {
                locomotiveControl.decreaseSpeed(myLocomotive, 1);

            }
            updateSpeed(myLocomotive.getCurrentOrTargetSpeed());
        }
    }

    private class LocomotiveToggleDirectionAction extends
            LocomotiveControlAction {

        @Override
        protected void doPerformAction(
                final LocomotiveController locomotiveControl,
                final Locomotive myLocomotive) {
            if (Preferences.getInstance().getBooleanValue(
                    PreferencesKeys.STOP_ON_DIRECTION_CHANGE)
                    && myLocomotive.getCurrentSpeed() != 0) {
                locomotiveControl.setSpeed(myLocomotive, 0,
                        myLocomotive.getCurrentFunctions());
            }
            directionToggeled = true;
            locomotiveControl.toggleDirection(myLocomotive);
        }
    }

    private class StopAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myLocomotive == null) {
                return;
            }
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();
            locomotiveControl.setSpeed(myLocomotive, 0,
                    myLocomotive.getCurrentFunctions());
            updateWidget();
            speedBar.requestFocus();
        }
    }

    private class ToggleDirectionAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myLocomotive == null) {
                return;
            }
            final LocomotiveController locomotiveControl = ctx
                    .getLocomotiveControl();
            if (Preferences.getInstance().getBooleanValue(
                    PreferencesKeys.STOP_ON_DIRECTION_CHANGE)
                    && myLocomotive.getCurrentSpeed() != 0) {
                locomotiveControl.setSpeed(myLocomotive, 0,
                        myLocomotive.getCurrentFunctions());
            }
            directionToggeled = true;
            locomotiveControl.toggleDirection(myLocomotive);
            speedBar.requestFocus();
        }
    }

    private class LockAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (myLocomotive == null) {
                return;
            }
            final boolean lockButtonState = lockButton.isSelected();
            try {
                final LocomotiveController locomotiveControl = ctx
                        .getLocomotiveControl();
//                if (lockButtonState) {
//                    final boolean succeeded = locomotiveControl
//                            .acquireLock(myLocomotive);
//                    lockButton.setSelected(succeeded);
//                } else {
//                    if (locomotiveControl.isLockedByMe(myLocomotive)) {
//                        final boolean succeeded = !locomotiveControl
//                                .releaseLock(myLocomotive);
//                        lockButton.setSelected(succeeded);
//                    } else {
//                        lockButton.setSelected(true);
//                    }
//                }
                speedBar.requestFocus();
            } catch (final ControllerException ex) {
                ctx.getMainApp().handleException(ex);
//                lockButton.setSelected(lockButtonState);
            }
            updateWidget();
        }
    }

    private class MouseAction extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {
            if (myLocomotive == null) {
                return;
            }
            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {

                if (handleLeftClick()) {
                    return;
                }
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                handleMiddleClick();
            }
            updateWidget();
        }

        private void handleMiddleClick() {
            final ToggleDirectionAction a = new ToggleDirectionAction();
            a.actionPerformed(null);
        }

        private boolean handleLeftClick() {
            if (ctx.isEditingMode()) {
                if (!isFree()) {
                    return true;
                }
                final LocomotiveController locomotiveControl = ctx
                        .getLocomotiveControl();

                locomotiveControl.removeLocomotiveChangeListener(
                        myLocomotive, LocomotiveWidget.this);
                new LocomotiveConfig(ctx, frame, myLocomotive, myLocomotive.getGroup(), false);

                locomotiveControl.addLocomotiveChangeListener(myLocomotive,
                        LocomotiveWidget.this);
                locomotiveChanged(myLocomotive);

            }
            return false;
        }
    }

    private class WheelControl implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            if (myLocomotive == null) {
                return;
            }
            AbstractAction a;
            switch (e.getWheelRotation()) {
                case -1:
                    a = new LocomotiveAccelerateAction();
                    break;
                case 1:
                    a = new LocomotiveDeccelerateAction();
                    break;
                default:
                    return;
            }
            a.actionPerformed(null);
        }
    }

}
