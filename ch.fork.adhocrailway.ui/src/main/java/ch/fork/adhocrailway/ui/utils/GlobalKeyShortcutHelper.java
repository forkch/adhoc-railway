package ch.fork.adhocrailway.ui.utils;

import javax.swing.*;

/**
 * Created by fork on 4/9/14.
 */
public class GlobalKeyShortcutHelper {
    public static void registerKey(JComponent component, final int keyEvent, int modifiers, Action action) {
        final KeyStroke stroke = KeyStroke.getKeyStroke(keyEvent, modifiers);
        component.registerKeyboardAction(action, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
