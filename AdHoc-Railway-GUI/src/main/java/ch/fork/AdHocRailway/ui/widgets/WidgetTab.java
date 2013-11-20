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

package ch.fork.AdHocRailway.ui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

public class WidgetTab extends JPanel {
	private static final long serialVersionUID = -8940185953629421471L;
	private final JPanel widgets;

	public WidgetTab() {

		widgets = new JPanel();
		widgets.setLayout(new BetterFlowLayout(FlowLayout.LEADING));

		final JScrollPane groupScrollPane = new JScrollPane(widgets,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		groupScrollPane.setBorder(BorderFactory.createEmptyBorder());
		groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);
		setLayout(new BorderLayout());
		add(groupScrollPane, BorderLayout.CENTER);
	}

	public void addWidget(final JPanel widget) {
		widgets.add(widget);
	}

	public void remove(final JPanel widget) {
		widgets.remove(widget);
	}

	@Override
	public void addMouseListener(final MouseListener l) {
		widgets.addMouseListener(l);
	}

}
