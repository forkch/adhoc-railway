/*------------------------------------------------------------------------
 * 
 * <./ui/switches/canvas/DoubleCrossSwitchCanvas.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:45 BST 2006
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

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

public class DoubleCrossTurnoutCanvas extends TurnoutCanvas {
    public DoubleCrossTurnoutCanvas(Turnout myTurnout) {
        super(myTurnout);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g3 = img.createGraphics();
        g3.drawImage(createImageIcon("switches/canvas/double_cross_switch.png")
                .getImage(), 0, 0, this);
        switch (myTurnout.getTurnoutState()) {
        case STRAIGHT:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_yellow.png").getImage(),
                    0, 17, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 0, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_up_yellow.png").getImage(),
                    28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            break;
        case RIGHT:
        case LEFT:
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_yellow.png")
                    .getImage(), 0, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png").getImage(),
                    0, 17, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_up_yellow.png").getImage(),
                    28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            break;
        case UNDEF:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png").getImage(),
                    0, 17, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 0, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png").getImage(),
                    28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            break;
        }
        rotate(g2, img);
    }
}
