
package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import ch.fork.AdHocRailway.domain.configuration.Preferences;

public class PreferencesDialog extends JDialog {
    private JSpinner           defaultActivationTime;
    private JSpinner           defaultRoutingDelay;
    private SpinnerNumberModel defaultActivationTimeModel;
    private SpinnerNumberModel defaultRoutingDelayModel;
    private JSpinner           locomotiveControlNumber;
    private JSpinner           switchControlNumber;
    private SpinnerNumberModel locomotiveControlNumberModel;
    private SpinnerNumberModel switchControlNumberModel;
    private JTextField         hostnameTextField;
    private JTextField         portnumberTextField;
    private JTabbedPane        preferencesPane;
    private boolean            cancelPressed = false;
    private boolean            okPressed     = false;
    private JComboBox          keyBoardLayoutComboBox;
    private JCheckBox          interface6051;
    private JCheckBox          writeLog;
    private List<String>       hostnames;

    public PreferencesDialog(JFrame owner) {
        super(owner, "Preferences", true);
    }

    protected void editPreferences(Preferences p) {
        initGUI();
        loadPreferences(p);
        pack();
        setVisible(true);
        if (okPressed) {
            savePreferences(p);
        }
    }

    private void initGUI() {
        preferencesPane = new JTabbedPane();
        preferencesPane.add(createGUISettingsTab(), "General Settings");
        preferencesPane.add(createDigitalDataTab(), "Digital Data");
        preferencesPane.add(createServerTab(), "Server");
        add(preferencesPane, BorderLayout.CENTER);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                PreferencesDialog.this.setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                PreferencesDialog.this.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createGUISettingsTab() {
        JPanel guiSettingsTab = new JPanel(new SpringLayout());
        JLabel locomotiveControlNumberLabel = new JLabel(
            "Number of Locomotive Controls:");
        locomotiveControlNumberModel = new SpinnerNumberModel(5, 1, 10, 1);
        locomotiveControlNumber = new JSpinner(locomotiveControlNumberModel);
        JLabel switchControlNumberLabel = new JLabel(
            "Number of Switch Controls per row:");
        switchControlNumberModel = new SpinnerNumberModel(7, 1, 10, 1);
        switchControlNumber = new JSpinner(switchControlNumberModel);
        JLabel keyBoardLayoutLabel = new JLabel("Keyboard-Layout");
        keyBoardLayoutComboBox = new JComboBox();
        keyBoardLayoutComboBox.addItem("Swiss German");
        keyBoardLayoutComboBox.addItem("English");
        guiSettingsTab.add(locomotiveControlNumberLabel);
        guiSettingsTab.add(locomotiveControlNumber);
        guiSettingsTab.add(switchControlNumberLabel);
        guiSettingsTab.add(switchControlNumber);
        guiSettingsTab.add(keyBoardLayoutLabel);
        guiSettingsTab.add(keyBoardLayoutComboBox);
        SpringUtilities.makeCompactGrid(guiSettingsTab, 3, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        return guiSettingsTab;
    }

    private JPanel createDigitalDataTab() {
        JPanel digitalDataTab = new JPanel(new SpringLayout());
        JLabel defaultActivationTimeLabel = new JLabel(
            "Default activation time for solenoids:");
        defaultActivationTimeModel = new SpinnerNumberModel(50, 50, 1000, 10);
        defaultActivationTime = new JSpinner(defaultActivationTimeModel);
        digitalDataTab.add(defaultActivationTimeLabel);
        digitalDataTab.add(defaultActivationTime);
        JLabel defaultRoutingDelayLabel = new JLabel(
            "Default routing delay for solenoids:");
        defaultRoutingDelayModel = new SpinnerNumberModel(250, 100, 1000, 10);
        defaultRoutingDelay = new JSpinner(defaultRoutingDelayModel);
        interface6051 = new JCheckBox();
        JLabel interface6051Label = new JLabel("Interface 6051 attached");
        writeLog = new JCheckBox();
        JLabel writeLogLabel = new JLabel("Write Log");
        digitalDataTab.add(defaultRoutingDelayLabel);
        digitalDataTab.add(defaultRoutingDelay);
        digitalDataTab.add(interface6051Label);
        digitalDataTab.add(interface6051);
        digitalDataTab.add(writeLogLabel);
        digitalDataTab.add(writeLog);
        SpringUtilities.makeCompactGrid(digitalDataTab, 4, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        return digitalDataTab;
    }

    private JPanel createServerTab() {
        JPanel serverTab = new JPanel(new SpringLayout());
        JLabel hostnameLabel = new JLabel("Hostname (Name or IP):");
        hostnameTextField = new JTextField();
        serverTab.add(hostnameLabel);
        serverTab.add(hostnameTextField);
        JLabel portnumberLabel = new JLabel("Portnumber (e.g. 12345):");
        portnumberTextField = new JTextField("12345", 15);
        serverTab.add(portnumberLabel);
        serverTab.add(portnumberTextField);
        SpringUtilities.makeCompactGrid(serverTab, 2, 2, // rows, cols
            6, 6, // initX, initY
            6, 6); // xPad, yPad
        return serverTab;
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    private void loadPreferences(Preferences p) {
        defaultActivationTimeModel.setValue(p
            .getIntValue("DefaultActivationTime"));
        defaultRoutingDelayModel.setValue(p.getIntValue("DefaultRoutingDelay"));
        hostnameTextField.setText(p.getStringValue("Hostname"));
        portnumberTextField.setText(Integer.toString(p
            .getIntValue("Portnumber")));
        locomotiveControlNumberModel.setValue(p
            .getIntValue("LocomotiveControlesAmount"));
        switchControlNumberModel.setValue(p
            .getIntValue("SwitchControlesAmount"));
        keyBoardLayoutComboBox.setSelectedItem(p
            .getStringValue("KeyBoardLayout"));
        interface6051.setSelected(p.getBooleanValue("Interface6051"));
        writeLog.setSelected(p.getBooleanValue("WriteLog"));
    }

    private void savePreferences(Preferences p) {
        p.setIntValue("DefaultActivationTime", defaultActivationTimeModel
            .getNumber().intValue());
        p.setIntValue("DefaultRoutingDelay", defaultRoutingDelayModel
            .getNumber().intValue());
        p.setStringValue("Hostname", (String) hostnameTextField.getText());
        p.setIntValue("Portnumber", Integer.parseInt(portnumberTextField
            .getText()));
        p.setIntValue("LocomotiveControlesAmount", locomotiveControlNumberModel
            .getNumber().intValue());
        p.setIntValue("SwitchControlesAmount", switchControlNumberModel
            .getNumber().intValue());
        p.setStringValue("KeyBoardLayout", keyBoardLayoutComboBox
            .getSelectedItem().toString());
        p.setBooleanValue("Interface6051", interface6051.isSelected());
        p.setBooleanValue("WriteLog", interface6051.isSelected());
    }
}
