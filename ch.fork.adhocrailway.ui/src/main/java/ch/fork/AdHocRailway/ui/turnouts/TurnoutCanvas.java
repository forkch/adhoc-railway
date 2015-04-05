/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

package ch.fork.AdHocRailway.ui.turnouts;

import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.utils.ImageTools;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static ch.fork.AdHocRailway.ui.utils.ImageTools.createImageIconFromCustom;

public class TurnoutCanvas extends JPanel {
    public static final String LINKED_ROUTE = "canvas/linked_route.png";
    public static final String DEFAULT_SWITCH_RIGHT = "canvas/default_switch_right.png";
    public static final String LED_MIDDLE_WHITE = "canvas/LED_middle_white.png";
    public static final String LED_MIDDLE_YELLOW = "canvas/LED_middle_yellow.png";
    public static final String LED_DOWN_WHITE = "canvas/LED_down_white.png";
    public static final String LED_DOWN_YELLOW = "canvas/LED_down_yellow.png";
    public static final String DOUBLE_CROSS_SWITCH = "canvas/double_cross_switch.png";
    public static final String LED_UP_YELLOW = "canvas/LED_up_yellow.png";
    public static final String LED_UP_WHITE = "canvas/LED_up_white.png";
    public static final String THREE_WAY_SWITCH = "canvas/three_way_switch.png";
    public static final String DEFAULT_SWITCH_LEFT_PNG = "canvas/default_switch_left.png";

    private Turnout turnout;
    private TurnoutState turnoutState = TurnoutState.UNDEF;

    public TurnoutCanvas(final Turnout turnout) {
        this.turnout = turnout;
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (turnout.isDoubleCross()) {
            paintDoubleCross(g);
        } else if (turnout.isDefaultLeft()) {
            paintDefaultLeft(g);
        } else if (turnout.isDefaultRight()) {
            paintDefaultRight(g);
        } else if (turnout.isThreeWay()) {
            paintThreeway(g);
        } else if (turnout.isCutter()) {
            paintCutter(g);
        } else if (turnout.isLinkedToRoute()) {
            paintLinkedRoute(g);
        }
    }


    private void rotate(final Graphics g, final BufferedImage img) {
        final Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = null;
        switch (turnout.getOrientation()) {
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

    private void paintDefaultLeft(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 56,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(createImageIconFromCustom(DEFAULT_SWITCH_LEFT_PNG)
                .getImage(), 0, 0, this);
        switch (turnoutState) {
            case STRAIGHT:
                g3.drawImage(
                        createImageIconFromCustom(LED_MIDDLE_YELLOW)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 0, this);
                break;
            case LEFT:
            case RIGHT:
                g3.drawImage(createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                break;
            case UNDEF:
                g3.drawImage(createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                break;
        }
        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 0, 0, this);
        rotate(g, img);
    }

    private void paintDefaultRight(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 56,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(
                createImageIconFromCustom(DEFAULT_SWITCH_RIGHT)
                        .getImage(), 0, 0, this
        );
        switch (turnoutState) {
            case STRAIGHT:
                g3.drawImage(
                        createImageIconFromCustom(LED_MIDDLE_YELLOW)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(createImageIconFromCustom(LED_DOWN_WHITE)
                        .getImage(), 28, 0, this);
                break;
            case LEFT:
            case RIGHT:
                g3.drawImage(
                        createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(createImageIconFromCustom(LED_DOWN_YELLOW)
                        .getImage(), 28, 0, this);
                break;
            case UNDEF:
                g3.drawImage(createImageIconFromCustom(LED_DOWN_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                break;
        }
        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 0, 0, this);
        rotate(g, img);
    }

    private void paintCutter(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 56,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(
                createImageIconFromCustom("cutter.png").getImage(), 0, 0,
                this);

        rotate(g, img);
    }

    private void paintLinkedRoute(final Graphics g) {
        final BufferedImage img = new BufferedImage(112, 42,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(
                createImageIconFromCustom(LINKED_ROUTE)
                        .getImage(), 0, 0, this
        );

        if (turnout.getLinkedRoute() != null) {
            if (turnout.getLinkedRoute().isEnabled()) {
                g3.drawImage(createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 28, 21, this);
                g3.drawImage(createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 55, 3, this);
                g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                        .getImage(), 55, -14, this);
                g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                        .getImage(), 28, 21, this);
            } else {
                g3.drawImage(createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 21, this);
                g3.drawImage(createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 55, 3, this);
                g3.drawImage(createImageIconFromCustom(LED_MIDDLE_YELLOW)
                        .getImage(), 55, -14, this);
                g3.drawImage(createImageIconFromCustom(LED_MIDDLE_YELLOW)
                        .getImage(), 28, 21, this);
            }
        } else {
            g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                    .getImage(), 55, -14, this);
            g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                    .getImage(), 28, 21, this);
            g3.drawImage(createImageIconFromCustom(LED_UP_WHITE)
                    .getImage(), 28, 21, this);
            g3.drawImage(createImageIconFromCustom(LED_UP_WHITE)
                    .getImage(), 55, 3, this);
        }

        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 0, -14, this);
        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 28, -14, this);
        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 83, -14, this);

        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 0, 21, this);
        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 55, 21, this);
        g3.drawImage(createImageIconFromCustom(LED_MIDDLE_WHITE)
                .getImage(), 83, 21, this);

        ((Graphics2D) g).drawImage(img, AffineTransform.getTranslateInstance(0, 7), this);

    }

    private void paintDoubleCross(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 56,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(ImageTools
                .createImageIconFromCustom(DOUBLE_CROSS_SWITCH)
                .getImage(), 0, 0, this);
        switch (turnoutState) {
            case STRAIGHT:
                g3.drawImage(ImageTools
                        .createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 0, 17, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 0, 0, this
                );
                g3.drawImage(ImageTools
                        .createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                break;
            case RIGHT:
            case LEFT:
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom(LED_MIDDLE_YELLOW)
                                .getImage(), 0, 0, this
                );
                g3.drawImage(ImageTools
                        .createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 0, 17, this);
                g3.drawImage(ImageTools
                        .createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                break;
            case UNDEF:
                g3.drawImage(ImageTools
                        .createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 0, 17, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 0, 0, this
                );
                g3.drawImage(ImageTools
                        .createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools
                                .createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                break;
        }
        rotate(g, img);
    }

    private void paintThreeway(final Graphics g) {
        final BufferedImage img = new BufferedImage(56, 35,
                BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g3 = img.createGraphics();
        g3.drawImage(ImageTools.createImageIconFromCustom(THREE_WAY_SWITCH)
                .getImage(), 0, 0, this);
        switch (turnoutState) {
            case LEFT:
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_UP_YELLOW)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_DOWN_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 0, 0, this
                );
                break;
            case STRAIGHT:
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_YELLOW)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_DOWN_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 0, 0, this
                );
                break;
            case RIGHT:
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_DOWN_YELLOW)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 0, 0, this
                );
                break;
            case UNDEF:
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_UP_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 28, 0, this
                );
                g3.drawImage(ImageTools.createImageIconFromCustom(LED_DOWN_WHITE)
                        .getImage(), 28, 0, this);
                g3.drawImage(
                        ImageTools.createImageIconFromCustom(LED_MIDDLE_WHITE)
                                .getImage(), 0, 0, this
                );
        }
        rotate(g, img);
    }

    @Override
    public Dimension getPreferredSize() {
        if (turnout.isLinkedToRoute()) {
            return new Dimension(112, 56);
        } else {
            return new Dimension(56, 56);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public boolean isFocusTraversable() {
        return true;
    }

    public void setTurnoutState(final TurnoutState turnoutState) {
        this.turnoutState = turnoutState;
        revalidate();
        repaint();
    }
}
