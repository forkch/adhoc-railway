/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <DoubleCrossSwitch.java>  -  <>
 * 
 * begin     : Apr 15, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
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

package ch.fork.RailControl.domain.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class DoubleCrossSwitch extends DefaultSwitch {

	public DoubleCrossSwitch(int pNumber, String pDesc) {
		super(pNumber, pDesc);
	}
	
	public DoubleCrossSwitch(int pNumber, String pDesc, int pBus, Address pAddress) {
		super(pNumber, pDesc, pBus, pAddress);
	}
	
	public Image getImage(ImageObserver obs) {
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		g.drawImage(createImageIcon("icons/double_cross_switch.png", "", this)
				.getImage(), 0, 0, obs);
		switch (switchState) {
			case STRAIGHT :
				g.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 0, 17, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 0, 0, obs);
				g.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, obs);
				break;
			case CURVED :
				g.drawImage(createImageIcon("icons/LED_middle_yellow.png", "",
						this).getImage(), 0, 0, obs);
				g.drawImage(createImageIcon("icons/LED_up_white.png", "", this)
						.getImage(), 0, 17, obs);
				g.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, obs);
				break;
			case UNDEF :
				g.drawImage(
						createImageIcon("icons/LED_up_white.png", "", this)
								.getImage(), 0, 17, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 0, 0, obs);
				g.drawImage(
						createImageIcon("icons/LED_up_white.png", "", this)
								.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, obs);
				break;
		}
		
		return img;
	}
}
