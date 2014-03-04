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

package ch.fork.AdHocRailway.ui.widgets;

import javax.swing.*;
import java.awt.*;

public class SmallToolbarButton extends JButton {

    private static final Insets insets = new Insets(1, 1, 1, 1);

    public SmallToolbarButton(Action a) {
        super(a);
        setMargin(insets);
        setFocusable(false);
        setText("");
    }
}
