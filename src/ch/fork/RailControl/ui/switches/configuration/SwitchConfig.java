package ch.fork.RailControl.ui.switches.configuration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.Switch.SwitchOrientation;
import ch.fork.RailControl.domain.switches.Switch.SwitchState;
import ch.fork.RailControl.ui.SpringUtilities;
import ch.fork.RailControl.ui.switches.Test;

public class SwitchConfig extends JDialog {

    private Switch mySwitch;

    private boolean okPressed;

    private boolean cancelPressed;

    private JTextField numberTextField;

    private JTextField busTextField;

    private JTextField addressTextField;

    private JTextField descTextField;

    private JComboBox switchTypeComboBox;

    private JComboBox switchDefaultStateComboBox;

    private JComboBox switchOrientationComboBox;

    public SwitchConfig(Frame owner, Switch mySwitch) {
        super(owner, "Switch Config", true);
        this.mySwitch = mySwitch.clone();
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        JPanel configPanel = initConfigPanel();

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mySwitch.setNumber(Integer.parseInt(numberTextField
                    .getText()));
                Switch tmp = mySwitch;
                String value = (String)switchTypeComboBox.getSelectedItem();
                if (value.equals("DefaultSwitch")) {
                    mySwitch = new DefaultSwitch(tmp.getNumber(), tmp
                        .getDesc(), tmp.getBus(), tmp.getAddress());
                } else if (value.equals("DoubleCrossSwitch")) {
                    mySwitch = new DoubleCrossSwitch(tmp.getNumber(),
                        tmp.getDesc(), tmp.getBus(), tmp.getAddress());
                } else if (value.equals("ThreeWaySwitch")) {
                    mySwitch = new ThreeWaySwitch(tmp.getNumber(), tmp
                        .getDesc(), tmp.getBus(), tmp.getAddress());
                }
                mySwitch.setSession(tmp.getSession());
                
                mySwitch.setBus(Integer.parseInt(busTextField.getText()));
                mySwitch
                    .setAddress(new Address(addressTextField.getText()));
                mySwitch
                    .setDefaultState((SwitchState) switchDefaultStateComboBox
                        .getSelectedItem());
                mySwitch
                    .setSwitchOrientation((SwitchOrientation) switchOrientationComboBox
                        .getSelectedItem());
                mySwitch.setDesc(descTextField.getText());

                okPressed = true;
                SwitchConfig.this.setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                SwitchConfig.this.setVisible(false);
            }
        });
        Test test = new Test();
        JLabel gugus = new JLabel(test.getImage(mySwitch));
        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(gugus);

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
        JLabel addressLabel = new JLabel("Address");
        JLabel defaultStateLabel = new JLabel("Default State");
        JLabel orientationLabel = new JLabel("Orientation");
        JLabel descLabel = new JLabel("Desc");

        numberTextField = new JTextField();
        numberTextField.setText(Integer.toString(mySwitch.getNumber()));
        busTextField = new JTextField();
        busTextField.setText(Integer.toString(mySwitch.getBus()));
        addressTextField = new JTextField();
        addressTextField.setText(mySwitch.getAddress().toString());
        descTextField = new JTextField();
        descTextField.setText(mySwitch.getDesc());

        switchTypeComboBox = new JComboBox();
        switchTypeComboBox.addItem("DefaultSwitch");
        switchTypeComboBox.addItem("DoubleCrossSwitch");
        switchTypeComboBox.addItem("ThreeWaySwitch");
        switchTypeComboBox
            .setRenderer(new SwitchTypeComboBoxCellRenderer());
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
        switchOrientationComboBox.setSelectedItem(mySwitch.getSwitchOrientation());

        configPanel.add(numberLabel);
        configPanel.add(numberTextField);
        configPanel.add(typeLabel);
        configPanel.add(switchTypeComboBox);
        configPanel.add(busLabel);
        configPanel.add(busTextField);
        configPanel.add(addressLabel);
        configPanel.add(addressTextField);
        configPanel.add(defaultStateLabel);
        configPanel.add(switchDefaultStateComboBox);
        configPanel.add(orientationLabel);
        configPanel.add(switchOrientationComboBox);
        configPanel.add(descLabel);
        configPanel.add(descTextField);

        SpringUtilities.makeCompactGrid(configPanel, 7, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        return configPanel;
    }

    public Switch getSwitch() {
        return mySwitch;
    }

    public boolean isOkPressed() {
        return okPressed;
    }
}
