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

import javax.swing.JToggleButton;

import ch.fork.AdHocRailway.ui.ImageTools;

public class FunctionToggleButton extends JToggleButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5886703909665855244L;

	public FunctionToggleButton(final String text) {
		super(text);
		setIcon(ImageTools.createImageIconFromIconSet("dialog-error.png"));
		setSelectedIcon(ImageTools
				.createImageIconFromIconSet("dialog-ok-apply.png"));

	}
}
