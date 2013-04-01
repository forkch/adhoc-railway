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
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;

public class ImageTools {

	private static Logger LOGGER = Logger.getLogger(ImageTools.class);

	public final static String EMTPY_LOCO_ICON = "empty.png";

	public final static Map<String, ImageIcon> cache = new HashMap<String, ImageIcon>();

	public static ImageIcon createImageIcon(final String icon) {
		if (!cache.containsKey(icon)) {
			final ImageIcon imageIcon = new ImageIcon(
					ClassLoader.getSystemResource(icon));
			cache.put(icon, imageIcon);
			LOGGER.info("cache-miss: put icon for " + icon + " in cache");
			return imageIcon;
		}
		LOGGER.debug("cache-hit: got icon for " + icon + " from cache");
		return cache.get(icon);
	}

	public static ImageIcon createImageIconFromIconSet(final String icon) {
		if (!cache.containsKey(icon)) {
			final ImageIcon imageIcon = new ImageIcon(
					ClassLoader.getSystemResource("crystal/" + icon));
			cache.put(icon, imageIcon);
			LOGGER.info("cache-miss: put icon for " + icon + " in cache");
			return imageIcon;
		}
		LOGGER.debug("cache-hit: got icon for " + icon + " from cache");
		return cache.get(icon);
	}

	public static ImageIcon getLocomotiveIcon(final Locomotive locomotive) {
		return getLocomotiveIcon(locomotive, -1);
	}

	public static ImageIcon getLocomotiveIcon(final Locomotive locomotive,
			final int scale) {
		if (locomotive == null) {
			return null;
		}
		String image = locomotive.getImage();
		image = "locoimages/" + image;
		if (!cache.containsKey(image)) {
			if (StringUtils.isNotBlank(image) && new File(image).exists()) {
				return getScaledImage(image, scale);
			} else {
				return getScaledImage(EMTPY_LOCO_ICON, scale);
			}
		} else {
			LOGGER.debug("cache-hit: got icon for " + image + " from cache");
			return cache.get(image);
		}
	}

	private static ImageIcon getScaledImage(final String image, final int height) {
		BufferedImage img;
		try {
			img = ImageIO.read(new File(image));
			if (height > 0) {
				img = Scalr.resize(img, Mode.FIT_TO_WIDTH, height);
			}
			final ImageIcon icon = new ImageIcon(img);
			cache.put(image, icon);
			LOGGER.info("cache-miss: put icon for " + image + " in cache");
			return icon;

		} catch (final IOException e) {
			return null;
		}
	}
}
