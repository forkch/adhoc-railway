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

package ch.fork.AdHocRailway.ui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SplashWindow extends JWindow {

    private final int steps;
    private final int waitTime;
    private final Icon icon;
    private JLabel msgLabel;
    private JProgressBar progressBar;

    public SplashWindow(Icon icon, Frame f, int waitTime, int steps) {
        super(f);
        this.icon = icon;
        this.steps = steps;
        this.waitTime = waitTime;
        initGUI();

        addMouseListener(new MouseAdapter() {
            @Override
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
        progressBar = new JProgressBar(0, steps);
        msgLabel = new JLabel("Start");
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.NORTH);
        progressPanel.add(msgLabel, BorderLayout.SOUTH);
        getContentPane().add(progressPanel, BorderLayout.SOUTH);

        pack();
    }

    public void nextStep(String msg) {
        msgLabel.setText(msg);
        progressBar.setValue(progressBar.getValue() + 1);
        if (progressBar.getValue() == steps) {
            final int pause = waitTime;
            final Runnable closerRunner = new Runnable() {
                @Override
                public void run() {
                    setVisible(false);
                    dispose();
                }
            };
            Runnable waitRunner = new Runnable() {
                @Override
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
