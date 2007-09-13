
package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public abstract class ConfigurationDialog extends JDialog {

    protected boolean okPressed;
    protected boolean cancelPressed;
    public JButton okButton;
    public JButton cancelButton;
    public JPanel mainButtonPanel;

    public ConfigurationDialog(JFrame owner, String title) {
        super(owner, title, true);

        initBasicGUI();

    }
    
    public ConfigurationDialog(JDialog owner, String title) {
        super(owner, title, true);

        initBasicGUI();

    }

    private void initBasicGUI() {
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                ConfigurationDialog.this.setVisible(false);
            }
        });
        cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                ConfigurationDialog.this.setVisible(false);
            }
        });
        mainButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        mainButtonPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                ConfigurationDialog.this.setVisible(false);
            }
        }, "", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        mainButtonPanel.add(okButton);
        //mainButtonPanel.add(cancelButton);
        add(mainButtonPanel, BorderLayout.SOUTH);
    }

    protected void addMainComponent(JComponent mainComponent) {
        add(mainComponent, BorderLayout.CENTER);
    }


    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }
}
