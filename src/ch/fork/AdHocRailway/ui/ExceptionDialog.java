
package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ExceptionDialog extends JDialog {

    private static ExceptionDialog instance = null;
    private JLabel                 messageLabel;
    private static final String    NAME     = "Error";

    private ExceptionDialog(JFrame owner) throws HeadlessException {
        super(owner, NAME, true);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 100));
        messageLabel = new JLabel();
        messageLabel.setIcon(ImageTools.createImageIcon(
            "icons/messagebox_critical.png", "Critical", this));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JButton detailsButton = new JButton("Details");
        messageLabel.setIconTextGap(20);
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        southPanel.add(okButton);
        //southPanel.add(detailsButton);
        add(messageLabel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        setLocationByPlatform(true);
        pack();
    }

    public static void start(JFrame parent) {
        if (instance == null) {
            instance = new ExceptionDialog(parent);
        }
    }

    public static ExceptionDialog getInstance() {
        return instance;
    }

    public void processException(Exception e) {
        String msg = e.getMessage();
        
        messageLabel.setText(msg);
        pack();
        setVisible(true);
    }

}
