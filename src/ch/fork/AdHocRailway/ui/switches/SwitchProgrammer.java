
package ch.fork.AdHocRailway.ui.switches;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class SwitchProgrammer extends JDialog {
    private SRCPSession session;

    public SwitchProgrammer(JFrame owner, SRCPSession session) {
        super(owner, "Switch Programmer", true);
        this.session = session;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(4, 4));
        for (int i = 1; i <= 112; i = i + 4) {
            JButton button = new JButton("" + i);
            buttonPanel.add(button);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int address = Integer.parseInt(e.getActionCommand());
                    GA ga = new GA(session);
                    ga.setAddress(address);
                    ga.setBus(1);
                    try {
                        ga.set(0, 1, 1000);
                    } catch (SRCPException e1) {
                        ExceptionProcessor.getInstance().processException(e1);
                    }
                }
            });
        }
        JLabel titleLabel = new JLabel("Enter first address of decoder");
        add(titleLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }
}