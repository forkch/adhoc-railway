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

package ch.fork.AdHocRailway.ui.tools;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveImageHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class ImageTools {

    private static final Logger LOGGER = Logger.getLogger(ImageTools.class);

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
        return cache.get(icon);
    }

    public static ImageIcon createImageIconFromCustom(final String icon) {
        return createImageIcon("custom/" + icon);
    }

    public static ImageIcon createImageIconFromIconSet(final String icon) {
        return createImageIcon("crystal/" + icon);
    }


    public static String getImageBase64(Locomotive locomotive) {
        final String image = LocomotiveImageHelper.getImagePath(locomotive);
        if (image == null) {
            return null;
        }

        try {
            BufferedImage read = ImageIO.read(new File(image));
            return encodeToString(read, "bmp");
        } catch (IOException e) {
            System.out.println(image);
        }
        return null;
    }


    /**
     * Decode string to image
     *
     * @param imageString The string to decode
     * @return decoded image
     */
    public static BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Encode image to string
     *
     * @param image The image to encode
     * @param type  jpeg, bmp, ...
     * @return encoded string
     */
    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    public static ImageIcon getLocomotiveIcon(final Locomotive locomotive) {
        return getLocomotiveIcon(locomotive, -1);
    }

    public static ImageIcon getLocomotiveIcon(final Locomotive locomotive,
                                              final int height) {
        if (locomotive == null) {
            return null;
        }

        final String key = getKey(locomotive, height);
        if (!cache.containsKey(key)) {

            if(StringUtils.isNotBlank(locomotive.getImageBase64())) {
                BufferedImage bufferedImage = decodeToImage(locomotive.getImageBase64());
                return getScaledImage(locomotive, bufferedImage, height);
            } else {
                return getScaledImage(locomotive, EMTPY_LOCO_ICON, height);
            }
        } else {
            return cache.get(key);
        }
    }

    private static ImageIcon getScaledImage(Locomotive locomotive, BufferedImage img, final int height) {
        if (height > 0) {
            img = Scalr.resize(img, Mode.FIT_TO_WIDTH, height);
        }
        final String key = getKey(locomotive, height);
        final ImageIcon icon = new ImageIcon(img);
        cache.put(key, icon);
        LOGGER.info("cache-miss: put icon for " + key + " in cache");
        return icon;

    }

    private static ImageIcon getScaledImage(final Locomotive locomotive, final String image, final int height) {
        BufferedImage img;
        try {
            img = ImageIO.read(new File(image));
            if (height > 0) {
                img = Scalr.resize(img, Mode.FIT_TO_WIDTH, height);
            }
            final String key = getKey(locomotive, height);
            final ImageIcon icon = new ImageIcon(img);
            cache.put(key, icon);
            LOGGER.info("cache-miss: put icon for " + key + " in cache");
            return icon;

        } catch (final IOException e) {
            return null;
        }
    }

    private static String getKey(final Locomotive locomotive, final int height) {
        String key;
        if (height > 0) {
            key = locomotive.getImage() + "_" + height;
        } else {
            key = locomotive.getImage()+ "_orig";
        }
        return key;
    }
}
