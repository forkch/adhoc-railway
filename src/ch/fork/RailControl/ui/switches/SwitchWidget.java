/*------------------------------------------------------------------------
 * 
 * <src/TrackSwitchWidget.java>  -  <desc>
 * 
 * begin     : Sun May 15 18:09:11 CEST 2005
 * copyright : (C)  by Benjamin Mueller 
 * email     : akula@akula.ch
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
package ch.fork.RailControl.ui.switches;

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
import javax.swing.border.EtchedBorder;

import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchChangeListener;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.ui.ExceptionProcessor;

public class SwitchWidget extends JPanel implements SwitchChangeListener {

	private static final long serialVersionUID = 1L;

	private Switch mySwitch;

	private SwitchCanvas switchState;

	public SwitchWidget(Switch aSwitch) {
		mySwitch = aSwitch;
		initGUI();
	}

	private void initGUI() {
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(layout);

		gbc.insets = new Insets(5, 5, 5, 5);

		gbc.gridx = 0;
		JLabel numberLabel = new JLabel(Integer.toString(mySwitch.getNumber()));
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 40));
		layout.setConstraints(numberLabel, gbc);
		add(numberLabel);

		gbc.gridx = 1;
		JLabel descLabel = new JLabel(mySwitch.getDesc());
		layout.setConstraints(descLabel, gbc);
		add(descLabel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		if (mySwitch instanceof DoubleCrossSwitch) {
			switchState = new DoubleCrossSwitchCanvas(mySwitch);
		} else if (mySwitch instanceof DefaultSwitch) {
			switchState = new DefaultSwitchCanvas(mySwitch);
		} else if (mySwitch instanceof ThreeWaySwitch) {
			switchState = new ThreeWaySwitchCanvas(mySwitch);
		}
		layout.setConstraints(switchState, gbc);
		add(switchState);
		switchState.repaint();

		addMouseListener(new ToggleAction());

		switchState.addMouseListener(new ToggleAction());
	}

	public void switchChanged(Switch changedSwitch) {
		if (mySwitch.equals(changedSwitch)) {
			SwingUtilities.invokeLater(new SwitchWidgetUpdater());
		}
	}
	
	private class SwitchWidgetUpdater implements Runnable {

		public void run() {
			SwitchWidget.this.repaint();
			SwitchWidget.this.revalidate();
			switchState.repaint();
		}
		
	}
	
	private class ToggleAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			try {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					if (!mySwitch.isInitialized()) {
						mySwitch.init();
					}
					SwitchControl.getInstance().toggle(mySwitch);
					SwitchWidget.this.revalidate();
					SwitchWidget.this.repaint();
					switchState.repaint();
				}
			} catch (SwitchException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}
	}
}
