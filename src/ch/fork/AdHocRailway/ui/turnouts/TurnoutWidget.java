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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;

public class TurnoutWidget extends JPanel implements TurnoutChangeListener,
		LockChangeListener {
	private TurnoutControlIface turnoutControl;

	private static final long serialVersionUID = 1L;

	private Turnout myTurnout;

	private JLabel numberLabel;

	private JLabel descLabel;

	private TurnoutCanvas turnoutCanvas;

	private GridBagLayout turnoutWidgetLayout;

	private GridBagConstraints turnoutWidgetConstraints;

	private JFrame frame;
	
	private TurnoutState actualTurnoutState = TurnoutState.UNDEF;

	public TurnoutWidget(Turnout turnout, JFrame frame) {
		myTurnout = turnout;
		this.frame = frame;
		this.turnoutControl = SRCPTurnoutControl.getInstance();
		initGUI();
		turnoutControl.addTurnoutChangeListener(turnout, this);
	}

	private void initGUI() {
		turnoutCanvas = new TurnoutCanvas(myTurnout);
		
		turnoutCanvas.addMouseListener(new MouseAction());
		addMouseListener(new MouseAction());

		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		turnoutWidgetLayout = new GridBagLayout();
		turnoutWidgetConstraints = new GridBagConstraints();
		setLayout(turnoutWidgetLayout);
		turnoutWidgetConstraints.insets = new Insets(5, 5, 5, 5);
		turnoutWidgetConstraints.gridx = 0;
		numberLabel = new JLabel(Integer.toString(myTurnout.getNumber()));
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
		turnoutWidgetLayout.setConstraints(numberLabel, turnoutWidgetConstraints);
		add(numberLabel);
		turnoutWidgetConstraints.gridx = 1;
		descLabel = new JLabel(myTurnout.getDescription());
		turnoutWidgetLayout.setConstraints(descLabel, turnoutWidgetConstraints);
		add(descLabel);
		turnoutWidgetConstraints.gridx = 0;
		turnoutWidgetConstraints.gridy = 1;
		turnoutWidgetConstraints.gridwidth = 2;
		turnoutWidgetLayout
				.setConstraints(turnoutCanvas, turnoutWidgetConstraints);
		add(turnoutCanvas);

	}

	public void lockChanged(ControlObject changedLock) {
		//TODO reimplement
		//		if (changedLock instanceof Turnout) {
//			Turnout changedSwitch = (Turnout) changedLock;
//			turnoutChanged(changedSwitch);
//		}
	}

	private class MouseAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			try {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					turnoutControl.toggle(myTurnout);
				} else if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON3) {
					displaySwitchConfig();
				}
			} catch (TurnoutException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void displaySwitchConfig() {
			turnoutControl.removeSwitchChangeListener(myTurnout);
			TurnoutConfig switchConf = new TurnoutConfig(frame, myTurnout);
			turnoutControl.addTurnoutChangeListener(myTurnout, TurnoutWidget.this);
			
			turnoutChanged(myTurnout, actualTurnoutState);
		}
	}

	public Turnout getMyTurnout() {
		return myTurnout;
	}

	public void turnoutChanged(Turnout changedTurnout, TurnoutState newState) {
		if (myTurnout.equals(changedTurnout)) {
			actualTurnoutState = newState;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					numberLabel.setText(Integer.toString(myTurnout.getNumber()));
					descLabel.setText(myTurnout.getDescription());
					turnoutCanvas.setTurnoutState(actualTurnoutState);
					TurnoutWidget.this.revalidate();
					TurnoutWidget.this.repaint();
				}
			});
		}
	}
}
