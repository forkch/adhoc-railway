/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/LocomotiveWidget.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:02 BST 2006
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locking.LockChangeListener;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locking.exception.LockingException;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.NoneLocomotive;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfig;

public class LocomotiveWidget extends JPanel implements
    LocomotiveChangeListener, LockChangeListener {
    private static final long serialVersionUID = 1L;

    private enum LocomotiveActionType {
        ACCELERATE, DECCELERATE, TOGGLE_DIRECTION
    };

    private JComboBox                   locomotiveComboBox;
    private JComboBox                   locomotiveGroupComboBox;
    private JLabel                      image;
    private JLabel                      desc;
    private JProgressBar                speedBar;
    private JButton                     increaseSpeed;
    private JButton                     decreaseSpeed;
    private JLabel                      currentSpeed;
    private JButton                     stopButton;
    private JButton                     directionButton;
    private LockToggleButton            lockButton;
    private Locomotive                  myLocomotive;
    private int                         accelerateKey, deccelerateKey,
        toggleDirectionKey;
    private FunctionToggleButton[]      functionToggleButtons;
    private Color                       defaultBackground;
    private Locomotive                  none;
    private LocomotiveGroup             allLocomotives;
    private LocomotiveSelectAction      locomotiveSelectAction;
    private LocomotiveGroupSelectAction groupSelectAction;
    private JFrame                      frame;

    public LocomotiveWidget(int accelerateKey, int deccelerateKey,
        int toggleDirectionKey, JFrame frame) {
        super();
        this.accelerateKey = accelerateKey;
        this.deccelerateKey = deccelerateKey;
        this.toggleDirectionKey = toggleDirectionKey;
        this.frame = frame;
        initGUI();
        initKeyboardActions();
    }

    private void initGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(200, 250));
        JPanel selectionPanel = initSelectionPanel();
        JPanel controlPanel = initControlPanel();
        JPanel centerPanel = new JPanel(new BorderLayout());
        desc = new JLabel(myLocomotive.getDesc(), SwingConstants.CENTER);

        addMouseListener(new MouseAction());
        centerPanel.add(controlPanel, BorderLayout.CENTER);
        centerPanel.add(desc, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(selectionPanel, BorderLayout.NORTH);

        addMouseWheelListener(new WheelControl());

    }

    private JPanel initSelectionPanel() {
        JPanel selectionPanel = new JPanel(new BorderLayout(5, 5));
        locomotiveGroupComboBox = new JComboBox();
        locomotiveGroupComboBox.setFocusable(false);
        groupSelectAction = new LocomotiveGroupSelectAction();
        locomotiveComboBox = new JComboBox();
        none = new NoneLocomotive();
        locomotiveComboBox.addItem(none);
        locomotiveComboBox.setFocusable(false);
        myLocomotive = none;
        defaultBackground = locomotiveComboBox.getBackground();
        locomotiveSelectAction = new LocomotiveSelectAction();
        selectionPanel.add(locomotiveGroupComboBox, BorderLayout.NORTH);
        selectionPanel.add(locomotiveComboBox, BorderLayout.SOUTH);
        return selectionPanel;
    }

    private JPanel initControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        speedBar = new JProgressBar(JProgressBar.VERTICAL);
        speedBar.setPreferredSize(new Dimension(20, 200));
        controlPanel.add(speedBar, BorderLayout.EAST);
        JPanel functionsPanel = initFunctionsControl();
        JPanel speedControlPanel = initSpeedControl();
        controlPanel.add(functionsPanel, BorderLayout.WEST);
        controlPanel.add(speedControlPanel, BorderLayout.CENTER);
        return controlPanel;
    }

    private JPanel initFunctionsControl() {
        JPanel functionsPanel = new JPanel();
        BoxLayout bl = new BoxLayout(functionsPanel, BoxLayout.PAGE_AXIS);
        functionsPanel.setLayout(bl);
        Dimension size = new Dimension(60, 30);
        Insets margin = new Insets(2, 2, 2, 2);

        FunctionToggleButton functionButton = new FunctionToggleButton("Fn");
        FunctionToggleButton f1Button = new FunctionToggleButton("F1");
        FunctionToggleButton f2Button = new FunctionToggleButton("F2");
        FunctionToggleButton f3Button = new FunctionToggleButton("F3");
        FunctionToggleButton f4Button = new FunctionToggleButton("F4");
        functionToggleButtons = new FunctionToggleButton[] { functionButton,
            f1Button, f2Button, f3Button, f4Button };

        for (int i = 0; i < functionToggleButtons.length; i++) {
            functionToggleButtons[i].setMargin(margin);
            functionToggleButtons[i].setMaximumSize(size);
            functionToggleButtons[i]
                .addActionListener(new LocomotiveFunctionAction(i));
            functionsPanel.add(functionToggleButtons[i]);
        }
        return functionsPanel;
    }

    private JPanel initSpeedControl() {

        JPanel speedControlPanel = new JPanel();
        BoxLayout bl = new BoxLayout(speedControlPanel, BoxLayout.PAGE_AXIS);
        speedControlPanel.setLayout(bl);
        Dimension size = new Dimension(60, 30);
        Insets margin = new Insets(2, 2, 2, 2);

        increaseSpeed = new JButton("+");
        decreaseSpeed = new JButton("-");
        stopButton = new JButton("Stop");
        directionButton = new JButton(ImageTools.createImageIcon(
            "icons/forward.png", "Toggle Direction", this));
        lockButton = new LockToggleButton("");

        increaseSpeed.setMaximumSize(size);
        decreaseSpeed.setMaximumSize(size);
        stopButton.setMaximumSize(size);
        directionButton.setMaximumSize(size);
        lockButton.setMaximumSize(size);

        increaseSpeed.setMargin(margin);
        decreaseSpeed.setMargin(margin);
        stopButton.setMargin(margin);
        directionButton.setMargin(margin);
        lockButton.setMargin(margin);

        increaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        decreaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        directionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        increaseSpeed.addActionListener(new IncreaseSpeedAction());
        decreaseSpeed.addActionListener(new DecreaseSpeedAction());
        stopButton.addActionListener(new StopAction());
        directionButton.addActionListener(new ToggleDirectionAction());
        lockButton.addActionListener(new LockAction());

        speedControlPanel.add(increaseSpeed);
        speedControlPanel.add(decreaseSpeed);
        speedControlPanel.add(stopButton);
        speedControlPanel.add(directionButton);
        speedControlPanel.add(lockButton);
        return speedControlPanel;
    }

    private void initKeyboardActions() {
        registerKeyboardAction(new LocomotiveControlAction(), "accelerate",
            KeyStroke.getKeyStroke(accelerateKey, 0), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(new LocomotiveControlAction(), "deccelerate",
            KeyStroke.getKeyStroke(deccelerateKey, 0), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(new LocomotiveControlAction(),
            "toggle_direction", KeyStroke.getKeyStroke(toggleDirectionKey, 0),
            WHEN_IN_FOCUSED_WINDOW);
    }

    protected void updateWidget() {
        double speedInPercent = ((double) myLocomotive.getCurrentSpeed())
            / ((double) myLocomotive.getDrivingSteps());
        if (speedInPercent > 0.9) {
            speedBar.setForeground(new Color(255, 0, 0));
        } else if (speedInPercent > 0.7) {
            speedBar.setForeground(new Color(255, 255, 0));
        } else {
            speedBar.setForeground(new Color(0, 255, 0));
        }
        speedBar.setMinimum(0);
        speedBar.setMaximum(myLocomotive.getDrivingSteps());
        speedBar.setValue(myLocomotive.getCurrentSpeed());
        boolean functions[] = myLocomotive.getFunctions();
        for (int i = 0; i < functions.length; i++) {
            functionToggleButtons[i].setSelected(functions[i]);
        }
        switch (myLocomotive.getDirection()) {
        case FORWARD:
            directionButton.setIcon(ImageTools.createImageIcon(
                "icons/forward.png", "Forward", this));
            break;
        case REVERSE:
            directionButton.setIcon(ImageTools.createImageIcon(
                "icons/back.png", "Reverse", this));
            break;
        default:
            directionButton.setIcon(ImageTools.createImageIcon(
                "icons/forward.png", "Forward", this));
        }
        lockButton.setSelected(myLocomotive.isLocked());
        if (myLocomotive.isLocked()) {
            if (lockedByMe()) {
                lockButton.setSelectedIcon(ImageTools.createImageIcon(
                    "icons/locked_by_me.png", "Locked by me", this));

            } else {
                lockButton.setSelectedIcon(ImageTools.createImageIcon(
                    "icons/locked_by_enemy.png", "Locked by enemy", this));

            }
        }
        lockButton.revalidate();
        lockButton.repaint();
        setPreferredSize(new Dimension(200, 250));
    }

    public void updateLocomotiveGroups(
        Collection<LocomotiveGroup> locomotiveGroups) {
        locomotiveComboBox.removeActionListener(locomotiveSelectAction);
        locomotiveGroupComboBox.removeActionListener(groupSelectAction);
        allLocomotives = new LocomotiveGroup("All");
        for (LocomotiveGroup lg : locomotiveGroups) {
            locomotiveGroupComboBox.addItem(lg);
            for (Locomotive l : lg.getLocomotives()) {
                locomotiveComboBox.addItem(l);
            }
        }
        locomotiveGroupComboBox.insertItemAt(allLocomotives, 0);
        locomotiveGroupComboBox.setSelectedItem(allLocomotives);
        locomotiveComboBox.addActionListener(locomotiveSelectAction);
        locomotiveGroupComboBox.addActionListener(groupSelectAction);
    }

    public Locomotive getMyLocomotive() {
        return myLocomotive;
    }

    public void locomotiveChanged(Locomotive changedLocomotive) {
        if (myLocomotive.equals(changedLocomotive)) {
            SwingUtilities.invokeLater(new LocomotiveWidgetUpdater(
                changedLocomotive));
        }
    }

    private class LocomotiveWidgetUpdater implements Runnable {
        private Locomotive locomotive;

        public LocomotiveWidgetUpdater(Locomotive l) {
            this.locomotive = l;
        }

        public void run() {
            updateWidget();
        }
    }
    
    public void lockChanged(ControlObject changedLock) {
        if (changedLock instanceof Locomotive) {
            Locomotive changedLocomotive = (Locomotive) changedLock;
            locomotiveChanged(changedLocomotive);
        }
    }

    private class LocomotiveFunctionAction extends AbstractAction {
        private int function;

        public LocomotiveFunctionAction(int function) {
            this.function = function;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                boolean[] functions = myLocomotive.getFunctions();
                functions[function] = functionToggleButtons[function]
                    .isSelected();
                LocomotiveControl.getInstance().setFunctions(myLocomotive,
                    functions);
            } catch (LocomotiveException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
            speedBar.requestFocus();
        }
    }
    private class LocomotiveControlAction extends AbstractAction {
        private LocomotiveActionType type;
        private long                 time = 0;

        public void actionPerformed(ActionEvent e) {
            if (time == 0 || e.getWhen() > time + 200) {
                try {
                    if (e.getActionCommand().equals("accelerate")) {
                        LocomotiveControl.getInstance().increaseSpeed(
                            myLocomotive);
                    } else if (e.getActionCommand().equals("deccelerate")) {
                        LocomotiveControl.getInstance().decreaseSpeed(
                            myLocomotive);
                    } else if (e.getActionCommand().equals("toggle_direction")) {
                        LocomotiveControl.getInstance().toggleDirection(
                            myLocomotive);
                    }
                    if (time == 0) {
                        time = System.currentTimeMillis();
                    } else {
                        time = 0;
                    }
                } catch (LocomotiveException e3) {
                    ExceptionProcessor.getInstance().processException(e3);
                }
            } else {
                if (e.getWhen() > time + 1000) {
                    try {
                        if (type == LocomotiveActionType.ACCELERATE) {
                            LocomotiveControl.getInstance().increaseSpeedStep(
                                myLocomotive);
                        } else if (type == LocomotiveActionType.DECCELERATE) {
                            LocomotiveControl.getInstance().decreaseSpeedStep(
                                myLocomotive);
                        }
                    } catch (LocomotiveException e3) {
                        ExceptionProcessor.getInstance().processException(e3);
                    }
                    time = 0;
                }
            }
            updateWidget();
        }
    }
    private class LocomotiveSelectAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (locomotiveComboBox.getItemCount() == 0) {
                return;
            }
            if (myLocomotive.getCurrentSpeed() == 0 && !lockedByMe()) {
                locomotiveComboBox.setBackground(defaultBackground);
                lockButton.setBackground(defaultBackground);
                myLocomotive = (Locomotive) locomotiveComboBox
                    .getSelectedItem();

                LocomotiveControl.getInstance().addLocomotiveChangeListener(
                    myLocomotive, LocomotiveWidget.this);

                updateWidget();
                desc.setText(myLocomotive.getDesc());
                speedBar.requestFocus();
            } else {
                locomotiveComboBox.setSelectedItem(myLocomotive);
                locomotiveComboBox.setBackground(Color.RED);
            }
        }
    }
    private class LocomotiveGroupSelectAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            LocomotiveGroup lg = (LocomotiveGroup) locomotiveGroupComboBox
                .getSelectedItem();
            locomotiveComboBox.removeAllItems();
            Locomotive none = new NoneLocomotive();
            locomotiveComboBox.addItem(none);
            if (lg == allLocomotives) {
                SortedSet<Locomotive> sl = new TreeSet<Locomotive>(
                    LocomotiveControl.getInstance().getLocomotives());
                for (Locomotive l : sl) {
                    locomotiveComboBox.addItem(l);
                }
            } else {
                for (Locomotive l : lg.getLocomotives()) {
                    locomotiveComboBox.addItem(l);
                }
            }
            locomotiveComboBox.setSelectedIndex(0);
            locomotiveComboBox.revalidate();
            locomotiveComboBox.repaint();
        }
    }
    private class StopAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().setSpeed(myLocomotive, 0);
                updateWidget();
                speedBar.requestFocus();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
        }
    }
    private class ToggleDirectionAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().toggleDirection(myLocomotive);
                speedBar.requestFocus();
                updateWidget();
            } catch (LocomotiveException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
        }
    }
    private class IncreaseSpeedAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().increaseSpeed(myLocomotive);
                updateWidget();
                speedBar.requestFocus();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
        }
    }
    private class DecreaseSpeedAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().decreaseSpeed(myLocomotive);
                updateWidget();
                speedBar.requestFocus();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
        }
    }

    private class LockAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            LockControl lc = LockControl.getInstance();
            try {
                if (lockButton.isSelected()) {
                    boolean succeeded = lc.acquireLock(myLocomotive);
                    lockButton.setSelected(succeeded);
                } else {
                    if (lockedByMe()) {
                        boolean succeeded = !lc.releaseLock(myLocomotive);
                        lockButton.setSelected(succeeded);
                    } else {
                        lockButton.setSelected(true);
                    }
                }
                updateWidget();
                speedBar.requestFocus();
            } catch (LockingException e1) {
                lockButton.setSelected(false);
                ExceptionProcessor.getInstance().processException(e1);
            }
        }

    }

    private class MouseAction extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {

            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                LocomotiveConfig locomotiveConfig = new LocomotiveConfig(frame,
                    myLocomotive);
                if (locomotiveConfig.isOkPressed()) {
                    LocomotiveControl lc = LocomotiveControl.getInstance();
                    lc.unregisterLocomotive(myLocomotive);

                    myLocomotive = locomotiveConfig.getLocomotive();
                    lc.registerLocomotive(myLocomotive);
                    desc.setText(myLocomotive.getDesc());
                    locomotiveChanged(myLocomotive);
                }
            }
        }
    }

    private class WheelControl implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {
            AbstractAction a = null;
            switch (e.getWheelRotation()) {
            case -1:
                a = new IncreaseSpeedAction();
                break;
            case 1:
                a = new DecreaseSpeedAction();
                break;
            default:
                return;
            }
            a.actionPerformed(null);
        }
    }

    private boolean lockedByMe() {
        return LockControl.getInstance().getSessionID() == myLocomotive
            .getLockedBySession();
    }
}
