/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchConfig.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:11 BST 2006
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


package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.sun.corba.se.impl.ior.NewObjectKeyTemplateBase;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchOrientation;
import ch.fork.AdHocRailway.ui.SpringUtilities;

public class SwitchConfig extends JDialog {
    private Switch     mySwitch;
    private boolean    okPressed;
    private JTextField numberTextField;
    private JTextField busTextField;
    private JTextField address0TextField;
    private JTextField address1TextField;
    private JCheckBox  switched0Checkbox;
    private JCheckBox  switched1Checkbox;
    private JTextField descTextField;
    private JComboBox  switchTypeComboBox;
    private JComboBox  switchDefaultStateComboBox;
    private JComboBox  switchOrientationComboBox;

    public SwitchConfig(Switch mySwitch) {
        super(new JFrame(), "Switch Config", true);
        this.mySwitch = mySwitch;
        initGUI();
    }

    public SwitchConfig(Frame owner, Switch mySwitch) {
        super(owner, "Switch Config", true);
        this.mySwitch = mySwitch.clone();
        initGUI();
    }

    public SwitchConfig(JDialog owner, Switch mySwitch) {
        super(owner, "Switch Config", true);
        this.mySwitch = mySwitch.clone();
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
                SwitchConfig.this.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(configPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setLocationByPlatform(true);
        pack();
        setVisible(true);
    }

    private JPanel initConfigPanel() {
        JPanel configPanel = new JPanel(new SpringLayout());
        JLabel numberLabel = new JLabel("Number");
        JLabel typeLabel = new JLabel("Type");
        JLabel busLabel = new JLabel("Bus");
        JLabel address0Label = new JLabel("Address 1");
        final JLabel address1Label = new JLabel("Address 2");
        JLabel address0SwitchedLabel = new JLabel("Address 1 switched");
        final JLabel address1SwitchedLabel = new JLabel("Address 2 switched");
        JLabel defaultStateLabel = new JLabel("Default State");
        JLabel orientationLabel = new JLabel("Orientation");
        JLabel descLabel = new JLabel("Desc");

        numberTextField = new JTextField();
        numberTextField.setText(Integer.toString(mySwitch.getNumber()));

        busTextField = new JTextField();
        busTextField.setText(Integer.toString(mySwitch.getAddress(0).getBus()));

        address0TextField = new JTextField();
        address0TextField.setText("" + mySwitch.getAddress(0).getAddress());

        address1TextField = new JTextField();
        address1TextField.setEnabled(false);
        address1Label.setEnabled(false);
        if (mySwitch instanceof ThreeWaySwitch) {
            address1TextField.setText("" + mySwitch.getAddress(1).getAddress());
            address1TextField.setEnabled(true);
            address1Label.setEnabled(true);
        }
        switched0Checkbox = new JCheckBox();
        switched0Checkbox.setSelected(mySwitch.getAddress(0)
            .isAddressSwitched());

        switched1Checkbox = new JCheckBox();
        switched1Checkbox.setEnabled(false);
        address1SwitchedLabel.setEnabled(false);
        if (mySwitch instanceof ThreeWaySwitch) {
            switched1Checkbox.setSelected(mySwitch.getAddress(1)
                .isAddressSwitched());
            switched1Checkbox.setEnabled(true);
            address1SwitchedLabel.setEnabled(true);
        }


        descTextField = new JTextField();
        descTextField.setText(mySwitch.getDesc());

        switchTypeComboBox = new JComboBox();
        switchTypeComboBox.addItem("DefaultSwitch");
        switchTypeComboBox.addItem("DoubleCrossSwitch");
        switchTypeComboBox.addItem("ThreeWaySwitch");
        switchTypeComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String selected = ((String) switchTypeComboBox
                    .getSelectedItem());
                if (selected.equals("ThreeWaySwitch")) {
                    address1Label.setEnabled(true);
                    address1TextField.setEnabled(true);

                    address1SwitchedLabel.setEnabled(true);
                    switched1Checkbox.setEnabled(true);
                }
            }

        });
        switchTypeComboBox.setRenderer(new SwitchTypeComboBoxCellRenderer());
        switchTypeComboBox.setSelectedItem(mySwitch.getType());
        switchDefaultStateComboBox = new JComboBox();
        switchDefaultStateComboBox.addItem(SwitchState.STRAIGHT);
        switchDefaultStateComboBox.addItem(SwitchState.LEFT);
        switchDefaultStateComboBox
            .setRenderer(new SwitchDefaultStateComboBoxCellRenderer());
        switchDefaultStateComboBox.setSelectedItem(mySwitch.getDefaultState());
        switchOrientationComboBox = new JComboBox();
        switchOrientationComboBox.addItem(SwitchOrientation.NORTH);
        switchOrientationComboBox.addItem(SwitchOrientation.EAST);
        switchOrientationComboBox.addItem(SwitchOrientation.SOUTH);
        switchOrientationComboBox.addItem(SwitchOrientation.WEST);
        switchOrientationComboBox.setSelectedItem(mySwitch
            .getSwitchOrientation());
        configPanel.add(numberLabel);
        configPanel.add(numberTextField);
        configPanel.add(typeLabel);
        configPanel.add(switchTypeComboBox);
        configPanel.add(busLabel);
        configPanel.add(busTextField);
        configPanel.add(address0Label);
        configPanel.add(address0TextField);
        configPanel.add(address1Label);
        configPanel.add(address1TextField);
        configPanel.add(address0SwitchedLabel);
        configPanel.add(switched0Checkbox);
        configPanel.add(address1SwitchedLabel);
        configPanel.add(switched1Checkbox);
        configPanel.add(defaultStateLabel);
        configPanel.add(switchDefaultStateComboBox);
        configPanel.add(orientationLabel);
        configPanel.add(switchOrientationComboBox);
        configPanel.add(descLabel);
        configPanel.add(descTextField);
        SpringUtilities.makeCompactGrid(configPanel, 10, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        return configPanel;
    }

    public Switch getSwitch() {
    	System.out.println(mySwitch);
        return mySwitch;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    class ApplyChangesAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int newNumber = Integer.parseInt(numberTextField.getText());
            int newBus = Integer.parseInt(busTextField.getText());
            int newAddress0 = Integer.parseInt(address0TextField.getText());
            int newAddress1 = 0;
            if(address1TextField.isEnabled())
            	newAddress1 = Integer.parseInt(address1TextField.getText());
            
            boolean newSwitched0 = switched0Checkbox.isSelected();
            boolean newSwitched1 = false;
            if(switched1Checkbox.isEnabled())
            	newSwitched1 = switched1Checkbox.isSelected();
            
            SwitchState newDefaultState = (SwitchState) switchDefaultStateComboBox
            .getSelectedItem();
            SwitchOrientation newSwitchOrientation = (SwitchOrientation)switchOrientationComboBox.getSelectedItem();
            
            String newDesc = descTextField.getText();
            
            String value = (String) switchTypeComboBox.getSelectedItem();
            if (value.equals("DefaultSwitch")) {
                mySwitch = new DefaultSwitch(newNumber, newDesc, new Address(newBus, newAddress0));
            } else if (value.equals("DoubleCrossSwitch")) {
                mySwitch = new DoubleCrossSwitch(newNumber, newDesc, new Address(newBus, newAddress0));
            } else if (value.equals("ThreeWaySwitch")) {
                if (!(mySwitch instanceof ThreeWaySwitch)) {
                    Address[] newAddresses = new Address[2];
                    newAddresses[0] = new Address(newBus, newAddress0);
                    newAddresses[1] = new Address(newBus, 0);

                    mySwitch = new ThreeWaySwitch(newNumber, newDesc, newAddresses);
                } else {
                	Address[] newAddresses = new Address[2];
                    newAddresses[0] = new Address(newBus, newAddress0);
                    newAddresses[1] = new Address(newBus, newAddress1);
                	
                    mySwitch = new ThreeWaySwitch(newNumber, newDesc, newAddresses);
                }
            }

            mySwitch.setNumber(Integer.parseInt(numberTextField.getText()));
            
            mySwitch.getAddress(0).setBus(newBus);
            mySwitch.getAddress(0).setAddress(newAddress0);
            mySwitch.getAddress(0).setAddressSwitched(newSwitched0);
            if (mySwitch.getAddresses().length == 2) {
                mySwitch.getAddress(1).setBus(newBus);
                mySwitch.getAddress(1).setAddress(newAddress1);
                mySwitch.getAddress(1).setAddressSwitched(newSwitched1);
            }


            mySwitch.setDefaultState(newDefaultState);
            mySwitch
                .setSwitchOrientation(newSwitchOrientation);
            mySwitch.setDesc(newDesc);
            okPressed = true;
            SwitchConfig.this.setVisible(false);
        }
    }
}
