package ch.fork.RailControl.Test;
/*------------------------------------------------------------------------
 * 
 * <src/LocomotiveControl.java>  -  <desc>
 * 
 * begin     : Sun May 15 13:55:57 CEST 2005
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
import javax.swing.border.*;
import java.awt.*;

public class LocomotiveControl extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String locName;
    private BorderLayout baseLayout;
    private JLabel name;
    private JLabel image;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private JTextField currentSpeed;
    private JButton increaseSpeed;
    private JButton decreaseSpeed;
    private JButton stopButton;

    public LocomotiveControl(String name) {
        super();
        this.locName = name;
        initGUI();
    }

    private void initGUI() {
        baseLayout = new BorderLayout();
        setLayout(baseLayout);
        name = new JLabel(locName, SwingConstants.CENTER); 
        image = new JLabel("Image", SwingConstants.CENTER);
        add(name, BorderLayout.NORTH);

        initSpeedComponents();
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(150,200));
    }

    public void initSpeedComponents() {
        BorderLayout speedLayout = new BorderLayout();
        JPanel speed = new JPanel();
        speedSlider = new JSlider(JSlider.VERTICAL, 0, 25, 0);
        speedSlider.setPaintTicks(true);
        speedSlider.setMajorTickSpacing(5);
        speedSlider.setMinorTickSpacing(1);
        stopButton = new JButton("Stop");
        speed.setLayout(speedLayout);
        speed.add(image, BorderLayout.NORTH);
        speed.add(speedSlider, BorderLayout.EAST);

        GridBagLayout speedControlLayout = new GridBagLayout();
        JPanel speedControlPanel = new JPanel();
        speedControlPanel.setLayout(speedControlLayout);
        speedLabel = new JLabel("Speed");
        currentSpeed = new JTextField("250");
        increaseSpeed = new JButton("+");
        decreaseSpeed = new JButton("-");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        speedControlLayout.setConstraints(speedLabel, gbc);
        speedControlPanel.add(speedLabel);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        speedControlLayout.setConstraints(currentSpeed, gbc);
        speedControlPanel.add(currentSpeed);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        speedControlLayout.setConstraints(increaseSpeed, gbc);
        speedControlPanel.add(increaseSpeed);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        speedControlLayout.setConstraints(decreaseSpeed, gbc);
        speedControlPanel.add(decreaseSpeed);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        speedControlLayout.setConstraints(stopButton, gbc);
        speedControlPanel.add(stopButton);
        speed.add(speedControlPanel, BorderLayout.CENTER);

        add(speed, BorderLayout.CENTER);
    }
}
