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
import java.util.*;
import java.util.List;

public class TurnoutProgrammer extends ConfigurationDialog {
    private final TurnoutContext ctx;
    private List<TurnoutState> turnoutStates = new ArrayList<>();

    public TurnoutProgrammer(final JFrame owner, final TurnoutContext ctx) {
        super(owner, "Turnout Programmer", false);
        this.ctx = ctx;
        initGUI();
    }

    private void initGUI() {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        final JPanel buttonPanel = new JPanel(new GridLayout(320/20,20,5,5));
        for (int i = 0; i < 320; i++) {
            final JButton addressButton = new JButton("" + (i + 1));
            addressButton.setFont(new Font("Dialog", Font.BOLD, 14));
            addressButton.setMargin(new Insets(5,5,5,5));
            buttonPanel.add(addressButton);
            turnoutStates.add(TurnoutState.LEFT);
            addressButton.addActionListener(new SwitchAddressAction(i + 1));
        }
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        addMainComponent(mainPanel);
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private class SwitchAddressAction implements ActionListener {
        private int address;

        public SwitchAddressAction(int address) {
            this.address = address;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final TurnoutController turnoutControl = ctx
                    .getTurnoutControl();

            TurnoutState nextState;
            if (TurnoutState.LEFT.equals(turnoutStates.get(address - 1))) {
                turnoutControl.setTurnoutWithAddress(address,
                        TurnoutState.LEFT);
                nextState = TurnoutState.STRAIGHT;
            } else {
                turnoutControl.setTurnoutWithAddress(address,
                        TurnoutState.STRAIGHT);
                nextState = TurnoutState.LEFT;
            }
            turnoutStates.set(address - 1, nextState);

        }
    }

}
