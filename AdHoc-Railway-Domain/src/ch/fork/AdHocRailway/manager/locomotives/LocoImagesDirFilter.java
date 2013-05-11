package ch.fork.AdHocRailway.manager.locomotives;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

public class LocoImagesDirFilter implements IOFileFilter {

	@Override
	public boolean accept(File arg0) {
		if (arg0.isDirectory()) {
			if (StringUtils.containsIgnoreCase(arg0.getName(), ".svn")) {
				return false;
			}
			return true;
		} else {
			if (StringUtils.endsWithAny(arg0.getName().toLowerCase(), ".gif",
					".png", ".jpg", ".bmp")) {
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean accept(File arg0, String arg1) {
		if (StringUtils.containsIgnoreCase(arg0.getName(), ".svn")) {
			return false;
		}
		return true;
	}

}
