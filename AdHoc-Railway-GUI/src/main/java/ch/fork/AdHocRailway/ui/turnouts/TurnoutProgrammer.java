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

import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.tools.SwingUtils;
import ch.fork.AdHocRailway.ui.widgets.ConfigurationDialog;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TurnoutProgrammer extends ConfigurationDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5978084216666903357L;
	private final SRCPSession session;
	private final TurnoutContext ctx;

	public TurnoutProgrammer(final JFrame owner, final TurnoutContext ctx) {
		super(owner, "Turnout Programmer");
		this.ctx = ctx;
		this.session = ctx.getSession();
		initGUI();
	}

	private void initGUI() {
		final JPanel mainPanel = new JPanel(new BorderLayout());
		final JPanel buttonPanel = new JPanel(new GridLayout(4, 4));
		for (int i = 1; i <= 252; i = i + 4) {
			final JButton button = new JButton("" + i);
			buttonPanel.add(button);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final int address = Integer.parseInt(e.getActionCommand());
					final GA ga = new GA(session, 1);
					ga.setAddress(address);
					try {
						ga.set(0, 1, 1000);
					} catch (final SRCPException e1) {
						ctx.getMainApp().handleException(e1);
					}
				}
			});
		}
		final JLabel titleLabel = new JLabel("Enter first address of decoder");
		mainPanel.add(titleLabel, BorderLayout.NORTH);
		mainPanel.add(buttonPanel, BorderLayout.CENTER);

		addMainComponent(mainPanel);
		pack();
		SwingUtils.addEscapeListener(this);
		setVisible(true);
	}
}
