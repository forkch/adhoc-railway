/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <ExceptionProcessor.java>  -  <>
 * 
 * begin     : Apr 15, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
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

package ch.fork.RailControl.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExceptionProcessor {

	private JFrame parent;
	private static ExceptionProcessor instance;

	private ExceptionProcessor(JFrame parent) {
		this.parent = parent;
	}

	public static ExceptionProcessor getInstance(JFrame parent) {
		if (instance == null) {
			instance = new ExceptionProcessor(parent);
		}
		return instance;

	}
	
	public static ExceptionProcessor getInstance() {
		return instance;
	}

	public void processException(Exception e) {
		String msg = e.getMessage(); 
		if(e.getCause() != null) {
			msg += ":\n" + e.getCause().getMessage();
		}
		JOptionPane.showMessageDialog(parent, msg, "Error occured",
				JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}
}
