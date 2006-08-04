
package ch.fork.AdHocRailway.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExceptionProcessor {
    private JFrame                    parent;
    private static ExceptionProcessor instance;
    

    private ExceptionProcessor(JFrame parent) {
        this.parent = parent;
        
    }

    public static ExceptionProcessor getInstance(JFrame parent) {
        if (instance == null) {
            instance = new ExceptionProcessor(parent);
        }
        return instance;
    }

    public static ExceptionProcessor getInstance() {
        return instance;
    }

    public void processException(Exception e) {
        String exceptionMsg = e.getMessage();
        if (e.getCause() != null) {
            exceptionMsg += ":\n\nCause: " + e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(parent, exceptionMsg, "Error occured",
            JOptionPane.ERROR_MESSAGE, ImageTools.createImageIcon(
                "icons/messagebox_critical.png", "Critical", this));
        e.printStackTrace();
        
    }

    public void processException(String msg, Exception e) {
        String exceptionMsg = e.getMessage();
        msg = msg + "\n" + exceptionMsg;
        if (e.getCause() != null) {
            exceptionMsg += ":\n\nCause: " + e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(parent, msg, "Error occured",
            JOptionPane.ERROR_MESSAGE, ImageTools.createImageIcon(
                "icons/messagebox_critical.png", "Critical", this));
        e.printStackTrace();
    }
}
