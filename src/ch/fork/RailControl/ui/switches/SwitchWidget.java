package ch.fork.RailControl.ui.switches;
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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchException;
import ch.fork.RailControl.ui.ExceptionProcessor;
import ch.fork.RailControl.ui.RailControlGUI;

public class SwitchWidget extends JPanel {

	private static final long serialVersionUID = 1L;
	private Switch mySwitch;
	private JLabel iconLabel;

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
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		layout.setConstraints(numberLabel, gbc);
		add(numberLabel);

		gbc.gridx = 1;
		JLabel descLabel = new JLabel(mySwitch.getDesc());
		layout.setConstraints(descLabel, gbc);
		add(descLabel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon(mySwitch.getImage(this)));
		layout.setConstraints(iconLabel, gbc);
		add(iconLabel);

		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getClickCount() == 2) {
						SwitchControl.getInstance().toggle(mySwitch);
						SwitchWidget.this.revalidate();
						SwitchWidget.this.repaint();
						iconLabel.setIcon(new ImageIcon(mySwitch.getImage(SwitchWidget.this)));
						iconLabel.repaint();
						iconLabel.revalidate();
					}
				} catch (SwitchException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
			}
			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		});
	}
}
