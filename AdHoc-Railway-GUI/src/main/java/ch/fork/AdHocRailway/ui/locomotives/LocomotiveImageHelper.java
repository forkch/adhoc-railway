package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class LocomotiveImageHelper {
    private static final Logger LOGGER = Logger
            .getLogger(LocomotiveImageHelper.class);
    private static Map<String, String> imageToPathMap = new HashMap<String, String>();

    private static final String BASE_PATH = "locoimages";

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
}
