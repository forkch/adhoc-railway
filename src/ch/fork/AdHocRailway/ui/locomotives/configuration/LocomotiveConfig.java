/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/configuration/LocomotiveConfig.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:57 BST 2006
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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.ui.SpringUtilities;

public class LocomotiveConfig extends JDialog {
	private Locomotive myLocomotive;

	private boolean okPressed;

	private JTextField nameTextField;

	private JTextField busTextField;

	private JTextField addressTextField;

	private JTextField descTextField;

	private JTextField imageTextField;

	private JComboBox locomotiveTypeComboBox;

	private HashMap<String, LocomotiveType> locomotiveTypes;

	private static final String NAME = "Locomotive Config";

	public LocomotiveConfig(Locomotive myLocomotive) {
		super(new JFrame(), NAME, true);
		this.myLocomotive = myLocomotive;
		initGUI();
	}

	public LocomotiveConfig(Frame owner, Locomotive myLocomotive) {
		super(owner, NAME, true);
		this.myLocomotive = myLocomotive;
		initGUI();
	}

	public LocomotiveConfig(JDialog owner, Locomotive myLocomotive) {
		super(owner, NAME, true);
		this.myLocomotive = myLocomotive;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		JPanel configPanel = initConfigPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ApplyChangesAction());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LocomotiveConfig.this.setVisible(false);
			}
		});
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(configPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		setLocationByPlatform(true);
		setSize(new Dimension(200, 300));
		setVisible(true);
	}

	private JPanel initConfigPanel() {
		JPanel configPanel = new JPanel(new SpringLayout());
		JLabel nameLabel = new JLabel("Name");
		JLabel typeLabel = new JLabel("Type");
		JLabel busLabel = new JLabel("Bus");
		JLabel addressLabel = new JLabel("Address");
		JLabel imageLabel = new JLabel("Image");
		JLabel descLabel = new JLabel("Desc");

		nameTextField = new JTextField();
		nameTextField.setText(myLocomotive.getName());

		busTextField = new JTextField();
		busTextField.setText(Integer.toString(myLocomotive.getBus()));

		addressTextField = new JTextField();
		addressTextField.setText("" + myLocomotive.getAddress());

		imageTextField = new JTextField();
		imageTextField.setText(myLocomotive.getImage());

		descTextField = new JTextField();
		descTextField.setText(myLocomotive.getDescription());

		SortedSet<LocomotiveType> types = LocomotiveControl.getInstance()
				.getLocomotiveTypes();
		locomotiveTypeComboBox = new JComboBox();
		locomotiveTypes = new HashMap<String, LocomotiveType>();
		for (LocomotiveType type : types) {
			locomotiveTypeComboBox.addItem(type.getTypeName());
		}
		if (myLocomotive.getLocomotiveType() != null)
			locomotiveTypeComboBox.setSelectedItem(myLocomotive
					.getLocomotiveType().getTypeName());
		else 
			locomotiveTypeComboBox.setSelectedIndex(0);

		configPanel.add(nameLabel);
		configPanel.add(nameTextField);
		configPanel.add(typeLabel);
		configPanel.add(locomotiveTypeComboBox);
		configPanel.add(busLabel);
		configPanel.add(busTextField);
		configPanel.add(addressLabel);
		configPanel.add(addressTextField);
		configPanel.add(imageLabel);
		configPanel.add(imageTextField);
		configPanel.add(descLabel);
		configPanel.add(descTextField);
		SpringUtilities.makeCompactGrid(configPanel, 6, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		return configPanel;
	}

	public Locomotive getLocomotive() {
		return myLocomotive;
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			myLocomotive.setName(nameTextField.getText());
			LocomotiveType newType = LocomotiveControl
					.getInstance()
					.getLocomotiveTypeByName((String)
							locomotiveTypeComboBox.getSelectedItem());
			myLocomotive.setLocomotiveType(newType);

			int bus = Integer.parseInt(busTextField.getText());
			myLocomotive.setBus(bus);

			int newAddress = Integer.parseInt(addressTextField.getText());
			myLocomotive.setAddress(newAddress);

			myLocomotive.setImage(imageTextField.getText());
			myLocomotive.setDescription(descTextField.getText());
			okPressed = true;
			LocomotiveConfig.this.setVisible(false);
		}
	}
}
