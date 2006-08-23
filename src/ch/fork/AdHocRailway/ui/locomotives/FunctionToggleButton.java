/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/FunctionToggleButton.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:04 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/


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
