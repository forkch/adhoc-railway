package ch.fork.RailControl.ui.locomotives;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

public class FunctionToggleButton extends JToggleButton {

    public FunctionToggleButton() {
        super();
    }

    public FunctionToggleButton(Icon icon) {
        super(icon);
    }

    public FunctionToggleButton(Icon icon, boolean selected) {
        super(icon, selected);
    }

    public FunctionToggleButton(String text) {
        super(text);
        /*
         * setIcon(ImageTools.createImageIcon( "icons/button_cancel.png",
         * "Disabled", this)); setSelectedIcon(ImageTools.createImageIcon(
         * "icons/button_ok.png", "Enabled", this));
         */
    }

    public FunctionToggleButton(String text, boolean selected) {
        super(text, selected);
    }

    public FunctionToggleButton(Action a) {
        super(a);
    }

    public FunctionToggleButton(String text, Icon icon) {
        super(text, icon);
    }

    public FunctionToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
    }

}
