package ch.fork.RailControl.ui.switches;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import ch.fork.RailControl.ui.ExceptionProcessor;

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
        setLayout(new GridLayout(4, 4));
        for(int i = 1; i <= 112; i=i+4) {
            JButton button = new JButton("" + i);
            add(button);
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
        pack();
        setVisible(true);
    }
}
