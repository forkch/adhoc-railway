/*------------------------------------------------------------------------
 * 
 * <src/TrackSwitchWidget.java>  -  <desc>
 * 
 * begin     : Sun May 15 18:09:11 CEST 2005
 * copyright : (C)  by Benjamin Mueller 
 * email     : akula@akula.ch
 * language  : java
 * version   : $Id:$
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
import javax.swing.border.*;
import java.awt.*;

public class TrackSwitchWidget extends JPanel {

    private String desc;
    private int number;
    private TrackSwitchColorBox greenPanel;
    private TrackSwitchColorBox redPanel;

    private JLabel numberLabel;
    private JLabel descLabel;

    public TrackSwitchWidget(String desc, int number) {
        this.desc = desc;
        this.number = number;
        initGUI();
    }

    private void initGUI() {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(layout);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10,10,10,10);
        numberLabel = new JLabel(Integer.valueOf(number).toString());
        layout.setConstraints(numberLabel, gbc);
        add(numberLabel);

        gbc.gridx = 1;
        greenPanel = new TrackSwitchColorBox(Color.GREEN);
        layout.setConstraints(greenPanel, gbc);
        add(greenPanel);

        gbc.gridx = 2;
        redPanel = new TrackSwitchColorBox(Color.RED);
        layout.setConstraints(redPanel, gbc);
        add(redPanel);

        gbc.gridx = 3;
        descLabel = new JLabel(desc);
        layout.setConstraints(descLabel, gbc);
        add(descLabel);
    }

    public void setGreen(boolean value) {
        if(value) {
            greenPanel.activate();
            redPanel.deactivate();
        } else {
            redPanel.activate();
            greenPanel.deactivate();
        }
    }
}
