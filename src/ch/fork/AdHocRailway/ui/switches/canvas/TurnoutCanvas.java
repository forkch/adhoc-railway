/*------------------------------------------------------------------------
 * 
 * <./ui/switches/canvas/SwitchCanvas.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:42 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
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


package ch.fork.AdHocRailway.ui.switches.canvas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

public class TurnoutCanvas extends JPanel {
    protected Turnout myTurnout;

    public TurnoutCanvas(Turnout mySwitch) {
        this.myTurnout = mySwitch;
    }

    protected void rotate(Graphics g, BufferedImage img) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = null;
        switch (myTurnout.getOrientationEnum()) {
        case NORTH:
            at = AffineTransform.getRotateInstance(Math.PI / 2 * 3,
                (56 + 1) / 2, (56 + 1) / 2);
            at.concatenate(AffineTransform.getTranslateInstance(0, 10));
            break;
        case EAST:
            at = AffineTransform.getRotateInstance(0, 0, 0);
            at.concatenate(AffineTransform.getTranslateInstance(0, 14));
            break;
        case SOUTH:
            at = AffineTransform.getRotateInstance(Math.PI / 2, (56 + 1) / 2,
                (56 + 1) / 2);
            at.concatenate(AffineTransform.getTranslateInstance(0, 10));
            break;
        case WEST:
            at = AffineTransform.getRotateInstance(Math.PI, (56 + 1) / 2,
                (56 + 1) / 2);
            at.concatenate(AffineTransform.getTranslateInstance(0, 14));
            break;
        }
        g2.drawImage(img, at, this);
    }

    public Dimension getPreferredSize() {
        return new Dimension(56, 56);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public boolean isFocusTraversable() {
        return true;
    }
}
