/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <ImageCombiner.java>  -  <>
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

package ch.fork.RailControl.ui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

public class ImageTools {

	public static ImageIcon createDefaultSwitch(ImageObserver obs, Class comp) {
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		System.out.println(comp.getClass());
		g.drawImage(createImageIcon(
				"../../domain/switches/icons/default_switch.png", "", comp)
				.getImage(), 0, 0, obs);
		g.drawImage(createImageIcon(
				"../../domain/switches/icons/LED_up_yellow.png", "", comp)
				.getImage(), 28, 0, obs);
		g.drawImage(createImageIcon(
				"../../domain/switches/icons/LED_middle_white.png", "", comp)
				.getImage(), 28, 0, obs);

		g.drawImage(createImageIcon(
				"../../domain/switches/icons/LED_middle_white.png", "", comp)
				.getImage(), 0, 0, obs);
		return new ImageIcon(img);
	}

	public static ImageIcon createDoubleCrossSwitch(boolean straight,
			ImageObserver obs, Component comp) {
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		g.drawImage(createImageIcon("switches/icons/double_cross_switch.png",
				"", comp).getImage(), 0, 0, obs);
		if (straight) {
			g.drawImage(createImageIcon("switches/icons/LED_up_yellow.png", "",
					comp).getImage(), 0, 17, obs);
			g.drawImage(createImageIcon("switches/icons/LED_middle_white.png",
					"", comp).getImage(), 0, 0, obs);
			g.drawImage(createImageIcon("switches/icons/LED_up_yellow.png", "",
					comp).getImage(), 28, 0, obs);
			g.drawImage(createImageIcon("switches/icons/LED_middle_white.png",
					"", comp).getImage(), 28, 0, obs);
		} else {
			g.drawImage(createImageIcon("switches/icons/LED_middle_yellow.png",
					"", comp).getImage(), 0, 0, obs);
			g.drawImage(createImageIcon("switches/icons/LED_up_white.png", "",
					comp).getImage(), 0, 17, obs);
			g.drawImage(createImageIcon("switches/icons/LED_up_yellow.png", "",
					comp).getImage(), 28, 0, obs);
			g.drawImage(createImageIcon("switches/icons/LED_middle_white.png",
					"", comp).getImage(), 28, 0, obs);
		}
		return new ImageIcon(img);
	}

	public static ImageIcon createThreeWaySwitch(int state, ImageObserver obs,
			Component comp) {
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		g.drawImage(createImageIcon("switches/icons/three_way_switch.png", "",
				comp).getImage(), 0, 0, obs);
		switch (state) {
			case 0 :
				g.drawImage(createImageIcon("switches/icons/LED_up_yellow.png",
						"", comp).getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_middle_white.png", "", comp)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_down_white.png", "", comp)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_middle_white.png", "", comp)
						.getImage(), 0, 0, obs);
				break;
			case 1 :
				g.drawImage(createImageIcon("switches/icons/LED_up_white.png",
						"", comp).getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_middle_yellow.png", "", comp)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_down_white.png", "", comp)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_middle_white.png", "", comp)
						.getImage(), 0, 0, obs);
				break;
			case 2 :
				g.drawImage(createImageIcon("switches/icons/LED_up_white.png",
						"", comp).getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_middle_white.png", "", comp)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_down_yellow.png", "", comp)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon(
						"switches/icons/LED_middle_white.png", "", comp)
						.getImage(), 0, 0, obs);
				break;
		}
		return new ImageIcon(img);
	}
	
	public static ImageIcon createImageIcon(String path, String description,
			Object o) {
		return createImageIcon(path, description, o.getClass());
	}
	
	public static ImageIcon createImageIcon(String path, String description,
			Class o) {
		java.net.URL imgURL = o.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}
