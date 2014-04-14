/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

import ch.fork.AdHocRailway.ui.utils.ImageTools;

import javax.swing.*;

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

        setIcon(ImageTools.createImageIconFromIconSet("unlocked.png"));
        setSelectedIcon(ImageTools.createImageIconFromIconSet("locked.png"));

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
