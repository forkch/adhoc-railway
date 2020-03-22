package ch.fork.adhocrailway.ui.locomotives;

import ch.fork.adhocrailway.model.AdHocRailwayException;
import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.ui.utils.ImageTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class LocomotiveImageHelper {

    public final static String EMTPY_LOCO_ICON = "empty.png";
    public final static Map<String, ImageIcon> cache = new HashMap<String, ImageIcon>();

    private static final Logger LOGGER = LoggerFactory.getLogger(LocomotiveImageHelper.class);

    public static String getImageBase64(File file) {
        try {
            BufferedImage read = ImageIO.read(file);
            return ImageTools.encodeToString(read, "png");
        } catch (IOException e) {
            throw new AdHocRailwayException("error loading image from file " + file, e);
        }
    }

    public static ImageIcon getLocomotiveIconOriginalSize(final Locomotive locomotive) {
        return getLocomotiveIconScaledToHeight(locomotive, -1);
    }

    public static ImageIcon getLocomotiveIconScaledToHeight(final Locomotive locomotive,
                                                            final int height) {
        return getLocomotiveImageScaled(locomotive, height, Scalr.Mode.FIT_TO_HEIGHT);
    }

    public static ImageIcon getLocomotiveIconScaledToWidth(final Locomotive locomotive,
                                                           final int width) {
        return getLocomotiveImageScaled(locomotive, width, Scalr.Mode.FIT_TO_WIDTH);
    }

    private static ImageIcon getLocomotiveImageScaled(Locomotive locomotive, int height, Scalr.Mode mode) {
        if (locomotive == null) {
            return null;
        }

        final String key = getKey(locomotive, height);
        if (!cache.containsKey(key)) {

            if (StringUtils.isNotBlank(locomotive.getImageBase64())) {
                BufferedImage bufferedImage = ImageTools.decodeToImage(locomotive.getImageBase64());
                return getScaledImage(locomotive, bufferedImage, height, mode);
            } else {
                return getScaledImage(locomotive, EMTPY_LOCO_ICON, height, mode);
            }
        } else {
            return cache.get(key);
        }
    }

    public static ImageIcon getEmptyLocoIconScaledToHeight(final int height) {
        if (!cache.containsKey(EMTPY_LOCO_ICON)) {
            try {

                final InputStream resourceAsStream = LocomotiveImageHelper.class.getResourceAsStream("/" + EMTPY_LOCO_ICON);
                BufferedImage img = ImageIO.read(resourceAsStream);
                resourceAsStream.close();
                return getScaledImageAndPutInCache(EMTPY_LOCO_ICON, img, height, Scalr.Mode.FIT_TO_HEIGHT);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return cache.get(EMTPY_LOCO_ICON);
        }
        return new ImageIcon();
    }

    private static ImageIcon getScaledImage(Locomotive locomotive, BufferedImage img, final int height, Scalr.Mode mode) {
        final ImageIcon icon = getScaledImageAndPutInCache(getKey(locomotive, height), img, height, mode);
        return icon;

    }

    private static ImageIcon getScaledImage(final Locomotive locomotive, final String image, final int height, Scalr.Mode mode) {
        BufferedImage img;
        try {
            img = ImageIO.read(new File(image));
            final ImageIcon icon = getScaledImageAndPutInCache(getKey(locomotive, height), img, height, mode);
            return icon;

        } catch (final IOException e) {
            return null;
        }
    }

    private static ImageIcon getScaledImageAndPutInCache(String key, BufferedImage img, int size, Scalr.Mode mode) {
        if (size > 0) {
            img = Scalr.resize(img, mode, size);
        }
        final ImageIcon icon = new ImageIcon(img);
        cache.put(key, icon);
        LOGGER.debug("cache-miss: put icon for " + key + " in cache");
        return icon;
    }

    private static String getKey(final Locomotive locomotive, final int height) {
        String key;
        if (height > 0) {
            key = locomotive.getImage() + "_" + height;
        } else {
            key = locomotive.getImage() + "_orig";
        }
        return key;
    }
}
