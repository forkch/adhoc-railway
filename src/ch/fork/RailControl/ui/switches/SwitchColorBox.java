package ch.fork.RailControl.ui.switches;
/*------------------------------------------------------------------------
 * 
 * <src/TrackSwitchColorBox.java>  -  <desc>
 * 
 * begin     : Sun May 15 22:48:36 CEST 2005
 * copyright : (C)  by Benjamin Mueller 
 * email     : akula@akula.ch
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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class SwitchColorBox extends Canvas {

	private static final long serialVersionUID = 1L;
	private Color color;
    private boolean activated;

    public SwitchColorBox(Color color) {
        this.color = color;
        activated = false;
    }

    public void paint(Graphics g) {
        if(activated) {
            g.setColor(color);
            g.fillRect(0,0,50,20);
        } else {
            g.setColor(Color.GRAY);
        	g.fillRect(0,0,50,20);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(50,20);
    }

    public Dimension getMinimumSize() {
        return new Dimension(50,20);
    }

    public void activate() {
        this.activated = true;
        repaint();
    } 

    public void deactivate() {
        this.activated = false;
        repaint();
    }

    public boolean isActivated() {
        return activated;
    }
}
