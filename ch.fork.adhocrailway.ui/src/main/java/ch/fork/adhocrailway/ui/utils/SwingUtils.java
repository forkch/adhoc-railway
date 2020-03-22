package ch.fork.adhocrailway.ui.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class SwingUtils {
    public static void addEscapeListener(final JDialog dialog) {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        };

        dialog.getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

    }


    public static void enableDisableSpinners(boolean enable, JSpinner... fields) {
        for (JSpinner field : fields) {
            if(!enable) {
                field.setValue(0);
            }
            field.setEnabled(enable);
        }
    }

}
