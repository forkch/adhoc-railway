/*------------------------------------------------------------------------
 * 
 * <./ui/SplashWindow.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:33 BST 2006
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;


public class SplashWindow extends JWindow {

    private int          steps;
    private int          waitTime;
    private Icon         icon;
    private JLabel       msgLabel;
    private JProgressBar progressBar;

    public SplashWindow(Icon icon, Frame f, int waitTime, int steps) {
        super(f);
        this.icon = icon;
        this.steps = steps;
        this.waitTime = waitTime;
        initGUI();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setVisible(false);
                dispose();
            }
        });
        setVisible(true);
    }

    private void initGUI() {
        JLabel l = new JLabel(icon);

        getContentPane().add(l, BorderLayout.CENTER);
        progressBar = new JProgressBar(0, 100);
        msgLabel = new JLabel("");
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.NORTH);
        progressPanel.add(msgLabel, BorderLayout.SOUTH);
        getContentPane().add(progressPanel, BorderLayout.SOUTH);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
            screenSize.height / 2 - (labelSize.height / 2));
    }

    public void nextStep(String msg) {
        msgLabel.setText(msg);
        progressBar.setValue((int) Math.ceil(progressBar.getValue() + 100.
            / steps));
        if (progressBar.getValue() == 100) {
            final int pause = waitTime;
            final Runnable closerRunner = new Runnable() {
                public void run() {
                    setVisible(false);
                    dispose();
                }
            };
            Runnable waitRunner = new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(pause);
                        SwingUtilities.invokeAndWait(closerRunner);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // can catch InvocationTargetException
                        // can catch InterruptedException
                    }
                }
            };
            Thread splashThread = new Thread(waitRunner, "SplashThread");
            splashThread.start();
        }
    }
}
