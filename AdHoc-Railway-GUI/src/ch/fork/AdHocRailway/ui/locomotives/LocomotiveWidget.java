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

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.domain.locking.LockingException;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerListener;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;

public class LocomotiveWidget extends JPanel implements
		LocomotiveChangeListener, LocomotiveManagerListener {

	/**
	 * 
	 */
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

	private FunctionToggleButton[] functionToggleButtons;

	private LocomotiveSelectAction locomotiveSelectAction;

	private LocomotiveGroupSelectAction groupSelectAction;

	private final JFrame frame;

	private JLabel imageLabel;

	private final LocomotiveManager locomotivePersistence = AdHocRailway
			.getInstance().getLocomotivePersistence();

	private final LocomotiveGroup allLocomotivesGroup;

	public LocomotiveWidget(final int number, final JFrame frame) {
		super();
		this.number = number;
		this.frame = frame;
		initGUI();
		initKeyboardActions();

		allLocomotivesGroup = new LocomotiveGroup();
		allLocomotivesGroup.setName("All");
		locomotivePersistence.addLocomotiveManagerListener(this);
	}

	private void initGUI() {
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		setLayout(new MigLayout("wrap 3"));

		initSelectionPanel();
		final JPanel controlPanel = initControlPanel();

		addMouseListener(new MouseAction());

		imageLabel = new JLabel();
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

		add(controlPanel, "span 3, grow");
		add(imageLabel, "span 3, grow, width 200, height 60");

		addMouseWheelListener(new WheelControl());

	}

	private void initSelectionPanel() {
		locomotiveGroupComboBox = new JComboBox<LocomotiveGroup>();
		locomotiveGroupComboBox.setFocusable(false);
		locomotiveGroupComboBox.setFont(locomotiveGroupComboBox.getFont()
				.deriveFont(14));
		locomotiveGroupComboBox.setSelectedIndex(-1);

		groupSelectAction = new LocomotiveGroupSelectAction();
		locomotiveComboBox = new JComboBox<Locomotive>();
		locomotiveComboBox.setFocusable(false);
		locomotiveSelectAction = new LocomotiveSelectAction();
		locomotiveComboBox.setRenderer(new LocomotiveComboBoxRenderer());

		add(locomotiveGroupComboBox, "span 3, grow, width 200");
		add(locomotiveComboBox, "span 3, grow, width 200");

	}

	private JPanel initControlPanel() {
		final JPanel controlPanel = new JPanel(new MigLayout("fill"));

		speedBar = new JProgressBar(SwingConstants.VERTICAL);

		final JPanel functionsPanel = initFunctionsControl();
		final JPanel speedControlPanel = initSpeedControl();

		controlPanel.add(functionsPanel, "grow, west");
		controlPanel.add(speedControlPanel, "grow");
		controlPanel.add(speedBar, "east, width 30");

		setLocomotiveImage();

		return controlPanel;
	}

	/**
	 * @param imageLabel
	 */
	public void setLocomotiveImage() {

		if (myLocomotive == null) {
			return;
		}
		imageLabel.setIcon(ImageTools.getLocomotiveIcon(myLocomotive));
	}

	private JPanel initFunctionsControl() {
		final JPanel functionsPanel = new JPanel();
		functionsPanel.setLayout(new MigLayout("wrap 1, fill"));

		final FunctionToggleButton functionButton = new FunctionToggleButton(
				"Fn");
		final FunctionToggleButton f1Button = new FunctionToggleButton("F1");
		final FunctionToggleButton f2Button = new FunctionToggleButton("F2");
		final FunctionToggleButton f3Button = new FunctionToggleButton("F3");
		final FunctionToggleButton f4Button = new FunctionToggleButton("F4");
		functionToggleButtons = new FunctionToggleButton[] { functionButton,
				f1Button, f2Button, f3Button, f4Button };

		for (int i = 0; i < functionToggleButtons.length; i++) {
			functionToggleButtons[i]
					.addActionListener(new LocomotiveFunctionAction(i));
			functionToggleButtons[i].setFocusable(false);
			functionsPanel.add(functionToggleButtons[i], "height 30, width 60");
		}
		return functionsPanel;
	}

	private JPanel initSpeedControl() {

		final JPanel speedControlPanel = new JPanel();
		speedControlPanel.setLayout(new MigLayout("wrap 1, fill"));

		increaseSpeed = new JButton("+");
		decreaseSpeed = new JButton("-");
		stopButton = new JButton("Stop");
		directionButton = new JButton(
				createImageIcon("locomotives/forward.png"));
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

	private boolean isFree() {
		final LocomotiveControlface locomotiveControl = AdHocRailway
				.getInstance().getLocomotiveControl();
		if (myLocomotive == null) {
			return true;
		}
		if (locomotiveControl.getCurrentSpeed(myLocomotive) == 0) {
			if (locomotiveControl.isLocked(myLocomotive)) {
				if (locomotiveControl.isLockedByMe(myLocomotive)) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			if (locomotiveControl.isLocked(myLocomotive)) {
				if (locomotiveControl.isLockedByMe(myLocomotive)) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public void updateLocomotiveGroups(final List<LocomotiveGroup> groups) {
		locomotiveGroupComboBox.removeItemListener(groupSelectAction);
		locomotiveComboBox.removeItemListener(locomotiveSelectAction);

		locomotiveGroupComboBox.removeAllItems();
		locomotiveComboBox.removeAllItems();
		allLocomotivesGroup.getLocomotives().clear();
		locomotiveGroupComboBox.addItem(allLocomotivesGroup);
		for (final LocomotiveGroup lg : groups) {
			for (final Locomotive l : lg.getLocomotives()) {
				allLocomotivesGroup.addLocomotive(l);
				locomotiveComboBox.addItem(l);
			}
			locomotiveGroupComboBox.addItem(lg);
		}

		locomotiveGroupComboBox.addItemListener(groupSelectAction);
		locomotiveGroupComboBox.setSelectedIndex(0);

		locomotiveComboBox.addItemListener(locomotiveSelectAction);
		if (locomotiveComboBox.getItemCount() > 0) {
			locomotiveComboBox.setSelectedIndex(0);
		}

		revalidate();
		repaint();
	}

	private class LocomotiveGroupSelectAction implements ItemListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5974667988792786828L;

		@Override
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				return;
			}
			locomotiveComboBox.removeAllItems();
			locomotiveComboBox.removeItemListener(locomotiveSelectAction);
			final LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBox
					.getSelectedItem();

			if (lg == null) {
				return;
			}
			for (final Locomotive l : lg.getLocomotives()) {
				locomotiveComboBox.addItem(l);
			}

			locomotiveComboBox.addItemListener(locomotiveSelectAction);
			if (locomotiveComboBox.getItemCount() > 0) {
				locomotiveComboBox.setSelectedIndex(0);
			}
			revalidate();
			repaint();
		}
	}

	private class LocomotiveSelectAction implements ItemListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2142620918859490601L;

		@Override
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				return;
			}
			final LocomotiveControlface locomotiveControl = AdHocRailway
					.getInstance().getLocomotiveControl();
			if (locomotiveComboBox.getItemCount() == 0
					|| locomotiveComboBox.getSelectedIndex() == -1) {
				myLocomotive = null;
				return;
			}
			if (isFree()) {
				try {
					if (myLocomotive != null) {
						locomotiveControl.setSpeed(myLocomotive, 0, null);
					}
				} catch (final LocomotiveException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
				locomotiveComboBox
						.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
				lockButton.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
				myLocomotive = (Locomotive) locomotiveComboBox
						.getSelectedItem();

				locomotiveControl.addLocomotiveChangeListener(myLocomotive,
						LocomotiveWidget.this);
				updateWidget();
				speedBar.requestFocus();
			} else {
				locomotiveGroupComboBox
						.setSelectedItem(myLocomotive.getGroup());
				locomotiveComboBox.setBackground(UIConstants.ERROR_COLOR);
				locomotiveComboBox.setSelectedItem(myLocomotive);
			}
		}
	}

	protected void updateWidget() {
		final LocomotiveControlface locomotiveControl = AdHocRailway
				.getInstance().getLocomotiveControl();
		if (myLocomotive == null) {
			return;
		}
		final float speedInPercent = ((float) locomotiveControl
				.getCurrentSpeed(myLocomotive))
				/ ((float) myLocomotive.getType().getDrivingSteps());

		final float hue = (1.0f - speedInPercent) * 0.3f;
		final Color speedColor = Color.getHSBColor(hue, 1.0f, 1.0f);

		speedBar.setForeground(speedColor);
		speedBar.setMinimum(0);
		speedBar.setMaximum(myLocomotive.getType().getDrivingSteps());
		speedBar.setValue(locomotiveControl.getCurrentSpeed(myLocomotive));
		final boolean functions[] = locomotiveControl
				.getFunctions(myLocomotive);
		for (int i = 0; i < functions.length; i++) {
			functionToggleButtons[i].setSelected(functions[i]);
		}
		switch (locomotiveControl.getDirection(myLocomotive)) {
		case FORWARD:
			directionButton.setIcon(createImageIcon("locomotives/forward.png"));
			break;
		case REVERSE:
			directionButton.setIcon(createImageIcon("locomotives/back.png"));
			break;
		default:
			directionButton.setIcon(createImageIcon("locomotives/forward.png"));
		}

		final boolean locked = locomotiveControl.isLocked(myLocomotive);
		lockButton.setSelected(locked);
		if (locked) {
			if (locomotiveControl.isLockedByMe(myLocomotive)) {
				lockButton.setSelectedIcon(ImageTools
						.createImageIcon("locomotives/locked_by_me.png"));

			} else {
				lockButton.setSelectedIcon(ImageTools
						.createImageIcon("locomotives/locked_by_enemy.png"));
			}
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

		setLocomotiveImage();
		if (isFree()) {
			locomotiveGroupComboBox.setEnabled(true);
			locomotiveComboBox.setEnabled(true);
		} else {
			UIManager.put("ComboBox.disabledForeground", new ColorUIResource(
					Color.BLACK));
			locomotiveGroupComboBox.setEnabled(false);
			locomotiveComboBox.setEnabled(false);
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
				final LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				doPerformAction(locomotiveControl, myLocomotive);
				if (time == 0) {
					time = System.currentTimeMillis();
				} else {
					time = 0;
				}
				updateWidget();
				speedBar.requestFocus();
				// SwingUtilities.invokeLater(new LocomotiveWidgetUpdater());
			} catch (final LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}

		}

		protected abstract void doPerformAction(
				LocomotiveControlface locomotiveControl, Locomotive myLocomotive)
				throws LocomotiveException;
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
				final LocomotiveControlface locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException {
			try {
				final boolean[] functions = locomotiveControl
						.getFunctions(myLocomotive);
				if (function < functions.length) {
					functions[function] = functionToggleButtons[function]
							.isSelected();
					locomotiveControl.setFunctions(myLocomotive, functions);
				}
			} catch (final LocomotiveException e1) {
				ExceptionProcessor.getInstance().processException(e1);
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
				final LocomotiveControlface locomotiveControl,
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
				final LocomotiveControlface locomotiveControl,
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
				final LocomotiveControlface locomotiveControl,
				final Locomotive myLocomotive) throws LocomotiveException {
			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.STOP_ON_DIRECTION_CHANGE)
					&& locomotiveControl.getCurrentSpeed(myLocomotive) != 0) {
				locomotiveControl.setSpeed(myLocomotive, 0,
						locomotiveControl.getFunctions(myLocomotive));
			}
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
				final LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				locomotiveControl.setSpeed(myLocomotive, 0, null);
				updateWidget();
				speedBar.requestFocus();
			} catch (final LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
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
				final LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				if (Preferences.getInstance().getBooleanValue(
						PreferencesKeys.STOP_ON_DIRECTION_CHANGE)
						&& locomotiveControl.getCurrentSpeed(myLocomotive) != 0) {
					locomotiveControl.setSpeed(myLocomotive, 0,
							locomotiveControl.getFunctions(myLocomotive));
				}
				locomotiveControl.toggleDirection(myLocomotive);
				speedBar.requestFocus();
				updateWidget();
			} catch (final LocomotiveException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}
	}

	// private class IncreaseSpeedAction extends AbstractAction {
	// public void actionPerformed(ActionEvent e) {
	// if (myLocomotive == null)
	// return;
	// try {
	// LocomotiveControlface locomotiveControl = AdHocRailway
	// .getInstance().getLocomotiveControl();
	// locomotiveControl.increaseSpeed(myLocomotive);
	// updateWidget();
	// speedBar.requestFocus();
	// } catch (LocomotiveException e3) {
	// ExceptionProcessor.getInstance().processException(e3);
	// }
	// }
	// }

	// private class DecreaseSpeedAction extends AbstractAction {
	// public void actionPerformed(ActionEvent e) {
	// if (myLocomotive == null)
	// return;
	// try {
	// LocomotiveControlface locomotiveControl = AdHocRailway
	// .getInstance().getLocomotiveControl();
	// locomotiveControl.decreaseSpeed(myLocomotive);
	// updateWidget();
	// speedBar.requestFocus();
	// } catch (LocomotiveException e3) {
	// ExceptionProcessor.getInstance().processException(e3);
	// }
	// }
	// }

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
				final LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
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
				ExceptionProcessor.getInstance().processException(ex);
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
				new LocomotiveConfig(frame, myLocomotive,
						myLocomotive.getGroup());
				locomotiveChanged(myLocomotive);
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

	public Locomotive getMyLocomotive() {
		return myLocomotive;
	}

	@Override
	public void locomotiveChanged(final Locomotive changedLocomotive) {
		if (myLocomotive == null) {
			// locomotive yet
			return;
		}
		if (myLocomotive.equals(changedLocomotive)) {
			// SwingUtilities.invokeLater(new LocomotiveWidgetUpdater());
		}
	}

	@Override
	public void locomotivesUpdated(final List<LocomotiveGroup> locomotiveGroups) {
		updateLocomotiveGroups(locomotiveGroups);

	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {
		updateLocomotiveGroups(locomotivePersistence.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveUpdated(final Locomotive locomotive) {
		updateLocomotiveGroups(locomotivePersistence.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {
		updateLocomotiveGroups(locomotivePersistence.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveRemoved(final Locomotive locomotive) {
		updateLocomotiveGroups(locomotivePersistence.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {
		updateLocomotiveGroups(locomotivePersistence.getAllLocomotiveGroups());

	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {
		updateLocomotiveGroups(locomotivePersistence.getAllLocomotiveGroups());

	}

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {

	}
}
