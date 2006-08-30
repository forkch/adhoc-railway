/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchesControlPanel.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:29 BST 2006
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


package ch.fork.AdHocRailway.ui.switches;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.switches.canvas.Segment7;

public class SwitchesControlPanel extends JPanel {
    private SwitchGroupPane switchGroupPane;
    private Segment7        seg1;
    private Segment7        seg2;
    private Segment7        seg3;
    private StringBuffer    enteredNumberKeys;
    private JPanel          switchesHistory;
    private JFrame          frame;

    public SwitchesControlPanel(JFrame frame) {
        this.frame = frame;
        enteredNumberKeys = new StringBuffer();
        initGUI();
        initKeyboardActions();
    }

    public void update(Collection<SwitchGroup> switchGroups) {
        switchGroupPane.update(switchGroups);
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        switchGroupPane = new SwitchGroupPane(frame);
        JPanel segmentPanelNorth = new JPanel(new FlowLayout(
            FlowLayout.TRAILING, 5, 0));
        segmentPanelNorth.setBackground(new Color(0, 0, 0));
        seg1 = new Segment7();
        seg2 = new Segment7();
        seg3 = new Segment7();
        segmentPanelNorth.add(seg3);
        segmentPanelNorth.add(seg2);
        segmentPanelNorth.add(seg1);
        switchesHistory = new JPanel();
        JPanel sh1 = new JPanel(new BorderLayout());
        sh1.add(switchesHistory, BorderLayout.NORTH);
        BoxLayout boxLayout = new BoxLayout(switchesHistory, BoxLayout.Y_AXIS);
        switchesHistory.setLayout(boxLayout);
        JPanel segmentPanel = new JPanel(new BorderLayout());
        segmentPanel.add(segmentPanelNorth, BorderLayout.NORTH);
        segmentPanel.add(sh1, BorderLayout.CENTER);

        add(switchGroupPane, BorderLayout.CENTER);
        add(segmentPanel, BorderLayout.EAST);
    }

    private void initKeyboardActions() {
        for (int i = 0; i <= 10; i++) {
            registerKeyboardAction(new NumberEnteredAction(), Integer
                .toString(i), KeyStroke.getKeyStroke(Integer.toString(i)),
                WHEN_IN_FOCUSED_WINDOW);
            registerKeyboardAction(new NumberEnteredAction(), Integer
                .toString(i), KeyStroke.getKeyStroke("NUMPAD"
                + Integer.toString(i)), WHEN_IN_FOCUSED_WINDOW);

        }
        registerKeyboardAction(new SwitchingAction(), "\n", KeyStroke
            .getKeyStroke("ENTER"), WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(new SwitchingAction(), "+", KeyStroke
            .getKeyStroke(KeyEvent.VK_ADD, 0), WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(new SwitchingAction(), "", KeyStroke
            .getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(new SwitchingAction(), "/", KeyStroke
            .getKeyStroke(KeyEvent.VK_DIVIDE, 0), WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(new SwitchingAction(), "*", KeyStroke
            .getKeyStroke(KeyEvent.VK_MULTIPLY, 0), WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(new SwitchingAction(), "-", KeyStroke
            .getKeyStroke(KeyEvent.VK_SUBTRACT, 0), WHEN_IN_FOCUSED_WINDOW);

        for (int i = 1; i <= 12; i++) {
            registerKeyboardAction(new SwitchGroupChangeAction(), Integer
                .toString(i - 1), KeyStroke.getKeyStroke("F"
                + Integer.toString(i)), WHEN_IN_FOCUSED_WINDOW);
        }
    }

    private void resetSelectedSwitchDisplay() {
        enteredNumberKeys = new StringBuffer();
        seg1.setValue(0);
        seg2.setValue(0);
        seg3.setValue(0);
        seg1.repaint();
        seg2.repaint();
        seg3.repaint();
    }

    private class NumberEnteredAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            enteredNumberKeys.append(e.getActionCommand());
            String switchNumberAsString = enteredNumberKeys.toString();
            int switchNumber = Integer.parseInt(switchNumberAsString);
            int origNumber = switchNumber;
            if (switchNumber > 999) {
                resetSelectedSwitchDisplay();
                return;
            }
            int seg1Value = switchNumber % 10;
            seg1.setValue(seg1Value);
            seg1.repaint();
            switchNumber = switchNumber - seg1Value;
            int seg2Value = (switchNumber % 100) / 10;
            seg2.setValue(seg2Value);
            seg2.repaint();
            switchNumber = switchNumber - seg2Value * 10;
            int seg3Value = (switchNumber % 1000) / 100;
            seg3.setValue(seg3Value);
            seg3.repaint();
            switchNumber = switchNumber - seg3Value * 100;

        }
    }
    private class SwitchingAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (enteredNumberKeys.toString().equals("")) {
                return;
            }
            String switchNumberAsString = enteredNumberKeys.toString();
            int switchNumber = Integer.parseInt(switchNumberAsString);
            Switch searchedSwitch = null;
            SwitchControl sc = SwitchControl.getInstance();
            searchedSwitch = sc.getNumberToSwitch().get(switchNumber);
            if (searchedSwitch == null) {
                resetSelectedSwitchDisplay();
                return;
            }
            try {
                if (e.getActionCommand().equals("/")) {
                    handleDivide(searchedSwitch);
                } else if (e.getActionCommand().equals("*")) {
                    handleMultiply(searchedSwitch);
                } else if (e.getActionCommand().equals("-")) {
                    handleMinus(searchedSwitch);
                } else if (e.getActionCommand().equals("+")) {
                    if (!(searchedSwitch instanceof ThreeWaySwitch)) {
                        handlePlus(searchedSwitch);
                    }
                } else if (e.getActionCommand().equals("")) {
                    if (!(searchedSwitch instanceof ThreeWaySwitch)) {
                        handlePlus(searchedSwitch);
                    }
                } else if (e.getActionCommand().equals("\n")) {
                    handleEnter(searchedSwitch);
                }
                resetSelectedSwitchDisplay();
                SwitchWidget sw = new SwitchWidget(searchedSwitch, null, true,
                    frame);
                SwitchControl.getInstance().addSwitchChangeListener(sw);
                Component[] oldWidgets = switchesHistory.getComponents();
                switchesHistory.removeAll();
                switchesHistory.add(sw);
                if (oldWidgets.length > 0
                    && sw.getMySwitch() != ((SwitchWidget) oldWidgets[0])
                        .getMySwitch())
                    switchesHistory.add(oldWidgets[0]);
                if (oldWidgets.length > 1
                    && sw.getMySwitch() != ((SwitchWidget) oldWidgets[1])
                        .getMySwitch())
                    switchesHistory.add(oldWidgets[1]);
                repaint();
                revalidate();
            } catch (SwitchException e1) {
                resetSelectedSwitchDisplay();
                ExceptionProcessor.getInstance().processException(e1);
            }
        }

        private void handleDivide(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setCurvedLeft(aSwitch);
        }

        private void handleMultiply(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setStraight(aSwitch);
        }

        private void handleMinus(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setCurvedRight(aSwitch);
        }

        private void handlePlus(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setCurvedLeft(aSwitch);
        }

        private void handleEnter(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setStraight(aSwitch);
        }
    }
    private class SwitchGroupChangeAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            int selectedSwitchGroup = Integer.parseInt(e.getActionCommand());
            if (selectedSwitchGroup < SwitchControl.getInstance()
                .getSwitchGroups().size()) {
                switchGroupPane.setSelectedIndex(selectedSwitchGroup);
            }
        }
    }
}
