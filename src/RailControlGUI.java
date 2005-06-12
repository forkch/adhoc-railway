/*------------------------------------------------------------------------
 * 
 * <src/RailControlGUI.java>  -  <desc>
 * 
 * begin     : Sun May 15 13:16:56 CEST 2005
 * copyright : (C)  by Benjamin Mueller 
 * email     : akula@akula.ch
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

import javax.swing.*;
import java.util.*;
import java.awt.*;

public class RailControlGUI extends JFrame {

    private static final String NAME = "RailControl";

    //GUI-Components
    private JTabbedPane trackSwitchPane;
    private JPanel controlPanel;

    private TrackSwitchTab mainLine;
    private TrackSwitchTab mountainLine;

    private JLabel statusBar;

    //Menu
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem quitItem;

    public RailControlGUI() {
        super(NAME);
        initGUI();
    }

    private void initGUI() {
        setFont(new Font("Verdana", Font.PLAIN, 19));
        initTrackSwitchPanel();
        initControlPanel();
        initMenu();
        BorderLayout centerLayout = new BorderLayout();
        JPanel center = new JPanel();
        center.setLayout(centerLayout);
        center.add(trackSwitchPane, BorderLayout.CENTER);
        center.add(controlPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        statusBar = new JLabel("Statusbar...");
        add(statusBar, BorderLayout.SOUTH);
        
        pack();
        setSize(1200,1024);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initTrackSwitchPanel() {
        mainLine = new TrackSwitchTab("Main Line");
        for(int i = 0; i < 30; i++) {
            mainLine.addTrackSwitchWidget(
                new TrackSwitchWidget("Main Line " + i, i));
        }
        mountainLine = new TrackSwitchTab("Mountain Line");
        for(int i = 0; i < 20; i++) {
            mountainLine.addTrackSwitchWidget(
                new TrackSwitchWidget("Mountain Line " + i, i));
        }
        trackSwitchPane = new JTabbedPane(JTabbedPane.BOTTOM);
        trackSwitchPane.addTab("Main Line", new JScrollPane(mainLine));
        trackSwitchPane.addTab("Mountain Line", new JScrollPane(mountainLine));
    }

    private void initControlPanel() {
        controlPanel = new JPanel();
        FlowLayout controlPanelLayout = 
            new FlowLayout(FlowLayout.LEFT, 10, 0);
        controlPanel.setLayout(controlPanelLayout);
        LocomotiveControl loc1 = new LocomotiveControl("Big Boy");
        LocomotiveControl loc2 = new LocomotiveControl("Ascom");
        LocomotiveControl loc3 = new LocomotiveControl("Santa Fe");
        LocomotiveControl loc4 = new LocomotiveControl("Doppelschnauz");
        controlPanel.add(loc1);
        controlPanel.add(loc2);
        controlPanel.add(loc3);
        controlPanel.add(loc4);
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        quitItem = new JMenuItem("Quit");
        fileMenu.add(quitItem);
        helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    public static void main (String[] args) {
        new RailControlGUI();
    }
}
