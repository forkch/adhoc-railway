
package ch.fork.AdHocRailway.ui.locomotives;

import javax.swing.JToggleButton;

import ch.fork.AdHocRailway.ui.ImageTools;

public class FunctionToggleButton extends JToggleButton {
    
    public FunctionToggleButton(String text) {
        super(text);

        setIcon(ImageTools.createImageIcon("icons/button_cancel.png",
            "Disabled", this));
        setSelectedIcon(ImageTools.createImageIcon("icons/button_ok.png",
            "Enabled", this));

    }
}
