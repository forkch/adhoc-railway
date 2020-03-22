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

package ch.fork.adhocrailway.ui;

import ch.fork.adhocrailway.controllers.RailwayDevice;
import ch.fork.adhocrailway.controllers.impl.dummy.DummyListener;
import ch.fork.adhocrailway.controllers.impl.dummy.DummyRailwayController;
import ch.fork.adhocrailway.model.AdHocRailwayException;
import ch.fork.adhocrailway.model.turnouts.TurnoutState;
import ch.fork.adhocrailway.railway.brain.brain.BrainController;
import ch.fork.adhocrailway.railway.brain.brain.BrainListener;
import ch.fork.adhocrailway.technical.configuration.Preferences;
import ch.fork.adhocrailway.ui.context.ApplicationContext;
import ch.fork.adhocrailway.ui.utils.SwingUtils;
import ch.fork.adhocrailway.ui.widgets.ConfigurationDialog;
import com.google.common.collect.Lists;
import de.dermoba.srcp.common.TokenizedLine;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.common.exception.SRCPUnsufficientDataException;
import de.dermoba.srcp.common.exception.SRCPWrongValueException;
import de.dermoba.srcp.devices.listener.GMInfoListener;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ch.fork.adhocrailway.technical.configuration.PreferencesKeys.RAILWAY_DEVICE;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class BrainTerminal extends ConfigurationDialog implements GMInfoListener, BrainListener, DummyListener {
    private final ApplicationContext ctx;
    private final LinkedList<String> commandHistory = Lists.newLinkedList();
    private int historyIndex = 0;
    private List<TurnoutState> turnoutStates = new ArrayList<>();
    private List<JButton> buttons = new ArrayList<>();
    private JTextArea brainResponses;

    public BrainTerminal(final JFrame owner, final ApplicationContext ctx) {
        super(owner, "AdHoc-Brain Terminal", false);
        this.ctx = ctx;
        initGUI();

        switch (getRailwayDevice()) {

            case SRCP:
                ctx.getSession().getInfoChannel().addGMInfoListener(this);
                break;
            case ADHOC_BRAIN:
                BrainController.getInstance().addBrainListener(this);
                break;
            case NULL_DEVICE:
                DummyRailwayController.getInstance().addDummyListener(this);
                break;
        }
    }

    private void initGUI() {
        final JPanel mainPanel = new JPanel(new BorderLayout());

        brainResponses = new JTextArea(24, 80);
        brainResponses.setFocusable(false);
        brainResponses.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        DefaultCaret caret = (DefaultCaret)brainResponses.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        final JScrollPane historyScrollPane = new JScrollPane(brainResponses, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);

        brainResponses.setAutoscrolls(true);

        final JTextField commandTextField = new JTextField();
        commandTextField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        commandTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = commandTextField.getText();
                commandHistory.addFirst(command);
                sendCommand(command);
                commandTextField.setText("");
                historyIndex = -1;

            }
        });

        commandTextField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        commandTextField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");

        commandTextField.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                historyIndex++;
                if (historyIndex < commandHistory.size()) {
                    commandTextField.setText(commandHistory.get(historyIndex));
                } else {
                    historyIndex = commandHistory.size()-1;
                }
            }
        });
        commandTextField.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                historyIndex--;
                if (historyIndex >= 0) {
                    commandTextField.setText(commandHistory.get(historyIndex));
                } else {
                    historyIndex = -1;
                }
            }
        });

        mainPanel.add(historyScrollPane, BorderLayout.CENTER);
        mainPanel.add(commandTextField, BorderLayout.SOUTH);


        addMainComponent(mainPanel);
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                switch (getRailwayDevice()) {

                    case SRCP:
                        ctx.getSession().getInfoChannel().removeGMInfoListener(BrainTerminal.this);
                        break;
                    case ADHOC_BRAIN:
                        BrainController.getInstance().removeBrainListener(BrainTerminal.this);
                        break;
                    case NULL_DEVICE:
                        DummyRailwayController.getInstance().removeDummyListener(BrainTerminal.this);
                        break;
                }
            }
        });
        setVisible(true);


    }


    public void sendCommand(String command) {

        final RailwayDevice railwayDevive = getRailwayDevice();
        if (!ctx.getRailwayDeviceManager().isConnected() && railwayDevive != RailwayDevice.NULL_DEVICE) {
            throw new AdHocRailwayException("not connected");
        }
        if (RailwayDevice.SRCP == railwayDevive) {
            try {
                ctx.getSession().getCommandChannel().send(String.format("SET 0 GM 0 0 BRAINCMD %s", command));
            } catch (SRCPException e) {
                throw new AdHocRailwayException("failed to send command to brain", e);
            }
        } else if (RailwayDevice.ADHOC_BRAIN == railwayDevive) {
            BrainController.getInstance().write(command);
        } else {
            DummyRailwayController.getInstance().send(command);
        }
    }

    private RailwayDevice getRailwayDevice() {
        final Preferences preferences = ctx.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        return RailwayDevice
                .fromString(railwayDeviceString);
    }

    @Override
    public void GMset(double v, int i, int i1, int i2, String s, TokenizedLine tokenizedLine) throws SRCPUnsufficientDataException, NumberFormatException, SRCPWrongValueException {
        receivedResponseFromBrain(s);

    }

    private void receivedResponseFromBrain(String response) {
        brainResponses.append("\n" + response);
    }

    @Override
    public void sentMessage(String sentMessage) {

    }

    @Override
    public void receivedMessage(String receivedMessage) {
        receivedResponseFromBrain(receivedMessage);
    }

    @Override
    public void brainReset(String receivedMessage) {

    }

    @Override
    public void brainMessage(String receivedMessage) {

    }

    @Override
    public void sentDummyMessage(String receivedMessage) {
        receivedResponseFromBrain(receivedMessage);

    }
}
