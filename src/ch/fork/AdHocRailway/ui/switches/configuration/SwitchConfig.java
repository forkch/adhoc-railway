
package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchOrientation;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchState;
import ch.fork.AdHocRailway.ui.SpringUtilities;

public class SwitchConfig extends JDialog {
    private Switch     mySwitch;
    private boolean    okPressed;
    private boolean    cancelPressed;
    private JTextField numberTextField;
    private JTextField busTextField;
    private JTextField address0TextField;
    private JTextField address1TextField;
    private JTextField descTextField;
    private JComboBox  switchTypeComboBox;
    private JComboBox  switchDefaultStateComboBox;
    private JComboBox  switchOrientationComboBox;

    public SwitchConfig(Switch mySwitch) {
        super(new JFrame(), "Switch Config", true);
        this.mySwitch = mySwitch.clone();
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
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
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
        JLabel address1Label = new JLabel("Address 2");
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


        descTextField = new JTextField();
        descTextField.setText(mySwitch.getDesc());

        switchTypeComboBox = new JComboBox();
        switchTypeComboBox.addItem("DefaultSwitch");
        switchTypeComboBox.addItem("DoubleCrossSwitch");
        switchTypeComboBox.addItem("ThreeWaySwitch");
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
        configPanel.add(defaultStateLabel);
        configPanel.add(switchDefaultStateComboBox);
        configPanel.add(orientationLabel);
        configPanel.add(switchOrientationComboBox);
        configPanel.add(descLabel);
        configPanel.add(descTextField);
        SpringUtilities.makeCompactGrid(configPanel, 8, 2, // rows, cols
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

    class ApplyChangesAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            mySwitch.setNumber(Integer.parseInt(numberTextField.getText()));
            Switch tmp = mySwitch;
            String value = (String) switchTypeComboBox.getSelectedItem();
            if (value.equals("DefaultSwitch")) {
                mySwitch = new DefaultSwitch(tmp.getNumber(), tmp.getDesc(),
                    tmp.getAddress(0));
            } else if (value.equals("DoubleCrossSwitch")) {
                mySwitch = new DoubleCrossSwitch(tmp.getNumber(),
                    tmp.getDesc(), tmp.getAddress(0));
            } else if (value.equals("ThreeWaySwitch")) {
                mySwitch = new ThreeWaySwitch(tmp.getNumber(), tmp.getDesc(),
                    tmp.getAddresses());
            }
            int bus = Integer.parseInt(busTextField.getText());
            mySwitch.getAddress(0).setBus(bus
                );
            if (mySwitch.getAddresses().length == 2) {
                mySwitch.getAddress(1).setBus(bus);
            }
            Address[] newAddress = null;
            if (mySwitch instanceof ThreeWaySwitch) {
                newAddress = new Address[] {
                    new Address(address0TextField.getText()),
                    new Address(address1TextField.getText()) };
            } else {
                newAddress = new Address[] { new Address(address0TextField
                    .getText()) };
            }

            mySwitch.setAddresses(newAddress);
            mySwitch.setDefaultState((SwitchState) switchDefaultStateComboBox
                .getSelectedItem());
            mySwitch
                .setSwitchOrientation((SwitchOrientation) switchOrientationComboBox
                    .getSelectedItem());
            mySwitch.setDesc(descTextField.getText());
            okPressed = true;
            SwitchConfig.this.setVisible(false);
        }
    }
}
