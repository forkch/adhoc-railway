
package ch.fork.AdHocRailway.ui.locomotives;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import ch.fork.AdHocRailway.ui.ImageTools;

public class LockToggleButton extends JToggleButton {
    public LockToggleButton() {
        super();
    }

    public LockToggleButton(Icon icon) {
        super(icon);
    }

    public LockToggleButton(Icon icon, boolean selected) {
        super(icon, selected);
    }

    public LockToggleButton(String text) {
        super(text);

        setIcon(ImageTools.createImageIcon("icons/unlocked.png",
            "Disabled", this));
        setSelectedIcon(ImageTools.createImageIcon("icons/locked.png",
            "Enabled", this));

    }

    public LockToggleButton(String text, boolean selected) {
        super(text, selected);
    }

    public LockToggleButton(Action a) {
        super(a);
    }

    public LockToggleButton(String text, Icon icon) {
        super(text, icon);
    }

    public LockToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
    }
}
