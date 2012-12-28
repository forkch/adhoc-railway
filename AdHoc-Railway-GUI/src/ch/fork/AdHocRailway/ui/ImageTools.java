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

package ch.fork.AdHocRailway.ui;

import java.io.File;

import javax.swing.ImageIcon;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;

public class ImageTools {

	private final static ImageIcon emptyLocoIcon = ImageTools
			.createImageIconFileSystem("locoimages/empty.png");

	public static ImageIcon createImageIcon(String icon) {
		return new ImageIcon(ClassLoader.getSystemResource(icon));
	}

	public static ImageIcon createImageIconFromIconSet(String icon) {
		return new ImageIcon(ClassLoader.getSystemResource("crystal/" + icon));
	}

	public static ImageIcon createImageIconFileSystem(String icon) {
		return new ImageIcon(icon);
	}

	public static ImageIcon getLocomotiveIcon(Locomotive locomotive) {
		if (locomotive == null) {
			return null;
		}
		String image = locomotive.getImage();

		if (image != null && !image.isEmpty()
				&& new File("locoimages/" + image).exists()) {
			return ImageTools.createImageIconFileSystem("locoimages/" + image);
		} else {
			return emptyLocoIcon;
		}
	}

}
