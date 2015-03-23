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
import ch.fork.AdHocRailway.model.Constants;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.widgets.ConfigurationDialog;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class TurnoutProgrammer extends ConfigurationDialog {
    private final TurnoutContext ctx;
    private List<TurnoutState> turnoutStates = new ArrayList<>();
    private List<JButton> buttons = new ArrayList<>();

    public TurnoutProgrammer(final JFrame owner, final TurnoutContext ctx) {
        super(owner, "Turnout Programmer", false);
        this.ctx = ctx;
        initGUI();
    }

    private void initGUI() {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        final JPanel buttonPanel = new JPanel(new GridLayout(Constants.MAX_TURNOUT_ADDRESS/9,9,5,5));

        Font dialog = new Font("Dialog", Font.BOLD, 24);
        for (int i = 0; i < Constants.MAX_TURNOUT_ADDRESS; i++) {
            final JButton addressButton = new JButton("" + (i + 1));
            addressButton.setFont(dialog);
            addressButton.setMargin(new Insets(5,5,5,5));
            buttonPanel.add(addressButton);
            turnoutStates.add(TurnoutState.LEFT);
            addressButton.addActionListener(new SwitchAddressAction(i + 1));
            buttons.add(addressButton);
        }
        JScrollPane scrollPane = new JScrollPane(buttonPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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
                buttons.get(address-1).setBackground(Color.RED);
                nextState = TurnoutState.STRAIGHT;
            } else {
                turnoutControl.setTurnoutWithAddress(address,
                        TurnoutState.STRAIGHT);
                buttons.get(address-1).setBackground(Color.GREEN);
                nextState = TurnoutState.LEFT;
            }
            turnoutStates.set(address - 1, nextState);

        }
    }

}
