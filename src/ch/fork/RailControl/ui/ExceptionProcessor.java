package ch.fork.RailControl.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExceptionProcessor {

    private JFrame parent;

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
        String msg = e.getMessage();
        if (e.getCause() != null) {
            msg += ":\n"
                + e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
            parent, msg, "Error occured", JOptionPane.ERROR_MESSAGE);
        //e.printStackTrace();
    }

    public void processException(String msg, Exception e) {
        String exceptionMsg = e.getMessage();
        msg = msg
            + "\n" + exceptionMsg;
        if (e.getCause() != null) {
            exceptionMsg += ":\n"
                + e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(
            parent, msg, "Error occured", JOptionPane.ERROR_MESSAGE);
        //e.printStackTrace();
    }
}
