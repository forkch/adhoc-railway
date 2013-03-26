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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;

public class ImageTools {

	private final static String emptyLocoIcon = "locoimages/empty.png";

	public static ImageIcon createImageIcon(final String icon) {
		return new ImageIcon(ClassLoader.getSystemResource(icon));
	}

	public static ImageIcon createImageIconFromIconSet(final String icon) {
		return new ImageIcon(ClassLoader.getSystemResource("crystal/" + icon));
	}

	public static ImageIcon createImageIconFileSystem(final String icon) {
		return new ImageIcon(icon);
	}

	public static ImageIcon getLocomotiveIcon(final Locomotive locomotive) {
		return getLocomotiveIcon(locomotive, -1);
	}

	public static ImageIcon getLocomotiveIcon(final Locomotive locomotive,
			final int scale) {
		if (locomotive == null) {
			return null;
		}
		final String image = locomotive.getImage();

		if (StringUtils.isNotBlank(image)
				&& new File("locoimages/" + image).exists()) {
			return getScaledImage(new File("locoimages/" + image), scale);
		} else {
			return getScaledImage(new File(emptyLocoIcon), scale);
		}
	}

	private static ImageIcon getScaledImage(final File file, final int height) {
		BufferedImage img;
		try {
			img = ImageIO.read(file);
			if (height > 0) {
				img = Scalr.resize(img, Mode.FIT_TO_WIDTH, height);
			}
			return new ImageIcon(img);

		} catch (final IOException e) {
			return null;
		}
	}

}
