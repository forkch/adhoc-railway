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

		this.maxCols = maxCols;
		layout = new GridBagLayout();
		// setLayout(layout);
		setLayout(new BetterFlowLayout(FlowLayout.LEADING));
		currentRow = 0;
		currentCol = 0;
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = currentRow;
		gbc.gridy = currentCol;
	}

	public void addWidget(JPanel widget) {
		add(widget);
		if (currentCol == maxCols) {
			currentRow++;
			currentCol = 0;
		}
		gbc.gridx = currentCol;
		gbc.gridy = currentRow;
		layout.setConstraints(widget, gbc);
		currentCol++;
	}

	static class BetterFlowLayout extends FlowLayout {

		public BetterFlowLayout() {

			super();

		}

		public BetterFlowLayout(int align) {

			super(align);

		}

		public BetterFlowLayout(int align, int hgap, int vgap) {

			super(align, hgap, vgap);

		}

		@Override
		public Dimension preferredLayoutSize(Container target) {

			return betterPreferredSize(target);

		}

		@Override
		public Dimension minimumLayoutSize(Container target) {

			return betterPreferredSize(target);

		}

		public Dimension betterPreferredSize(Container target) {

			synchronized (target.getTreeLock()) {

				Insets insets = target.getInsets();

				int maxwidth = target.getWidth()
						- (insets.left + insets.right + getHgap() * 2);

				int nmembers = target.getComponentCount();

				int x = 0, y = insets.top + getVgap();

				int rowh = 0;

				for (int i = 0; i < nmembers; i++) {

					Component m = target.getComponent(i);

					if (m.isVisible()) {

						Dimension d = m.getPreferredSize();

						m.setSize(d.width, d.height);

						if ((x == 0) || ((x + d.width) <= maxwidth)) {

							if (x > 0) {

								x += getHgap();

							}

							x += d.width;

							rowh = Math.max(rowh, d.height);

						} else {

							x = d.width;

							y += getVgap() + rowh;

							rowh = d.height;

						}

					}

				}

				return new Dimension(maxwidth, y + rowh + getVgap());

			}

		}

	}
}
