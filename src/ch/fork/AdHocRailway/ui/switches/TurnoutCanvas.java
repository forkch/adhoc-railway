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


package ch.fork.AdHocRailway.ui.switches;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

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
    
    public void paint(Graphics g) {
    	if (myTurnout.isDoubleCross()) {
			paintDoubleCross(g);
		} else if (myTurnout.isDefault()) {
			paintDefault(g);
		} else if (myTurnout.isThreeWay()) {
			paintThreeway(g);
		}
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
    
    private void paintDefault(Graphics g) {
        BufferedImage img = new BufferedImage(56, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g3 = img.createGraphics();
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
        rotate(g, img);
    }
    
    private void paintDoubleCross(Graphics g) {
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
        rotate(g, img);
    }
    
    private void paintThreeway(Graphics g) {
        BufferedImage img = new BufferedImage(56, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g3 = img.createGraphics();
        g3.drawImage(createImageIcon("switches/canvas/three_way_switch.png")
            .getImage(), 0, 0, this);
        switch (myTurnout.getTurnoutState()){
        case LEFT:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_yellow.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_down_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 0, 0, this);
            break;
        case STRAIGHT:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_middle_yellow.png").getImage(), 28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_down_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 0, 0, this);
            break;
        case RIGHT:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_down_yellow.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 0, 0, this);
            break;
        case UNDEF:
            g3.drawImage(createImageIcon("switches/canvas/LED_up_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 28, 0, this);
            g3.drawImage(createImageIcon("switches/canvas/LED_down_white.png")
                .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("switches/canvas/LED_middle_white.png")
                    .getImage(), 0, 0, this);
        }
        rotate(g, img);
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
