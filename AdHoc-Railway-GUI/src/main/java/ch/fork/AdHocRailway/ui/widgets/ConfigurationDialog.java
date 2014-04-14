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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ConfigurationDialog extends JDialog {

    public JButton okButton;
    public JButton cancelButton;
    public JPanel mainButtonPanel;
    protected boolean okPressed;
    protected boolean cancelPressed;

    public ConfigurationDialog(JFrame owner, String title) {
        super(owner, title, true);

        initBasicGUI();

    }

    public ConfigurationDialog(JDialog owner, String title) {
        super(owner, title, true);

        initBasicGUI();

    }

    private void initBasicGUI() {
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                ConfigurationDialog.this.setVisible(false);
            }
        });
        cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                ConfigurationDialog.this.setVisible(false);
            }
        });
        mainButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        mainButtonPanel.add(okButton);
        add(mainButtonPanel, BorderLayout.SOUTH);
    }

    protected void addMainComponent(JComponent mainComponent) {
        add(mainComponent, BorderLayout.CENTER);
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }
}
