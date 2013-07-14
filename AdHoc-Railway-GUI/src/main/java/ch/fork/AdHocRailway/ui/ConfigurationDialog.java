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

package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class ConfigurationDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3558143497279399956L;
	protected boolean	okPressed;
	protected boolean	cancelPressed;
	public JButton		okButton;
	public JButton		cancelButton;
	public JPanel		mainButtonPanel;

	public ConfigurationDialog(JFrame owner, String title) {
		super(owner, title, true);

		initBasicGUI();

	}

	public ConfigurationDialog(JDialog owner, String title) {
		super(owner, title, true);

		initBasicGUI();

	}

	private void initBasicGUI() {
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				ConfigurationDialog.this.setVisible(false);
			}
		});
		cancelButton = new JButton("Cancel");
		cancelPressed = false;
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelPressed = true;
				ConfigurationDialog.this.setVisible(false);
			}
		});
		mainButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
//		mainButtonPanel.registerKeyboardAction(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				cancelPressed = true;
//				ConfigurationDialog.this.setVisible(false);
//			}
//		}, "", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
//				JComponent.WHEN_IN_FOCUSED_WINDOW);

		mainButtonPanel.add(okButton);
		// mainButtonPanel.add(cancelButton);
		add(mainButtonPanel, BorderLayout.SOUTH);
	}

	protected void addMainComponent(JComponent mainComponent) {
		add(mainComponent, BorderLayout.CENTER);
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

	public boolean isOkPressed() {
		return okPressed;
	}
}
