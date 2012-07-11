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

import java.awt.FlowLayout;

import javax.swing.JPanel;

public class WidgetTab extends JPanel {
	private static final long serialVersionUID = 1L;

	public WidgetTab(int maxCols) {

		setLayout(new BetterFlowLayout(FlowLayout.LEADING));
	}

	public void addWidget(JPanel widget) {
		add(widget);
	}

}
