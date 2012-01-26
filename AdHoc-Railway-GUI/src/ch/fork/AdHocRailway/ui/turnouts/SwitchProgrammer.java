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

package ch.fork.AdHocRailway.ui.turnouts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class SwitchProgrammer extends ConfigurationDialog {
	private SRCPSession	session;

	public SwitchProgrammer(JFrame owner, SRCPSession session) {
		super(owner, "Switch Programmer");
		this.session = session;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new GridLayout(4, 4));
		for (int i = 1; i <= 252; i = i + 4) {
			JButton button = new JButton("" + i);
			buttonPanel.add(button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int address = Integer.parseInt(e.getActionCommand());
					GA ga = new GA(session,1);
					ga.setAddress(address);
					try {
						ga.set(0, 1, 1000);
					} catch (SRCPException e1) {
						ExceptionProcessor.getInstance().processException(e1);
					}
				}
			});
		}
		JLabel titleLabel = new JLabel("Enter first address of decoder");
		mainPanel.add(titleLabel, BorderLayout.NORTH);
		mainPanel.add(buttonPanel, BorderLayout.CENTER);
		addMainComponent(mainPanel);
		pack();
		setVisible(true);
	}
}
