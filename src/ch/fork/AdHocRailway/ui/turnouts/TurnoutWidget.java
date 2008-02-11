/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchWidget.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:35 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
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

package ch.fork.AdHocRailway.ui.turnouts;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;

public class TurnoutWidget extends JPanel implements TurnoutChangeListener,
		LockChangeListener {
	private TurnoutControlIface turnoutControl;

	private static final long serialVersionUID = 1L;

	private Turnout turnout;

	private JLabel numberLabel;

	private JLabel descLabel;

	private TurnoutCanvas turnoutCanvas;

	private GridBagLayout turnoutWidgetLayout;

	private GridBagConstraints turnoutWidgetConstraints;

	private TurnoutState actualTurnoutState = TurnoutState.UNDEF;

	private boolean widgetEnabled;

	private Color defaultBackground;

	private boolean testMode;

	private TurnoutPersistenceIface turnoutPersistence;

	public TurnoutWidget(Turnout turnout) {
		this(turnout, false);
	}

	public TurnoutWidget(Turnout turnout, boolean testMode) {
		this.testMode = testMode;
		this.turnout = turnout;
		this.turnoutControl = AdHocRailway.getInstance().getTurnoutControl();
		this.turnoutPersistence = AdHocRailway.getInstance()
				.getTurnoutPersistence();
		turnoutControl.addTurnoutChangeListener(turnout, this);
		defaultBackground = getBackground();
		widgetEnabled = true;

		initGUI();
		validateTurnout();
	}

	private void initGUI() {
		turnoutCanvas = new TurnoutCanvas(turnout);

		turnoutCanvas.addMouseListener(new MouseAction());
		addMouseListener(new MouseAction());

		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		turnoutWidgetLayout = new GridBagLayout();
		turnoutWidgetConstraints = new GridBagConstraints();
		setLayout(turnoutWidgetLayout);
		turnoutWidgetConstraints.insets = new Insets(0, 5, 0, 5);
		turnoutWidgetConstraints.gridx = 0;
		numberLabel = new JLabel(Integer.toString(turnout.getNumber()));
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
		turnoutWidgetLayout.setConstraints(numberLabel,
				turnoutWidgetConstraints);
		add(numberLabel);
//		turnoutWidgetConstraints.gridx = 1;
//		descLabel = new JLabel(turnout.getDescription());
//		turnoutWidgetLayout.setConstraints(descLabel, turnoutWidgetConstraints);
//		add(descLabel);
		
		turnoutWidgetConstraints.gridx = 0;
		turnoutWidgetConstraints.gridy = 1;
		turnoutWidgetConstraints.gridwidth = 2;
		turnoutWidgetLayout.setConstraints(turnoutCanvas,
				turnoutWidgetConstraints);
		add(turnoutCanvas);

	}

	private void validateTurnout() {
		boolean bus1Valid = true;
		if (turnout.getBus1() == 0) {
			setBackground(UIConstants.ERROR_COLOR);
			bus1Valid = false;
		} else {
			setBackground(UIConstants.DEFAULT_PANEL_COLOR);
		}
		boolean address1Valid = true;
		if (turnout.getAddress1() == 0
				|| turnout.getAddress1() > Constants.MAX_MM_TURNOUT_ADDRESS) {
			setBackground(UIConstants.ERROR_COLOR);
			address1Valid = false;

		} else {
			setBackground(UIConstants.DEFAULT_PANEL_COLOR);

		}
		if (bus1Valid && address1Valid) {
			int bus1 = turnout.getBus1();
			int address1 = turnout.getAddress1();
			Turnout aTurnout1 = turnoutPersistence.getTurnoutByAddressBus(bus1,
					address1);
			if (aTurnout1 != null && !aTurnout1.equals(turnout)) {
				setBackground(UIConstants.WARN_COLOR);
			} else {
				setBackground(UIConstants.DEFAULT_PANEL_COLOR);
			}
		}

		if (turnout.isThreeWay()) {
			boolean bus2Valid = true;
			if (turnout.getBus2() == 0) {
				setBackground(UIConstants.ERROR_COLOR);
				bus2Valid = false;
			} else {
				setBackground(UIConstants.DEFAULT_PANEL_COLOR);
			}
			boolean address2Valid = true;
			if (turnout.getAddress2() == 0
					|| turnout.getAddress2() > Constants.MAX_MM_TURNOUT_ADDRESS) {
				setBackground(UIConstants.ERROR_COLOR);
				address2Valid = false;
			} else {
				setBackground(UIConstants.DEFAULT_PANEL_COLOR);
			}
			if (bus2Valid && address2Valid) {
				int bus2 = turnout.getBus2();
				int address2 = turnout.getAddress2();
				Turnout aTurnout2 = turnoutPersistence.getTurnoutByAddressBus(
						bus2, address2);
				if (aTurnout2 != null && !aTurnout2.equals(turnout)) {
					setBackground(UIConstants.WARN_COLOR);
				} else {
					setBackground(UIConstants.DEFAULT_PANEL_COLOR);
				}
			}
		}
	}

	public void lockChanged(ControlObject changedLock) {
		// TODO reimplement
		// if (changedLock instanceof Turnout) {
		// Turnout changedSwitch = (Turnout) changedLock;
		// turnoutChanged(changedSwitch);
		// }
	}

	private class MouseAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!widgetEnabled)
				return;
			try {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					turnoutControl.toggle(turnout);
				} else if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON3) {
					displaySwitchConfig();
				}
			} catch (TurnoutException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void displaySwitchConfig() {
			if (testMode)
				return;
			turnoutControl.removeTurnoutChangeListener(turnout);
			TurnoutConfig switchConf = new TurnoutConfig(AdHocRailway
					.getInstance(), turnout);
			turnoutControl
					.addTurnoutChangeListener(turnout, TurnoutWidget.this);

			validateTurnout();
			turnoutChanged(turnout, actualTurnoutState);
		}
	}

	public Turnout getTurnout() {
		return turnout;
	}

	public void turnoutChanged(Turnout changedTurnout, TurnoutState newState) {
		if (turnout.equals(changedTurnout)) {
			actualTurnoutState = newState;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					numberLabel.setText(Integer.toString(turnout.getNumber()));
					descLabel.setText(turnout.getDescription());
					turnoutCanvas.setTurnoutState(actualTurnoutState);
					TurnoutWidget.this.revalidate();
					TurnoutWidget.this.repaint();
				}
			});
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (!enabled)
			setBackground(new Color(255, 177, 177));
		else
			setBackground(defaultBackground);
		widgetEnabled = enabled;
		turnoutControl.removeTurnoutChangeListener(this);
		turnoutControl.addTurnoutChangeListener(turnout, this);
		turnoutCanvas.setTurnoutState(TurnoutState.UNDEF);
	}
}
