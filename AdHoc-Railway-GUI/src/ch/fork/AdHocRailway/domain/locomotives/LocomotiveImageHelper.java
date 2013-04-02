package ch.fork.AdHocRailway.domain.locomotives;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class LocomotiveImageHelper {

	private static Map<String, String> imageToPathMap = new HashMap<String, String>();

	private static final String BASE_PATH = "locoimages";

	static {
		readFiles(new File(BASE_PATH));
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
		return imageToPathMap.get(locomotive.getImage());
	}

}
