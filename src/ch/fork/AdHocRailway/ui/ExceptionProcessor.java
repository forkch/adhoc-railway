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

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class ExceptionProcessor {
    private JFrame                    parent;
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
        e.printStackTrace();
        String exceptionMsg = e.getMessage();
        if (e.getCause() != null) {
            exceptionMsg += "\n\nCause: " + e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(parent, exceptionMsg, "Error occured",
            JOptionPane.ERROR_MESSAGE, ImageTools.createImageIcon("messagebox_critical.png"));

    }

    public void processException(String msg, Exception e) {
        e.printStackTrace();
        String exceptionMsg = e.getMessage();
        msg = msg + "\n" + exceptionMsg;
        if (e.getCause() != null) {
            exceptionMsg += "\n\nCause: " + e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(parent, msg, "Error occured",
            JOptionPane.ERROR_MESSAGE, ImageTools.createImageIcon(
                "messagebox_critical.png"));
    }
}
