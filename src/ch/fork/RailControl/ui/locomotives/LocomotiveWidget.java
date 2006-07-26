package ch.fork.RailControl.ui.locomotives;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveChangeListener;
import ch.fork.RailControl.domain.locomotives.LocomotiveControl;
import ch.fork.RailControl.domain.locomotives.LocomotiveGroup;
import ch.fork.RailControl.domain.locomotives.NoneLocomotive;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.ui.ExceptionProcessor;
import ch.fork.RailControl.ui.ImageTools;

public class LocomotiveWidget extends JPanel implements
    LocomotiveChangeListener {

    private static final long serialVersionUID = 1L;

    private enum LocomotiveActionType {
        ACCELERATE, DECCELERATE, TOGGLE_DIRECTION
    };

    private JComboBox locomotiveComboBox;
    private JComboBox locomotiveGroupComboBox;

    private JLabel image;

    private JLabel desc;

    private JProgressBar speedBar;

    private JButton increaseSpeed;

    private JButton decreaseSpeed;

    private JLabel currentSpeed;

    private JButton stopButton;

    private JButton directionButton;

    private Locomotive myLocomotive;

    private int accelerateKey, deccelerateKey, toggleDirectionKey;

    private JLabel currentDirection;

    private FunctionToggleButton[] functionToggleButtons;

    private Color defaultBackground;
    private Locomotive none;
    private LocomotiveGroup allLocomotives;
    private LocomotiveSelectAction locomotiveSelectAction;
    private LocomotiveGroupSelectAction groupSelectAction;

    public LocomotiveWidget(int accelerateKey, int deccelerateKey,
        int toggleDirectionKey) {
        super();
        this.accelerateKey = accelerateKey;
        this.deccelerateKey = deccelerateKey;
        this.toggleDirectionKey = toggleDirectionKey;
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
        centerPanel.add(controlPanel, BorderLayout.CENTER);
        centerPanel.add(desc, BorderLayout.NORTH);

        add(centerPanel, BorderLayout.CENTER);
        add(selectionPanel, BorderLayout.NORTH);
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

        FunctionToggleButton functionButton = new FunctionToggleButton(
            "Fn");
        FunctionToggleButton f1Button = new FunctionToggleButton("F1");
        FunctionToggleButton f2Button = new FunctionToggleButton("F2");
        FunctionToggleButton f3Button = new FunctionToggleButton("F3");
        FunctionToggleButton f4Button = new FunctionToggleButton("F4");

        functionToggleButtons = new FunctionToggleButton[] {
            functionButton, f1Button, f2Button, f3Button, f4Button };
        Insets margin = new Insets(3, 3, 3, 3);
        GridBagLayout functionControlLayout = new GridBagLayout();

        functionsPanel.setLayout(functionControlLayout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);

        for (int i = 0; i < functionToggleButtons.length; i++) {
            functionToggleButtons[i].setMargin(margin);
            functionToggleButtons[i]
                .addActionListener(new LocomotiveFunctionAction(i));
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            functionControlLayout.setConstraints(
                functionToggleButtons[i], gbc);
            functionsPanel.add(functionToggleButtons[i]);
        }

        return functionsPanel;
    }

    private JPanel initSpeedControl() {
        Dimension size = new Dimension(70, 30);
        JPanel speedControlPanel = new JPanel();

        currentSpeed = new JLabel("0%", SwingConstants.CENTER);
        currentDirection = new JLabel(ImageTools.createImageIcon(
            "icons/forward.png", "Forward", this));
        increaseSpeed = new JButton("+");
        decreaseSpeed = new JButton("-");
        stopButton = new JButton("Stop");
        directionButton = new JButton(ImageTools.createImageIcon(
            "icons/reload.png", "Toggle Direction", this));

        currentSpeed.setMaximumSize(size);
        increaseSpeed.setMaximumSize(size);
        decreaseSpeed.setMaximumSize(size);
        stopButton.setMaximumSize(size);
        directionButton.setMaximumSize(size);
        BoxLayout bl = new BoxLayout(speedControlPanel,
            BoxLayout.PAGE_AXIS);
        speedControlPanel.setLayout(bl);

        currentDirection.setAlignmentX(Component.CENTER_ALIGNMENT);
        increaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        decreaseSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        directionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        increaseSpeed.addActionListener(new IncreaseSpeedAction());
        decreaseSpeed.addActionListener(new DecreaseSpeedAction());
        stopButton.addActionListener(new StopAction());
        directionButton.addActionListener(new ToggleDirectionAction());
        
        speedControlPanel.add(currentDirection);
        speedControlPanel.add(increaseSpeed);
        speedControlPanel.add(decreaseSpeed);
        speedControlPanel.add(stopButton);
        speedControlPanel.add(directionButton);

        return speedControlPanel;
    }

    private void initKeyboardActions() {

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(accelerateKey, 0), "acc"
                + accelerateKey);
        this.getActionMap()
            .put(
                "acc"
                    + accelerateKey,
                new LocomotiveControlAction(
                    LocomotiveActionType.ACCELERATE, 1));

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(deccelerateKey, 0), "dec"
                + deccelerateKey);
        this.getActionMap().put(
            "dec"
                + deccelerateKey,
            new LocomotiveControlAction(LocomotiveActionType.DECCELERATE,
                1));

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(toggleDirectionKey, 0), "tog"
                + toggleDirectionKey);
        this.getActionMap().put(
            "tog"
                + toggleDirectionKey,
            new LocomotiveControlAction(
                LocomotiveActionType.TOGGLE_DIRECTION, 1));
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

        currentSpeed.setText((int) ((double) (myLocomotive
            .getCurrentSpeed())
            / (double) (myLocomotive.getDrivingSteps()) * 100.)
            + "%");

        boolean functions[] = myLocomotive.getFunctions();
        for (int i = 0; i < functions.length; i++) {
            functionToggleButtons[i].setSelected(functions[i]);
        }

        switch (myLocomotive.getDirection()) {
        case FORWARD:
            currentDirection.setIcon(ImageTools.createImageIcon(
                "icons/forward.png", "Forward", this));
            break;
        case REVERSE:
            currentDirection.setIcon(ImageTools.createImageIcon(
                "icons/back.png", "Reverse", this));
            break;
        default:
            currentDirection.setIcon(ImageTools.createImageIcon(
                "icons/forward.png", "Forward", this));
        }
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
                LocomotiveControl.getInstance().setFunctions(
                    myLocomotive, functions);
            } catch (LocomotiveException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }

            speedBar.requestFocus();
        }
    }

    private class LocomotiveControlAction extends AbstractAction {

        private LocomotiveActionType type;

        private long time = 0;

        private int locomotiveNumber;

        public LocomotiveControlAction(LocomotiveActionType type,
            int locomotiveNumber) {
            this.type = type;
            this.locomotiveNumber = locomotiveNumber;

        }

        public void actionPerformed(ActionEvent e) {
            if (time == 0
                || e.getWhen() > time + 200) {
                try {
                    if (type == LocomotiveActionType.ACCELERATE) {
                        LocomotiveControl.getInstance().increaseSpeed(
                            myLocomotive);
                    } else if (type == LocomotiveActionType.DECCELERATE) {
                        LocomotiveControl.getInstance().decreaseSpeed(
                            myLocomotive);
                    } else if (type == LocomotiveActionType.TOGGLE_DIRECTION) {
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
                            LocomotiveControl.getInstance()
                                .increaseSpeedStep(myLocomotive);
                        } else if (type == LocomotiveActionType.DECCELERATE) {
                            LocomotiveControl.getInstance()
                                .decreaseSpeedStep(myLocomotive);
                        }
                    } catch (LocomotiveException e3) {
                        ExceptionProcessor.getInstance().processException(
                            e3);
                    }
                    time = 0;
                }
            }
            updateWidget();
        }
    }

    class LocomotiveSelectAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (locomotiveComboBox.getItemCount() == 0) {
                return;
            }
            if (myLocomotive.getCurrentSpeed() == 0) {
                locomotiveComboBox.setBackground(defaultBackground);
                myLocomotive = (Locomotive) locomotiveComboBox
                    .getSelectedItem();
                updateWidget();
                desc.setText(myLocomotive.getDesc());
                speedBar.requestFocus();
            } else {
                locomotiveComboBox.setSelectedItem(myLocomotive);
                locomotiveComboBox.setBackground(Color.RED);
            }
        }
    }
    class LocomotiveGroupSelectAction extends AbstractAction {
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
    class StopAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {

                LocomotiveControl.getInstance().setSpeed(myLocomotive, 0);
                updateWidget();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }
            speedBar.requestFocus();
        }
    }

    class ToggleDirectionAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance().toggleDirection(
                    myLocomotive);
            } catch (LocomotiveException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
            speedBar.requestFocus();
            updateWidget();
        }
    }

    class IncreaseSpeedAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance()
                    .increaseSpeed(myLocomotive);

                updateWidget();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }

            speedBar.requestFocus();
        }
    }

    class DecreaseSpeedAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try {
                LocomotiveControl.getInstance()
                    .decreaseSpeed(myLocomotive);

                updateWidget();
            } catch (LocomotiveException e3) {
                ExceptionProcessor.getInstance().processException(e3);
            }

            speedBar.requestFocus();
        }
    }

}
