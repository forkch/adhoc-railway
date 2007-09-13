/*------------------------------------------------------------------------
 * 
 * <./ui/switches/canvas/DefaultSwitchCanvas.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:48 BST 2006
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


package ch.fork.AdHocRailway.ui.switches;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

public class DefaultTurnoutCanvas extends TurnoutCanvas {
    public DefaultTurnoutCanvas(Turnout myTurnout) {
        super(myTurnout);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        BufferedImage img = new BufferedImage(56, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g3 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g3.drawImage(createImageIcon("switches/canvas/default_switch.png")
            .getImage(), 0, 0, this);
        switch (myTurnout.getTurnoutState()){
        case STRAIGHT:
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_yellow.png").getImage(), 28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png")
                .getImage(), 28, 0, this);
            break;
        case LEFT:
        case RIGHT:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_yellow.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            break;
        case UNDEF:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            break;
        }
        g3.drawImage(createImageIcon("switches/canvas/LED_middle_white.png")
            .getImage(), 0, 0, this);
        rotate(g2, img);
    }
}
