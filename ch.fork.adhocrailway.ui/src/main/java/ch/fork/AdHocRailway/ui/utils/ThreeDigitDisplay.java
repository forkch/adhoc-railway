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

/**
 * description
 *
 * Version 0.1   14.02.2008 
 *
 * Copyright (C) Siemens Schweiz AG 2008, All Rights Reserved, Confidential
 */
package ch.fork.AdHocRailway.ui.utils;

import ch.fork.AdHocRailway.ui.widgets.Segment7;

import java.awt.*;


/**
 * @author Benjamin Mueller <benjamin.b.mueller@siemens.com>
 */
public class ThreeDigitDisplay extends javax.swing.JPanel {

    private Segment7 seg1;

    private Segment7 seg2;

    private Segment7 seg3;

    public ThreeDigitDisplay() {
        initGUI();
    }

    /**
     *
     */
    private void initGUI() {
        setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        setBackground(new Color(0, 0, 0));
        seg1 = new Segment7();
        seg2 = new Segment7();
        seg3 = new Segment7();
        add(seg3);
        add(seg2);
        add(seg1);
    }

    public void reset() {
        seg1.setValue(-1);
        seg2.setValue(-1);
        seg3.setValue(-1);
        seg3.setDisplayPeriod(false);

        seg1.repaint();
        seg2.repaint();
        seg3.repaint();
    }

    public void setPeriod(boolean period) {
        seg3.setDisplayPeriod(period);
        seg3.repaint();
    }

    public void setNumber(int number) {
        int seg1Value = number % 10;
        seg1.setValue(seg1Value);
        seg1.repaint();
        number = number - seg1Value;
        int seg2Value = 0;
        if (number != 0) {
            seg2Value = (number % 100) / 10;
            seg2.setValue(seg2Value);
        } else {
            seg2.setValue(-1);
        }
        seg2.repaint();
        number = number - seg2Value * 10;
        int seg3Value = 0;
        if (number != 0) {
            seg3Value = (number % 1000) / 100;
            seg3.setValue(seg3Value);
        } else {
            seg3.setValue(-1);
        }
        seg3.repaint();
        number = number - seg3Value * 100;
    }
}
