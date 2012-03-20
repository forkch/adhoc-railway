/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class WidgetTab extends JPanel {
	private static final long	serialVersionUID	= 1L;
	private int					maxCols;
	private int					currentRow;
	private int					currentCol;
	private GridBagLayout		layout;
	private GridBagConstraints	gbc;

	public WidgetTab(int maxCols) {

		//this.maxCols = maxCols;
		//layout = new GridBagLayout();
		// setLayout(layout);
		setLayout(new BetterFlowLayout(FlowLayout.LEADING));
		//currentRow = 0;
		//currentCol = 0;
		//gbc = new GridBagConstraints();
		//gbc.insets = new Insets(5, 5, 5, 5);
		//gbc.gridx = currentRow;
		//gbc.gridy = currentCol;
	}

	public void addWidget(JPanel widget) {
		add(widget);
		//if (currentCol == maxCols) {
		//	currentRow++;
		//	currentCol = 0;
		//}
		//gbc.gridx = currentCol;
		//gbc.gridy = currentRow;
		//layout.setConstraints(widget, gbc);
		//currentCol++;
	}

	
}
