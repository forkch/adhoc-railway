/*------------------------------------------------------------------------
 * 
 * <./ui/ExceptionProcessor.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:14 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
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

import javax.swing.JOptionPane;
import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

public class ExceptionProcessor {
	private ErrorPanel errorPanel;

	private static ExceptionProcessor instance;

	private ExceptionProcessor(ErrorPanel errorPanel) {
		this.errorPanel = errorPanel;
	}

	public static ExceptionProcessor getInstance(ErrorPanel errorPanel) {
		if (instance == null) {
			instance = new ExceptionProcessor(errorPanel);
		}
		return instance;
	}

	public static ExceptionProcessor getInstance() {
		return instance;
	}

	public void processException(Exception e) {
		e.printStackTrace();
		String exceptionMsg = e.getMessage();
		if (e.getCause() != null) {
			exceptionMsg += "\nCause: " + e.getCause().getMessage();
		}
		errorPanel.setErrorTextIcon(exceptionMsg, ImageTools
				.createImageIcon("messagebox_critical.png"));
	}

	public void processException(String msg, Exception e) {
		e.printStackTrace();
		String exceptionMsg = e.getMessage();
		msg = msg + "\n" + exceptionMsg;
		if (e.getCause() != null) {
			exceptionMsg += "\n\nCause: " + e.getCause().getMessage();
		}
		errorPanel.setErrorTextIcon(msg, ImageTools
				.createImageIcon("messagebox_critical.png"));
	}
	 public void processExceptionDialog(Exception e) {
	        String exceptionMsg = e.getMessage();
	        if (e.getCause() != null) {
	            exceptionMsg += "\n" + e.getCause().getMessage();
	        }
	        JOptionPane.showMessageDialog(AdHocRailway.getInstance(), exceptionMsg,
	                "Error", JOptionPane.ERROR_MESSAGE,
	                createImageIcon("messagebox_critical.png"));
	    }
}
