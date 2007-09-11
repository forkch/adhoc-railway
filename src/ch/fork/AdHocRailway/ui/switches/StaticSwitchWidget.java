/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchWidget.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:35 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id: SwitchWidget.java 99 2007-05-12 20:09:58Z fork_ch $
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

package ch.fork.AdHocRailway.ui.switches;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.turnouts.DefaultSwitch;
import ch.fork.AdHocRailway.domain.turnouts.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.turnouts.Switch;
import ch.fork.AdHocRailway.domain.turnouts.ThreeWaySwitch;
import ch.fork.AdHocRailway.ui.switches.canvas.DefaultSwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.DoubleCrossSwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.SwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.ThreeWaySwitchCanvas;

public class StaticSwitchWidget extends JPanel {

	private static final long serialVersionUID = 1L;

	private Switch mySwitch;

	private JLabel numberLabel;

	private SwitchCanvas switchCanvas;


	public StaticSwitchWidget(Switch aSwitch) {
		mySwitch = aSwitch;
		initGUI();
		
	}

	private void initGUI() {
		switchCanvas = null;
		if (mySwitch instanceof DoubleCrossSwitch) {
			switchCanvas = new DoubleCrossSwitchCanvas(mySwitch);
		} else if (mySwitch instanceof DefaultSwitch) {
			switchCanvas = new DefaultSwitchCanvas(mySwitch);
		} else if (mySwitch instanceof ThreeWaySwitch) {
			switchCanvas = new ThreeWaySwitchCanvas(mySwitch);
		}

		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		numberLabel = new JLabel(Integer.toString(mySwitch.getNumber()));
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 20));
		add(numberLabel);
		add(switchCanvas);
	}

	public Switch getMySwitch() {
		return mySwitch;
	}
}
