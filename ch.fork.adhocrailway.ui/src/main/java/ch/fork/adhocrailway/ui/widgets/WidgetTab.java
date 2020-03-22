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

package ch.fork.adhocrailway.ui.widgets;

import com.jgoodies.common.base.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

public class WidgetTab extends JPanel {
    private final JPanel widgets;

    public WidgetTab() {

        widgets = new JPanel();
        if (SystemUtils.IS_OS_MAC)
            widgets.setLayout(new WrapLayout(FlowLayout.LEADING, 0, 0)); // huge border on macs
        else
            widgets.setLayout(new WrapLayout(FlowLayout.LEADING, 5, 5));


        final JScrollPane groupScrollPane = new JScrollPane(widgets,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        groupScrollPane.setBorder(BorderFactory.createEmptyBorder());
        groupScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        groupScrollPane.getVerticalScrollBar().setBlockIncrement(10);

        setLayout(new BorderLayout(0, 0));
        add(groupScrollPane, BorderLayout.CENTER);
    }

    public void addWidget(final JPanel widget) {
        widgets.add(widget);
    }

    public void remove(final JPanel widget) {
        widgets.remove(widget);
    }

    public void removeAll() {
        widgets.removeAll();
    }

    @Override
    public void addMouseListener(final MouseListener l) {
        widgets.addMouseListener(l);
    }

}
