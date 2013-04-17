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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.dermoba.srcp.common.exception.SRCPException;

public class ExceptionProcessor {
	private static final Logger LOGGER = Logger
			.getLogger(ExceptionProcessor.class);
	private final ErrorPanel errorPanel;

	private static ExceptionProcessor instance;

	private ExceptionProcessor(final ErrorPanel errorPanel) {
		this.errorPanel = errorPanel;
	}

	public static ExceptionProcessor getInstance(final ErrorPanel errorPanel) {
		if (instance == null) {
			instance = new ExceptionProcessor(errorPanel);
		}
		return instance;
	}

	public static ExceptionProcessor getInstance() {
		return instance;
	}

	public void processException(final Exception e) {
		e.printStackTrace();
		processException(e.getMessage(), e);

	}

	public void processException(String msg, final Exception e) {

		LOGGER.error(e.getMessage(), e);

		if (e instanceof SRCPException) {
			msg = "SRCP: " + msg;
		}
		if (StringUtils.isBlank(msg)) {
			msg = e.getMessage();
		}
		errorPanel.setErrorTextIcon(msg, e.getMessage(),
				ImageTools.createImageIconFromIconSet("dialog-error.png"));

	}

}
