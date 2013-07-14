package ch.fork.AdHocRailway.manager.locomotives;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;

@SuppressWarnings("unchecked")
public class LocomotiveImageHelper {
	private static Logger LOGGER = Logger
			.getLogger(LocomotiveImageHelper.class);
	private static Map<String, String> imageToPathMap = new HashMap<String, String>();

	private static final String BASE_PATH = "locoimages";

	static {

		final File lockFile = new File(".locomotiveReading.lock");
		final File imageToPathMapDumpFile = new File(
				".locomotiveImageToPathMap.obj");
		try {
			if (!lockFile.exists() && !imageToPathMapDumpFile.exists()) {
				lockFile.createNewFile();
				imageToPathMapDumpFile.delete();

				LOGGER.info("start reading locomotive image files");
				readFiles(new File(BASE_PATH));
				LOGGER.info("read " + imageToPathMap.size()
						+ " locomotive image files");
				final ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(imageToPathMapDumpFile));
				oos.writeObject(imageToPathMap);
				oos.flush();
				oos.close();
				LOGGER.info("wrote locomotive image to path map to file "
						+ imageToPathMapDumpFile.getName());
				lockFile.delete();

			} else {
				while (lockFile.exists()) {
					Thread.sleep(500);
				}
				LOGGER.info("reading locomotive image to path map from file "
						+ imageToPathMapDumpFile.getName());
				final ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(imageToPathMapDumpFile));
				imageToPathMap = (Map<String, String>) ois.readObject();
				ois.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void readFiles(final File dir) {
		final LocoImagesDirFilter locoImagesDirFilter = new LocoImagesDirFilter();
		dir.listFiles();
		for (final File entry : FileUtils.listFiles(dir, locoImagesDirFilter,
				locoImagesDirFilter)) {
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
			LOGGER.debug("getting " + locomotive.getImage()
					+ " from image map for " + locomotive.getName());
			return imageToPathMap.get(locomotive.getImage());
		}
		return null;
	}
}
