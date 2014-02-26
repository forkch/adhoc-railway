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

import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIcon;
import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIconFromIconSet;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.bus.events.StartImportEvent;
import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveHelper;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerListener;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;
import ch.fork.AdHocRailway.ui.tools.ImageTools;

import com.google.common.eventbus.Subscribe;

public class LocomotiveWidget extends JPanel implements
		LocomotiveChangeListener, LocomotiveManagerListener {

	private static final long serialVersionUID = -9150574905752177937L;

	private JComboBox<Locomotive> locomotiveComboBox;

	private JComboBox<LocomotiveGroup> locomotiveGroupComboBox;

	private JProgressBar speedBar;

	private JButton increaseSpeed;

	private JButton decreaseSpeed;

	private JButton stopButton;

	private JButton directionButton;

	private LockToggleButton lockButton;

	private Locomotive myLocomotive;

	private final int number;

	private final List<FunctionToggleButton> functionToggleButtons = new ArrayList<FunctionToggleButton>();

	private LocomotiveSelectAction locomotiveSelectAction;

	private LocomotiveGroupSelectAction groupSelectAction;

	private final JFrame frame;

	private final LocomotiveManager locomotiveManager;

	private final LocomotiveGroup allLocomotivesGroup;

	private JPanel functionsPanel;

	private final LocomotiveContext ctx;

	public boolean directionToggeled;
	private DefaultComboBoxModel<LocomotiveGroup> locomotiveGroupComboBoxModel;
	private DefaultComboBoxModel<Locomotive> locomotiveComboBoxModel;

	private boolean ignoreEvents;
    private boolean disableListener;

    public LocomotiveWidget(final LocomotiveContext ctx, final int number,
			final JFrame frame) {
		super();
		this.ctx = ctx;
		this.number = number;
		this.frame = frame;

		locomotiveManager = ctx.getLocomotiveManager();
		ctx.getLocomotiveControl().addLocomotiveChangeListener(myLocomotive,
				this);

		ctx.getMainBus().register(this);
		initGUI();
		initKeyboardActions();

		allLocomotivesGroup = new LocomotiveGroup(Integer.MIN_VALUE, "All");
		locomotiveManager.addLocomotiveManagerListener(this);
	}

	@Subscribe
	public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
		if (event.isConnected()) {
			if (myLocomotive != null) {
				ctx.getLocomotiveControl().addLocomotiveChangeListener(
						myLocomotive, this);
			}
		} else {
			ctx.getLocomotiveControl().removeLocomotiveChangeListener(this);
		}
	}

    @Subscribe
    public void startImport(final StartImportEvent event) {
        disableListener = true;
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        disableListener = false;
        updateLocomotiveGroups(ctx.getLocomotiveManager().getAllLocomotiveGroups());
    }

	public void updateLocomotiveGroups(final SortedSet<LocomotiveGroup> groups) {
		if (myLocomotive != null) {
			return;
		}

		ignoreEvents = true;
		locomotiveGroupComboBoxModel.removeAllElements();
		locomotiveComboBoxModel.removeAllElements();
		allLocomotivesGroup.getLocomotives().clear();

		locomotiveGroupComboBoxModel.addElement(allLocomotivesGroup);

		for (final LocomotiveGroup lg : groups) {
			for (final Locomotive l : lg.getLocomotives()) {
				allLocomotivesGroup.addLocomotive(l);
				locomotiveComboBoxModel.addElement(l);
			}
			locomotiveGroupComboBoxModel.addElement(lg);
		}

		locomotiveComboBox.setSelectedIndex(-1);
		ignoreEvents = false;
	}

	public Locomotive getMyLocomotive() {
		return myLocomotive;
	}

	private void initGUI() {
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		setLayout(new MigLayout("wrap 3"));

		initSelectionPanel();
		final JPanel controlPanel = initControlPanel();

		addMouseListener(new MouseAction());

		add(controlPanel, "span 3, grow");

		addMouseWheelListener(new WheelControl());

	}

	private void initSelectionPanel() {
		locomotiveGroupComboBox = new JComboBox<LocomotiveGroup>();
		locomotiveGroupComboBoxModel = new DefaultComboBoxModel<>();
		locomotiveGroupComboBox.setModel(locomotiveGroupComboBoxModel);
		locomotiveGroupComboBox.setFocusable(false);
		locomotiveGroupComboBox.setFont(locomotiveGroupComboBox.getFont()
				.deriveFont(14));
		locomotiveGroupComboBox.setSelectedIndex(-1);
		groupSelectAction = new LocomotiveGroupSelectAction();
		locomotiveGroupComboBox.addItemListener(groupSelectAction);

		locomotiveComboBox = new JComboBox<Locomotive>();
		locomotiveComboBoxModel = new DefaultComboBoxModel<>();
		locomotiveComboBox.setModel(locomotiveComboBoxModel);
		locomotiveComboBox.setFocusable(false);
		locomotiveSelectAction = new LocomotiveSelectAction();
		locomotiveComboBox.addItemListener(locomotiveSelectAction);
		locomotiveComboBox.setRenderer(new LocomotiveComboBoxRenderer());

		final String locomotiveDescriptionToolTip = LocomotiveHelper
				.getLocomotiveDescription(myLocomotive);
		locomotiveComboBox.setToolTipText(locomotiveDescriptionToolTip);

		add(locomotiveGroupComboBox, "span 3, grow, width 200");
		add(locomotiveComboBox, "span 3, grow, width 200");

	}

	private JPanel initControlPanel() {
		final JPanel controlPanel = new JPanel(new MigLayout("fill"));

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

		controlPanel.add(functionsPanel, "grow, west");
		controlPanel.add(speedControlPanel, "grow");
		controlPanel.add(speedBar, "east, width 45");

		return controlPanel;
	}

    private void processMouseMovement(MouseEvent e) {
        double i = (double)e.getY()/ speedBar.getHeight();
        int newSpeed = (int)((1-i)*myLocomotive.getType().getDrivingSteps());
        ctx.getLocomotiveControl().setSpeed(myLocomotive,newSpeed , myLocomotive.getCurrentFunctions());
    }

	private JPanel initFunctionsControl() {
		functionsPanel = new JPanel();

		return functionsPanel;
	}

	private JPanel initSpeedControl() {

		final JPanel speedControlPanel = new JPanel();
		speedControlPanel.setLayout(new MigLayout("wrap 1, fill"));

		increaseSpeed = new JButton("+");
		decreaseSpeed = new JButton("-");
		stopButton = new JButton("Stop");
		directionButton = new JButton(
				createImageIcon("crystal/forward.png"));
		lockButton = new LockToggleButton("");

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

		speedControlPanel.add(increaseSpeed, "height 30, growx");
		speedControlPanel.add(decreaseSpeed, "height 30, growx");
		speedControlPanel.add(stopButton, "height 30, growx");
		speedControlPanel.add(directionButton, "height 30, growx");
		speedControlPanel.add(lockButton, "height 30, growx");
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
		if (myLocomotive.getFunctions().size() > 5) {
			functionsPanel.setLayout(new MigLayout("wrap 2, fill"));
		} else {
			functionsPanel.setLayout(new MigLayout("wrap, fill"));
		}

		for (final LocomotiveFunction fn : myLocomotive.getFunctions()) {
			final FunctionToggleButton functionButton = new FunctionToggleButton(
					fn.getShortDescription());
			functionToggleButtons.add(functionButton);
			final int i = functionToggleButtons.indexOf(functionButton);
			functionButton.addActionListener(new LocomotiveFunctionAction(i));
			functionButton.setToolTipText(fn.getDescription());

			functionButton.setFocusable(false);
			functionsPanel.add(functionButton, "height 30, width 60");
		}
		revalidate();
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
		updateSpeed();

		updateFunctions();

		updateDirection();

		updateLockedState(locomotiveControl);

		if (isFree()) {
			locomotiveGroupComboBox.setEnabled(true);
			locomotiveComboBox.setEnabled(true);
		} else {
			UIManager.put("ComboBox.disabledForeground", new ColorUIResource(
					Color.BLACK));
			locomotiveGroupComboBox.setEnabled(false);
			locomotiveComboBox.setEnabled(false);
		}
		speedBar.requestFocus();
	}

	private void updateLockedState(final LocomotiveController locomotiveControl) {
		final boolean locked = locomotiveControl.isLocked(myLocomotive);
		lockButton.setSelected(locked);
		if (locked) {
			if (locomotiveControl.isLockedByMe(myLocomotive)) {
				lockButton.setSelectedIcon(ImageTools
						.createImageIconFromCustom("locked_by_me.png"));

			} else {
				lockButton.setSelectedIcon(ImageTools
						.createImageIconFromCustom("locked_by_enemy.png"));
			}
		}
	}

	private void updateSpeed() {
		final int currentSpeed = myLocomotive.getCurrentSpeed();
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
		if (myLocomotive == null) {
			return true;
		}
		final LocomotiveController locomotiveControl = ctx
				.getLocomotiveControl();
		if (myLocomotive.getCurrentSpeed() == 0) {
			if (locomotiveControl.isLocked(myLocomotive)) {
				return !locomotiveControl.isLockedByMe(myLocomotive);
			} else {
				return true;
			}
		} else {
			if (locomotiveControl.isLocked(myLocomotive)) {
				return !locomotiveControl.isLockedByMe(myLocomotive);
			} else {
				return false;
			}
		}
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
		if(disableListener) {
            return;
        }
        updateLocomotiveGroups(locomotiveGroups);

	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {
        if(disableListener) {
            return;
        }updateLocomotiveGroups(locomotiveManager.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveUpdated(final Locomotive locomotive) {
        if(disableListener) {
            return;
        }updateLocomotiveGroups(locomotiveManager.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {
        if(disableListener) {
            return;
        }
        updateLocomotiveGroups(locomotiveManager.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveRemoved(final Locomotive locomotive) {
        if(disableListener) {
            return;
        }
        updateLocomotiveGroups(locomotiveManager.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {
        if(disableListener) {
            return;
        }
        updateLocomotiveGroups(locomotiveManager.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {
        if(disableListener) {
            return;
        }
        updateLocomotiveGroups(locomotiveManager.getAllLocomotiveGroups());

	}

	private void resetLoco() throws LocomotiveException {
		if (myLocomotive == null) {
			return;
		}
		final LocomotiveController locomotiveControl = ctx
				.getLocomotiveControl();
		locomotiveControl.removeLocomotiveChangeListener(myLocomotive, this);
		locomotiveControl.deactivateLoco(myLocomotive);
		myLocomotive = null;

	}

	private class LocomotiveGroupSelectAction implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				return;
			}
			if (ignoreEvents) {
				return;
			}
			if (!isFree()) {
				return;
			}
			final LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBoxModel
					.getSelectedItem();
			final int idx = locomotiveGroupComboBox.getSelectedIndex();

			if (lg == null) {
				return;
			}
			locomotiveComboBox.setEnabled(false);
			locomotiveComboBoxModel.removeAllElements();
			for (final Locomotive l : lg.getLocomotives()) {
				locomotiveComboBoxModel.addElement(l);
			}
			locomotiveComboBox.setEnabled(true);

			locomotiveComboBox.setSelectedIndex(-1);
			try {
				resetLoco();
				locomotiveGroupComboBox.setSelectedIndex(idx);
			} catch (final LocomotiveException e1) {
				ctx.getMainApp().handleException(e1);
			}

		}
	}

	private class LocomotiveSelectAction implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				return;
			}
			if (ignoreEvents) {
				return;
			}
			try {
				if (locomotiveComboBox.getItemCount() == 0
						|| locomotiveComboBox.getSelectedIndex() == -1) {
					resetLoco();
					return;
				}
				if (isFree()) {

					if (myLocomotive != null && ctx.getSession() != null) {
						resetLoco();
					}

					myLocomotive = (Locomotive) locomotiveComboBox
							.getSelectedItem();

					locomotiveManager.setActiveLocomotive(number, myLocomotive);
					final LocomotiveController locomotiveControl = ctx
							.getLocomotiveControl();
					locomotiveControl.addLocomotiveChangeListener(myLocomotive,
							LocomotiveWidget.this);
					locomotiveComboBox
							.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
					lockButton.setBackground(UIConstants.DEFAULT_PANEL_COLOR);

					final String locomotiveDescriptionToolTip = LocomotiveHelper
							.getLocomotiveDescription(myLocomotive);
					locomotiveComboBox
							.setToolTipText(locomotiveDescriptionToolTip);
					updateFunctionButtons();
					updateWidget();

				} else {
					locomotiveGroupComboBox.setSelectedItem(myLocomotive
							.getGroup());
					locomotiveComboBox.setBackground(UIConstants.ERROR_COLOR);
					locomotiveComboBox.setSelectedItem(myLocomotive);
				}
			} catch (final LocomotiveException e1) {
				ctx.getMainApp().handleException(e1);
			}
		}
	}

	private abstract class LocomotiveControlAction extends AbstractAction {

		/**
         *
         */
		private static final long serialVersionUID = 6793690334014866933L;
		private long time = 0;

		@Override
		public void actionPerformed(final ActionEvent e) {

			if (myLocomotive == null) {
				return;
			}
			try {
				final LocomotiveController locomotiveControl = ctx
						.getLocomotiveControl();
				doPerformAction(locomotiveControl, myLocomotive);
				if (time == 0) {
					time = System.currentTimeMillis();
				} else {
					time = 0;
				}
				// updateWidget();
				speedBar.requestFocus();
			} catch (final LocomotiveException e3) {
				ctx.getMainApp().handleException(e3);
			}

		}

		protected abstract void doPerformAction(
				final LocomotiveController locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException;
	}

	private class LocomotiveFunctionAction extends LocomotiveControlAction {

		/**
         *
         */
		private static final long serialVersionUID = -6134540146399675627L;
		private final int function;

		public LocomotiveFunctionAction(final int function) {
			this.function = function;
		}

		@Override
		protected void doPerformAction(
				final LocomotiveController locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException {
			try {
				final boolean state = functionToggleButtons.get(function)
						.isSelected();

				final LocomotiveFunction locomotiveFunction = myLocomotive
						.getFunction(function);
				final int deactivationDelay = locomotiveFunction != null ? locomotiveFunction
						.getDeactivationDelay() : -1;
				locomotiveControl.setFunction(myLocomotive, function, state,
						deactivationDelay);

			} catch (final LocomotiveException e1) {
				ctx.getMainApp().handleException(e1);
			}
			speedBar.requestFocus();
		}
	}

	private class LocomotiveAccelerateAction extends LocomotiveControlAction {

		/**
         *
         */
		private static final long serialVersionUID = -4072984388290979483L;

		@Override
		protected void doPerformAction(
				final LocomotiveController locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException {
			locomotiveControl.increaseSpeed(myLocomotive);
		}
	}

	private class LocomotiveDeccelerateAction extends LocomotiveControlAction {

		/**
         *
         */
		private static final long serialVersionUID = 1895312396353656555L;

		@Override
		protected void doPerformAction(
				final LocomotiveController locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException {
			locomotiveControl.decreaseSpeed(myLocomotive);
		}
	}

	private class LocomotiveToggleDirectionAction extends
			LocomotiveControlAction {

		/**
         *
         */
		private static final long serialVersionUID = 4415875142503406299L;

		@Override
		protected void doPerformAction(
				final LocomotiveController locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException {
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

		/**
         *
         */
		private static final long serialVersionUID = 733623668790654995L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (myLocomotive == null) {
				return;
			}
			try {
				final LocomotiveController locomotiveControl = ctx
						.getLocomotiveControl();
				locomotiveControl.setSpeed(myLocomotive, 0,
						myLocomotive.getCurrentFunctions());
				updateWidget();
				speedBar.requestFocus();
			} catch (final LocomotiveException e3) {
				ctx.getMainApp().handleException(e3);
			}
		}
	}

	private class ToggleDirectionAction extends AbstractAction {

		/**
         *
         */
		private static final long serialVersionUID = 1343795996489453299L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (myLocomotive == null) {
				return;
			}
			try {
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
			} catch (final LocomotiveException e1) {
				ctx.getMainApp().handleException(e1);
			}
		}
	}

	private class LockAction extends AbstractAction {

		/**
         *
         */
		private static final long serialVersionUID = 3534595688958149586L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (myLocomotive == null) {
				return;
			}
			final boolean lockButtonState = lockButton.isSelected();
			try {
				final LocomotiveController locomotiveControl = ctx
						.getLocomotiveControl();
				if (lockButtonState) {
					final boolean succeeded = locomotiveControl
							.acquireLock(myLocomotive);
					lockButton.setSelected(succeeded);
				} else {
					if (locomotiveControl.isLockedByMe(myLocomotive)) {
						final boolean succeeded = !locomotiveControl
								.releaseLock(myLocomotive);
						lockButton.setSelected(succeeded);
					} else {
						lockButton.setSelected(true);
					}
				}
				speedBar.requestFocus();
			} catch (final LockingException ex) {
				ctx.getMainApp().handleException(ex);
				lockButton.setSelected(lockButtonState);
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

				if (ctx.isEditingMode()) {
					if (!isFree()) {
						return;
					}
					final LocomotiveController locomotiveControl = ctx
							.getLocomotiveControl();

					locomotiveControl.removeLocomotiveChangeListener(
							myLocomotive, LocomotiveWidget.this);
					new LocomotiveConfig(frame, locomotiveManager,
							myLocomotive, myLocomotive.getGroup());

					locomotiveControl.addLocomotiveChangeListener(myLocomotive,
							LocomotiveWidget.this);
					locomotiveChanged(myLocomotive);
				} else {

				}
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				final ToggleDirectionAction a = new ToggleDirectionAction();
				a.actionPerformed(null);
			}
			updateWidget();
		}
	}

	private class WheelControl implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(final MouseWheelEvent e) {
			if (myLocomotive == null) {
				return;
			}
			AbstractAction a = null;
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

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {
	}

}
