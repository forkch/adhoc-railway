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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;

import com.jgoodies.binding.list.ArrayListModel;

public class LocomotiveWidget extends JPanel implements
		LocomotiveChangeListener {
	private static final long serialVersionUID = 1L;

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
	private final ImageIcon emptyLocoIcon = ImageTools
			.createImageIconFileSystem("locoimages/empty.png");

	public LocomotiveWidget(int number, JFrame frame) {
		super();
		this.number = number;
		this.frame = frame;
		initGUI();
		initKeyboardActions();
	}

	private void initGUI() {
		// setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		// setPreferredSize(new Dimension(200, 310));

		setLayout(new MigLayout("wrap 3"));

		initSelectionPanel();
		JPanel controlPanel = initControlPanel();

		addMouseListener(new MouseAction());

		imageLabel = new JLabel(emptyLocoIcon);
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

		groupSelectAction = new LocomotiveGroupSelectAction();
		locomotiveComboBox = new JComboBox<Locomotive>();
		locomotiveComboBox.setFocusable(false);
		locomotiveSelectAction = new LocomotiveSelectAction();

		add(locomotiveGroupComboBox, "span 3, grow, width 200");
		add(locomotiveComboBox, "span 3, grow, width 200");

	}

	private JPanel initControlPanel() {
		JPanel controlPanel = new JPanel(new MigLayout("fill"));

		speedBar = new JProgressBar(SwingConstants.VERTICAL);

		JPanel functionsPanel = initFunctionsControl();
		JPanel speedControlPanel = initSpeedControl();

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
		if (myLocomotive == null)
			return;
		String image = myLocomotive.getImage();

		if (image != null && !image.isEmpty()
				&& new File("locoimages/" + image).exists()) {
			imageLabel.setIcon(ImageTools
					.createImageIconFileSystem("locoimages/" + image));
		} else {
			imageLabel.setIcon(emptyLocoIcon);
		}
	}

	private JPanel initFunctionsControl() {
		JPanel functionsPanel = new JPanel();
		functionsPanel.setLayout(new MigLayout("wrap 1, fill"));

		FunctionToggleButton functionButton = new FunctionToggleButton("Fn");
		FunctionToggleButton f1Button = new FunctionToggleButton("F1");
		FunctionToggleButton f2Button = new FunctionToggleButton("F2");
		FunctionToggleButton f3Button = new FunctionToggleButton("F3");
		FunctionToggleButton f4Button = new FunctionToggleButton("F4");
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

		JPanel speedControlPanel = new JPanel();
		speedControlPanel.setLayout(new MigLayout("wrap 1, fill"));

		increaseSpeed = new JButton("+");
		decreaseSpeed = new JButton("-");
		stopButton = new JButton("Stop");
		directionButton = new JButton(
				createImageIcon("locomotives/forward.png"));
		lockButton = new LockToggleButton("");

		/*
		 * increaseSpeed.setMaximumSize(size);
		 * decreaseSpeed.setMaximumSize(size); stopButton.setMaximumSize(size);
		 * directionButton.setMaximumSize(size);
		 * lockButton.setMaximumSize(size);
		 * 
		 * increaseSpeed.setMargin(margin); decreaseSpeed.setMargin(margin);
		 * stopButton.setMargin(margin); directionButton.setMargin(margin);
		 * lockButton.setMargin(margin);
		 */

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
		KeyBoardLayout kbl = Preferences.getInstance().getKeyBoardLayout();
		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
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
		LocomotiveControlface locomotiveControl = AdHocRailway.getInstance()
				.getLocomotiveControl();
		if (myLocomotive == null)
			return true;
		if (locomotiveControl.getCurrentSpeed(myLocomotive) == 0) {
			if (locomotiveControl.isLocked(myLocomotive)) {
				if (locomotiveControl.isLockedByMe(myLocomotive))
					return false;
				else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			if (locomotiveControl.isLocked(myLocomotive)) {
				if (locomotiveControl.isLockedByMe(myLocomotive))
					return false;
				else {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public void updateLocomotiveGroups() {
		LocomotiveManager locomotivePersistence = AdHocRailway
				.getInstance().getLocomotivePersistence();
		locomotiveComboBox.removeActionListener(locomotiveSelectAction);
		locomotiveGroupComboBox.removeActionListener(groupSelectAction);

		// FIXME: add "All"-Group
		// locomotiveGroupComboBox.addItem("All");
		for (LocomotiveGroup lg : locomotivePersistence
				.getAllLocomotiveGroups()) {
			locomotiveGroupComboBox.addItem(lg);
			for (Locomotive l : lg.getLocomotives()) {
				locomotiveComboBox.addItem(l);
			}
		}

		// FIXME: redo
		// locomotiveGroupComboBox.setSelectedIndex(0);
		// if (myLocomotive != null)
		// locomotiveComboBox.setSelectedItem(myLocomotive);
		// else
		// locomotiveComboBox.setSelectedIndex(0);

		locomotiveComboBox.addActionListener(locomotiveSelectAction);
		locomotiveGroupComboBox.addActionListener(groupSelectAction);
	}

	private class LocomotiveGroupSelectAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			LocomotiveManager locomotivePersistence = AdHocRailway
					.getInstance().getLocomotivePersistence();

			locomotiveComboBox.removeAllItems();
			if (locomotiveGroupComboBox.getSelectedIndex() == 0) {
				ArrayListModel<Locomotive> sl = locomotivePersistence
						.getAllLocomotives();
				System.out.println("ALL");

				for (Locomotive l : sl) {
					locomotiveComboBox.addItem(l);
				}
			} else {
				LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBox
						.getSelectedItem();

				for (Locomotive l : lg.getLocomotives()) {
					locomotiveComboBox.addItem(l);
				}
			}
			locomotiveComboBox.setSelectedIndex(-1);
			locomotiveComboBox.revalidate();
			locomotiveComboBox.repaint();
		}
	}

	private class LocomotiveSelectAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			LocomotiveControlface locomotiveControl = AdHocRailway
					.getInstance().getLocomotiveControl();
			if (locomotiveComboBox.getItemCount() == 0
					|| locomotiveComboBox.getSelectedIndex() == 0) {
				myLocomotive = null;
				return;
			}
			if (isFree()) {
				try {
					if (myLocomotive != null)
						locomotiveControl.setSpeed(myLocomotive, 0, null);
				} catch (LocomotiveException e1) {
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
				locomotiveGroupComboBox.setSelectedItem(myLocomotive
						.getLocomotiveGroup());
				locomotiveComboBox.setBackground(UIConstants.ERROR_COLOR);
				locomotiveComboBox.setSelectedItem(myLocomotive);
			}
		}
	}

	protected void updateWidget() {
		System.out.println("updateWidget()");
		LocomotiveControlface locomotiveControl = AdHocRailway.getInstance()
				.getLocomotiveControl();
		if (myLocomotive == null)
			return;
		float speedInPercent = ((float) locomotiveControl
				.getCurrentSpeed(myLocomotive))
				/ ((float) myLocomotive.getLocomotiveType().getDrivingSteps());

		float hue = (1.0f - speedInPercent) * 0.3f;
		Color speedColor = Color.getHSBColor(hue, 1.0f, 1.0f);

		/*
		 * if (speedInPercent > 0.9) { speedBar.setForeground(new Color(255, 0,
		 * 0)); } else if (speedInPercent > 0.7) { speedBar.setForeground(new
		 * Color(255, 255, 0)); } else { speedBar.setForeground(new Color(0,
		 * 255, 0)); }
		 */

		speedBar.setForeground(speedColor);
		speedBar.setMinimum(0);
		speedBar.setMaximum(myLocomotive.getLocomotiveType().getDrivingSteps());
		speedBar.setValue(locomotiveControl.getCurrentSpeed(myLocomotive));
		boolean functions[] = locomotiveControl.getFunctions(myLocomotive);
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

		boolean locked = locomotiveControl.isLocked(myLocomotive);
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

		if (myLocomotive.getLocomotiveType().getFunctionCount() == 0) {
			for (FunctionToggleButton b : functionToggleButtons) {
				b.setEnabled(false);
			}
		} else {
			for (FunctionToggleButton b : functionToggleButtons) {
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
		// setPreferredSize(new Dimension(200, 250));
		// revalidate();
		// repaint();
	}

	private abstract class LocomotiveControlAction extends AbstractAction {

		private long time = 0;

		@Override
		public void actionPerformed(ActionEvent e) {

			if (myLocomotive == null)
				return;
			try {
				LocomotiveControlface locomotiveControl = AdHocRailway
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
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}

		}

		protected abstract void doPerformAction(
				LocomotiveControlface locomotiveControl, Locomotive myLocomotive)
				throws LocomotiveException;
	}

	private class LocomotiveFunctionAction extends LocomotiveControlAction {

		private final int function;

		public LocomotiveFunctionAction(int function) {
			this.function = function;
		}

		@Override
		protected void doPerformAction(LocomotiveControlface locomotiveControl,
				Locomotive myLocomotive) throws LocomotiveException {
			try {
				boolean[] functions = locomotiveControl
						.getFunctions(myLocomotive);
				if (function < functions.length) {
					functions[function] = functionToggleButtons[function]
							.isSelected();
					locomotiveControl.setFunctions(myLocomotive, functions);
				}
			} catch (LocomotiveException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
			speedBar.requestFocus();
		}
	}

	private class LocomotiveAccelerateAction extends LocomotiveControlAction {
		@Override
		protected void doPerformAction(LocomotiveControlface locomotiveControl,
				Locomotive myLocomotive) throws LocomotiveException {
			locomotiveControl.increaseSpeed(myLocomotive);
		}
	}

	private class LocomotiveDeccelerateAction extends LocomotiveControlAction {
		@Override
		protected void doPerformAction(LocomotiveControlface locomotiveControl,
				Locomotive myLocomotive) throws LocomotiveException {
			locomotiveControl.decreaseSpeed(myLocomotive);
		}
	}

	private class LocomotiveToggleDirectionAction extends
			LocomotiveControlAction {
		@Override
		protected void doPerformAction(LocomotiveControlface locomotiveControl,
				Locomotive myLocomotive) throws LocomotiveException {
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
		@Override
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				locomotiveControl.setSpeed(myLocomotive, 0, null);
				updateWidget();
				speedBar.requestFocus();
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}

	private class ToggleDirectionAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			try {
				LocomotiveControlface locomotiveControl = AdHocRailway
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
			} catch (LocomotiveException e1) {
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

		@Override
		public void actionPerformed(ActionEvent e) {
			if (myLocomotive == null)
				return;
			boolean lockButtonState = lockButton.isSelected();
			try {
				LocomotiveControlface locomotiveControl = AdHocRailway
						.getInstance().getLocomotiveControl();
				if (lockButtonState) {
					boolean succeeded = locomotiveControl
							.acquireLock(myLocomotive);
					lockButton.setSelected(succeeded);
				} else {
					if (locomotiveControl.isLockedByMe(myLocomotive)) {
						boolean succeeded = !locomotiveControl
								.releaseLock(myLocomotive);
						lockButton.setSelected(succeeded);
					} else {
						lockButton.setSelected(true);
					}
				}
				speedBar.requestFocus();
			} catch (LockingException ex) {
				ExceptionProcessor.getInstance().processException(ex);
				lockButton.setSelected(lockButtonState);
			}
			updateWidget();
		}

	}

	private class MouseAction extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (myLocomotive == null)
				return;
			LocomotiveControlface locomotiveControl = AdHocRailway
					.getInstance().getLocomotiveControl();
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
				LocomotiveManager locomotivePersistence = AdHocRailway
						.getInstance().getLocomotivePersistence();
				LocomotiveConfig locomotiveConfig = new LocomotiveConfig(frame,
						myLocomotive);
				if (locomotiveConfig.isOkPressed()) {
					locomotivePersistence.updateLocomotive(myLocomotive);
				}
				locomotiveChanged(myLocomotive);
				locomotiveControl.update();
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				ToggleDirectionAction a = new ToggleDirectionAction();
				a.actionPerformed(null);
			}
			updateWidget();
		}
	}

	private class WheelControl implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (myLocomotive == null)
				return;
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
	public void locomotiveChanged(Locomotive changedLocomotive) {
		if (myLocomotive == null) // this widget does not have a selected
			// locomotive yet
			return;
		if (myLocomotive.equals(changedLocomotive)) {
			// SwingUtilities.invokeLater(new LocomotiveWidgetUpdater());
		}
	}
}
