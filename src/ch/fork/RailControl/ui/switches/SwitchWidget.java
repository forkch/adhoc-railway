package ch.fork.RailControl.ui.switches;
/*------------------------------------------------------------------------
 * 
 * <src/TrackSwitchWidget.java>  -  <desc>
 * 
 * begin     : Sun May 15 18:09:11 CEST 2005
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

import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchException;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SwitchWidget extends JPanel {

	private static final long serialVersionUID = 1L;
	private Switch mySwitch;
    private SwitchColorBox greenBox;
    private SwitchColorBox redBox;
    private JPanel greenPanel;
    private JPanel redPanel;

    private JLabel descLabel;

    public SwitchWidget(Switch aSwitch) {
    	mySwitch = aSwitch;
        initGUI();
    }

    private void initGUI() {
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(layout);

        gbc.gridx = 0;
        greenBox = new SwitchColorBox(Color.GREEN);
        greenPanel = new JPanel();
        greenPanel.setPreferredSize(new Dimension(50,20));
        layout.setConstraints(greenPanel, gbc);
        add(greenPanel);

        gbc.gridx = 1;
        redBox = new SwitchColorBox(Color.RED);
        redPanel = new JPanel();
        redPanel.setPreferredSize(new Dimension(50,20));
        layout.setConstraints(redPanel, gbc);
        add(redPanel);

        gbc.gridx = 2;
        descLabel = new JLabel(mySwitch.getDesc());
        layout.setConstraints(descLabel, gbc);
        add(descLabel);
        
        addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				try {
					SwitchControl.getInstance().toggle(mySwitch);
					if(redBox.isActivated()) {
						redBox.deactivate();
						greenBox.activate();
					} else {
						greenBox.deactivate();
						redBox.activate();
					}
				} catch (SwitchException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

        });
    }

    public void setGreen() {
        greenPanel.setBackground(Color.GREEN);
        redBox.setBackground(new Color(238,238,238));
    }

    public void setRed() {
        redPanel.setBackground(Color.RED);
        greenPanel.setBackground(new Color(238,238,238));
    }
}
