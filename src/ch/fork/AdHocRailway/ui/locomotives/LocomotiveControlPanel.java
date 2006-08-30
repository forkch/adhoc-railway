/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/LocomotiveControlPanel.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:59 BST 2006
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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class LocomotiveControlPanel extends JPanel {
    private int[][]                keyBindingsUS = new int[][] {
        { KeyEvent.VK_A, KeyEvent.VK_Z, KeyEvent.VK_Q },
        { KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_W },
        { KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_E },
        { KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_R },
        { KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_T },
        { KeyEvent.VK_H, KeyEvent.VK_N, KeyEvent.VK_Y },
        { KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_U },
        { KeyEvent.VK_K, KeyEvent.VK_COMMA, KeyEvent.VK_I },
        { KeyEvent.VK_L, KeyEvent.VK_DECIMAL, KeyEvent.VK_O },
        { KeyEvent.VK_COLON, KeyEvent.VK_MINUS, KeyEvent.VK_P } };
    private int[][]                keyBindingsDE = new int[][] {
        { KeyEvent.VK_A, KeyEvent.VK_Y, KeyEvent.VK_Q },
        { KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_W },
        { KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_E },
        { KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_R },
        { KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_T },
        { KeyEvent.VK_H, KeyEvent.VK_N, KeyEvent.VK_Z },
        { KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_U },
        { KeyEvent.VK_K, KeyEvent.VK_COMMA, KeyEvent.VK_I },
        { KeyEvent.VK_L, KeyEvent.VK_DECIMAL, KeyEvent.VK_O },
        { KeyEvent.VK_COLON, KeyEvent.VK_MINUS, KeyEvent.VK_P } };
    private int[][]                keyBindings   = keyBindingsDE;
    private List<LocomotiveWidget> locomotiveWidgets;
    private JFrame                 frame;

    public LocomotiveControlPanel(JFrame frame) {
        super();
        this.frame = frame;
        locomotiveWidgets = new ArrayList<LocomotiveWidget>();
        initGUI();
    }

    private void initGUI() {
        FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
        setLayout(controlPanelLayout);
        registerKeyboardAction(new LocomotiveStopAction(), "", KeyStroke
            .getKeyStroke(KeyEvent.VK_SPACE, 0), WHEN_IN_FOCUSED_WINDOW);

    }

    public void update(Collection<LocomotiveGroup> locomotiveGroups) {
        LocomotiveControl lc = LocomotiveControl.getInstance();
        LockControl lockc = LockControl.getInstance();
        for (Component c : getComponents()) {
            lc.removeLocomotiveChangeListener((LocomotiveChangeListener) c);
            lockc.removeLockChangeListener((LockChangeListener) c);
        }
        removeAll();
        locomotiveWidgets.clear();
        if (Preferences.getInstance().getStringValue(
            PreferencesKeys.KEYBOARD_LAYOUT).equals("Swiss German")) {
            keyBindings = keyBindingsDE;
        } else {
            keyBindings = keyBindingsUS;
        }
        for (int i = 0; i < Preferences.getInstance().getIntValue(
            PreferencesKeys.LOCOMOTIVE_CONTROLES); i++) {
            LocomotiveWidget w = new LocomotiveWidget(keyBindings[i][0],
                keyBindings[i][1], keyBindings[i][2], frame);
            LocomotiveControl.getInstance().addLocomotiveChangeListener(w);
            LockControl.getInstance().addLockChangeListener(w);
            w.updateLocomotiveGroups(locomotiveGroups);
            add(w);
            locomotiveWidgets.add(w);
        }
    }

    private class LocomotiveStopAction extends AbstractAction implements
        Runnable {
        public void actionPerformed(ActionEvent e) {
            Thread t = new Thread(this);
            t.start();
        }

        public void run() {
            try {
                for (LocomotiveWidget widget : locomotiveWidgets) {
                    Locomotive myLocomotive = widget.getMyLocomotive();
                    LocomotiveControl.getInstance().setSpeed(myLocomotive, 0);
                    widget.updateWidget();
                    Thread.sleep(200);
                }
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            } catch (InterruptedException e) {
                ExceptionProcessor.getInstance().processException(e);
            }
        }
    }
}
