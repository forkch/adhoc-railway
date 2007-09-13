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

import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.SpringUtilities;

public class TurnoutConfig extends JDialog {
    private Turnout     myTurnout;
    private boolean    okPressed;
    private JTextField numberTextField;
    private JTextField busTextField;
    private JTextField address1TextField;
    private JTextField address2TextField;
    private JCheckBox  switched1Checkbox;
    private JCheckBox  switched2Checkbox;
    private JTextField descTextField;
    private JComboBox  turnoutTypeComboBox;
    private JComboBox  turnoutDefaultStateComboBox;
    private JComboBox  turnoutOrientationComboBox;
    
    private TurnoutPersistenceIface turnoutPersitence = HibernateTurnoutPersistence.getInstance();

    public TurnoutConfig(Turnout myTurnout) {
        super(new JFrame(), "Turntou Config", true);
        this.myTurnout = myTurnout;
        initGUI();
    }

    public TurnoutConfig(Frame owner, Turnout myTurnout) {
        super(owner, "Turnout Config", true);
        this.myTurnout = myTurnout;
        initGUI();
    }

    public TurnoutConfig(JDialog owner, Turnout myTurnout) {
        super(owner, "Turnout Config", true);
        this.myTurnout = myTurnout;
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
                TurnoutConfig.this.setVisible(false);
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
        JLabel address1Label = new JLabel("Address 1");
        final JLabel address2Label = new JLabel("Address 2");
        JLabel address1SwitchedLabel = new JLabel("Address 1 switched");
        final JLabel address2SwitchedLabel = new JLabel("Address 2 switched");
        JLabel defaultStateLabel = new JLabel("Default State");
        JLabel orientationLabel = new JLabel("Orientation");
        JLabel descLabel = new JLabel("Desc");

        numberTextField = new JTextField();
        numberTextField.setText(Integer.toString(myTurnout.getNumber()));

        busTextField = new JTextField();
        busTextField.setText(Integer.toString(myTurnout.getBus1()));

        address1TextField = new JTextField();
        address1TextField.setText("" + myTurnout.getAddress1());

        address2TextField = new JTextField();
        address2TextField.setEnabled(false);
        address2Label.setEnabled(false);
        if (myTurnout.isThreeWay()) {
            address2TextField.setText("" + myTurnout.getAddress2());
            address2TextField.setEnabled(true);
            address2Label.setEnabled(true);
        }
        switched1Checkbox = new JCheckBox();
        switched1Checkbox.setSelected(myTurnout.isAddress1Switched());

        switched2Checkbox = new JCheckBox();
        switched2Checkbox.setEnabled(false);
        address2SwitchedLabel.setEnabled(false);
        if (myTurnout.isThreeWay()) {
            switched2Checkbox.setSelected(myTurnout.isAddress2Switched());
            switched2Checkbox.setEnabled(true);
            address2SwitchedLabel.setEnabled(true);
        }


        descTextField = new JTextField();
        descTextField.setText(myTurnout.getDescription());

        turnoutTypeComboBox = new JComboBox();
        turnoutTypeComboBox.addItem(TurnoutTypes.DEFAULT);
        turnoutTypeComboBox.addItem(TurnoutTypes.DOUBLECROSS);
        turnoutTypeComboBox.addItem(TurnoutTypes.THREEWAY);
        turnoutTypeComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	TurnoutTypes selected = ((TurnoutTypes) turnoutTypeComboBox
                    .getSelectedItem());
                if (selected == TurnoutTypes.THREEWAY) {
                    address2Label.setEnabled(true);
                    address2TextField.setEnabled(true);

                    address2SwitchedLabel.setEnabled(true);
                    switched2Checkbox.setEnabled(true);
                }
            }

        });
        turnoutTypeComboBox.setRenderer(new TurnoutTypeComboBoxCellRenderer());
        turnoutTypeComboBox.setSelectedItem(myTurnout.getTurnoutType().getTurnoutTypeEnum());
        
        turnoutDefaultStateComboBox = new JComboBox();
        turnoutDefaultStateComboBox.addItem(TurnoutState.STRAIGHT);
        turnoutDefaultStateComboBox.addItem(TurnoutState.LEFT);
        turnoutDefaultStateComboBox
            .setRenderer(new TurnoutDefaultStateComboBoxCellRenderer());
        turnoutDefaultStateComboBox.setSelectedItem(myTurnout.getDefaultStateEnum());
        
        turnoutOrientationComboBox = new JComboBox();
        turnoutOrientationComboBox.addItem(TurnoutOrientation.NORTH);
        turnoutOrientationComboBox.addItem(TurnoutOrientation.EAST);
        turnoutOrientationComboBox.addItem(TurnoutOrientation.SOUTH);
        turnoutOrientationComboBox.addItem(TurnoutOrientation.WEST);
        turnoutOrientationComboBox.setSelectedItem(myTurnout
            .getOrientationEnum());
        
        configPanel.add(numberLabel);
        configPanel.add(numberTextField);
        configPanel.add(typeLabel);
        configPanel.add(turnoutTypeComboBox);
        configPanel.add(busLabel);
        configPanel.add(busTextField);
        configPanel.add(address1Label);
        configPanel.add(address1TextField);
        configPanel.add(address2Label);
        configPanel.add(address2TextField);
        configPanel.add(address1SwitchedLabel);
        configPanel.add(switched1Checkbox);
        configPanel.add(address2SwitchedLabel);
        configPanel.add(switched2Checkbox);
        configPanel.add(defaultStateLabel);
        configPanel.add(turnoutDefaultStateComboBox);
        configPanel.add(orientationLabel);
        configPanel.add(turnoutOrientationComboBox);
        configPanel.add(descLabel);
        configPanel.add(descTextField);
        SpringUtilities.makeCompactGrid(configPanel, 10, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        return configPanel;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    class ApplyChangesAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int newNumber = Integer.parseInt(numberTextField.getText());
            int newBus = Integer.parseInt(busTextField.getText());
            int newAddress1 = Integer.parseInt(address1TextField.getText());
            int newAddress2 = 0;
            if(address2TextField.isEnabled())
            	newAddress2 = Integer.parseInt(address2TextField.getText());
            
            boolean newSwitched1 = switched1Checkbox.isSelected();
            boolean newSwitched2 = false;
            if(switched2Checkbox.isEnabled())
            	newSwitched2 = switched2Checkbox.isSelected();
            
            TurnoutState newDefaultState = (TurnoutState) turnoutDefaultStateComboBox
            .getSelectedItem();
            TurnoutOrientation newOrientation = (TurnoutOrientation)turnoutOrientationComboBox.getSelectedItem();
            
            String newDescription = descTextField.getText();
            
            TurnoutTypes newType = (TurnoutTypes)turnoutTypeComboBox.getSelectedItem();
            myTurnout.setTurnoutType(turnoutPersitence.getTurnoutType(newType));

            myTurnout.setNumber(newNumber);
            
            myTurnout.setBus1(newBus);
            myTurnout.setAddress1(newAddress1);
            myTurnout.setAddress1Switched(newSwitched1);
            if (myTurnout.isThreeWay()) {
                myTurnout.setBus1(newBus);
                myTurnout.setAddress2(newAddress2);
                myTurnout.setAddress2Switched(newSwitched2);
            }


            myTurnout.setDefaultStateEnum(newDefaultState);
            myTurnout
                .setOrientationEnum(newOrientation);
            myTurnout.setDescription(newDescription);
            okPressed = true;
            TurnoutConfig.this.setVisible(false);
        }
    }
}
