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

package ch.fork.AdHocRailway.ui.turnouts;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.widgets.ConfigurationDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TurnoutProgrammer extends ConfigurationDialog {
    private final TurnoutContext ctx;

    public TurnoutProgrammer(final JFrame owner, final TurnoutContext ctx) {
        super(owner, "Turnout Programmer");
        this.ctx = ctx;
        initGUI();
    }

    private void initGUI() {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        final JPanel buttonPanel = new JPanel(new GridLayout(4, 4));
        for (int i = 1; i <= 252; i = i + 4) {
            final JButton button = new JButton("" + i);
            buttonPanel.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {

                    final TurnoutController turnoutControl = ctx
                            .getTurnoutControl();
                    final int address = Integer.parseInt(e.getActionCommand());
                    turnoutControl.setTurnoutWithAddress(address,
                            TurnoutState.STRAIGHT);

                }
            });
        }
        final JLabel titleLabel = new JLabel("Enter first address of decoder");
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        addMainComponent(mainPanel);
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }
}
