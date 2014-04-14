package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.ui.utils.ImageTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class LocomotiveImageHelper {

    public final static String EMTPY_LOCO_ICON = "empty.png";
    public final static Map<String, ImageIcon> cache = new HashMap<String, ImageIcon>();

    private static final Logger LOGGER = Logger
            .getLogger(LocomotiveImageHelper.class);
    private static final String BASE_PATH = "locoimages";
    private static Map<String, String> imageToPathMap = new HashMap<String, String>();

    static {
        LOGGER.info("start reading locomotive image files");
        readFiles(new File(BASE_PATH));
        LOGGER.info("read " + imageToPathMap.size()
                + " locomotive image files");
    }

    private static void readFiles(final File dir) {
        final LocoImagesDirFilter locoImagesDirFilter = new LocoImagesDirFilter();
        dir.listFiles();
        for (final File entry : FileUtils.listFilesAndDirs(dir,
                locoImagesDirFilter, locoImagesDirFilter)) {
            if (entry.getName().equals(dir.getName())) {
                continue;
            }
            if (entry.isDirectory()) {
                readFiles(entry);
            } else {
                imageToPathMap.put(entry.getName(), entry.getPath());
            }
        }
    }

    public static String getImagePath(final Locomotive locomotive) {
        if (StringUtils.isNotEmpty(locomotive.getImage())) {
            LOGGER.info("getting " + locomotive.getImage()
                    + " from image map for " + locomotive.getName());
            return imageToPathMap.get(locomotive.getImage());
        }
        return null;
    }

    public static String getImageBase64(Locomotive locomotive) {
        final String image = LocomotiveImageHelper.getImagePath(locomotive);
        if (image == null) {
            return null;
        }

        try {
            BufferedImage read = ImageIO.read(new File(image));
            return ImageTools.encodeToString(read, "png");
        } catch (IOException e) {
            System.out.println(image);
        }
        return null;
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

            if (StringUtils.isNotBlank(locomotive.getImageBase64())) {
                BufferedImage bufferedImage = ImageTools.decodeToImage(locomotive.getImageBase64());
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
            img = Scalr.resize(img, Scalr.Mode.FIT_TO_WIDTH, height);
        }
        final String key = getKey(locomotive, height);
        final ImageIcon icon = new ImageIcon(img);
        //cache.put(key, icon);
        LOGGER.debug("cache-miss: put icon for " + key + " in cache");
        return icon;

    }

    private static ImageIcon getScaledImage(final Locomotive locomotive, final String image, final int height) {
        BufferedImage img;
        try {
            img = ImageIO.read(new File(image));
            if (height > 0) {
                img = Scalr.resize(img, Scalr.Mode.FIT_TO_WIDTH, height);
            }
            final String key = getKey(locomotive, height);
            final ImageIcon icon = new ImageIcon(img);
            //cache.put(key, icon);
            LOGGER.debug("cache-miss: put icon for " + key + " in cache");
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
            key = locomotive.getImage() + "_orig";
        }
        return key;
    }
}
