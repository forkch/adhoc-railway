package ch.fork.AdHocRailway.ui.power;

import ch.fork.AdHocRailway.controllers.PowerChangeListener;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.model.power.Booster;
import ch.fork.AdHocRailway.model.power.BoosterState;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.PowerContext;
import ch.fork.AdHocRailway.ui.utils.ImageTools;
import ch.fork.AdHocRailway.ui.utils.UIConstants;
import ch.fork.AdHocRailway.ui.widgets.SimpleInternalFrame;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.dermoba.srcp.model.power.SRCPPowerSupply;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class PowerControlPanel extends JPanel implements PowerChangeListener {

    private static final Logger LOGGER = Logger
            .getLogger(PowerControlPanel.class);
    private final ImageIcon stopIcon;
    private final ImageIcon goIcon;
    private final ImageIcon shortcutIcon;
    private final Map<Integer, JToggleButton> numberToPowerToggleButtons = new HashMap<Integer, JToggleButton>();
    private final Map<Integer, ActionListener> numberToActionListener = new HashMap<Integer, ActionListener>();
    private final Map<JToggleButton, Integer> powerToggleButtonsToNumber = new HashMap<JToggleButton, Integer>();
    private final PowerContext ctx;
    private int numberOfBoosters;
    private JPanel powerControlPanel;
    private JButton allBoostersOn;
    private JButton allBoostersOff;

    public PowerControlPanel(final PowerContext ctx) {
        super();
        this.ctx = ctx;
        final EventBus mainBus = ctx.getMainBus();
        mainBus.register(this);
        stopIcon = ImageTools.createImageIconFromIconSet("stop_22.png");
        goIcon = ImageTools.createImageIconFromIconSet("go_22.png");
        shortcutIcon = ImageTools.createImageIconFromIconSet("shortcut_22.png");
        initGUI();
    }

    private void initGUI() {
        powerControlPanel = new JPanel(new MigLayout("debug, insets 5, wrap 2"));

        initKeyboardActions();
        update();

        final SimpleInternalFrame frame = new SimpleInternalFrame("Boosters");
        frame.add(powerControlPanel, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(frame, BorderLayout.CENTER);

    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        final boolean connected = event.isConnected();
        final PowerController powerController = ctx.getPowerControl();
        if (connected) {
            powerController.addPowerChangeListener(this);
            allBoostersOn.setEnabled(true);
            allBoostersOff.setEnabled(true);
        } else {
            powerController.removePowerChangeListener(this);
            allBoostersOn.setEnabled(false);
            allBoostersOff.setEnabled(false);
        }
        for (final JToggleButton toggleButton : numberToPowerToggleButtons
                .values()) {
            toggleButton.setEnabled(connected);
        }

    }

    public void powerSupplyChanged(final SRCPPowerSupply powerSupply,
                                   final String freeText) {
        if (freeText == null || freeText.isEmpty()) {
            return;
        }
        if (freeText.toUpperCase().contains("AUTO")) {
            return;
        }
        LOGGER.info("Power freeText: " + freeText);

        final Map<Integer, BoosterState> boosterStates = new HashMap<Integer, BoosterState>();
        final StringTokenizer tokenizer = new StringTokenizer(freeText);
        while (tokenizer.hasMoreTokens()) {
            int boosterNumber = -1;
            if (tokenizer.hasMoreTokens()) {

                final String t = tokenizer.nextToken().trim();

                try {
                    boosterNumber = Integer.parseInt(t);
                } catch (final NumberFormatException x) {
                }

            }
            if (tokenizer.hasMoreTokens()) {

                final String srcpState = tokenizer.nextToken().trim();
                if (BoosterState.isActive(srcpState)) {
                    boosterStates.put(boosterNumber, BoosterState.ACTIVE);
                } else if (BoosterState.isShortcut(srcpState)) {
                    boosterStates.put(boosterNumber, BoosterState.SHORTCUT);
                } else if (BoosterState.isInActive(srcpState)) {
                    boosterStates.put(boosterNumber, BoosterState.INACTIVE);
                }
            }
        }

        int activeBoosterCount = 0;
        for (final Entry<Integer, BoosterState> state : boosterStates
                .entrySet()) {
            final int boosterNumber = state.getKey();
            final BoosterState bs = state.getValue();
            if (boosterNumber != -1) {
                final JToggleButton button = numberToPowerToggleButtons
                        .get(boosterNumber);

                if (button == null) {
                    return;
                }
                button.removeActionListener(numberToActionListener
                        .get(boosterNumber));

                if (bs.equals(BoosterState.ACTIVE)) {
                    button.setSelected(true);
                    button.setIcon(goIcon);
                    activeBoosterCount++;
                } else {
                    button.setSelected(false);
                    if (bs.equals(BoosterState.SHORTCUT)) {
                        button.setIcon(shortcutIcon);
                        activeBoosterCount++;
                    } else {
                        button.setIcon(stopIcon);
                    }
                }
                button.addActionListener(numberToActionListener
                        .get(boosterNumber));
            }
        }
        ctx.setActiveBoosterCount(activeBoosterCount);

    }

    public void update() {

        numberOfBoosters = Preferences.getInstance().getIntValue(
                PreferencesKeys.NUMBER_OF_BOOSTERS);
        powerControlPanel.removeAll();

        allBoostersOn = new JButton("All On", goIcon);
        allBoostersOff = new JButton("All Off", stopIcon);
        allBoostersOff.setFocusable(false);
        allBoostersOn.setFocusable(false);
        allBoostersOn.addActionListener(new AllBoostersOnAction());
        allBoostersOff.addActionListener(new AllBoostersOffAction());

        String params = "height 30, growx";
        if (Preferences.getInstance().getBooleanValue(PreferencesKeys.TABLET_MODE)) {
            params = "height " + UIConstants.SIZE_TABLET + ", growx";
        }
        powerControlPanel.add(allBoostersOn, params);
        powerControlPanel.add(allBoostersOff, params + ", wrap");

        for (int i = 0; i < numberOfBoosters; i++) {

            final JToggleButton boosterButton = new JToggleButton(
                    "F" + (i + 1), stopIcon);
            boosterButton.setHorizontalAlignment(SwingConstants.LEADING);
            final ToggleBoosterAction action = new ToggleBoosterAction();
            boosterButton.addActionListener(action);
            powerControlPanel.add(boosterButton, params);

            numberToPowerToggleButtons.put(i, boosterButton);
            numberToActionListener.put(i, action);
            powerToggleButtonsToNumber.put(boosterButton, i);
            boosterButton.setFocusable(false);
        }
        initKeyboardActions();
    }

    private void initKeyboardActions() {

        final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        final KeyBoardLayout kbl = Preferences.getInstance()
                .getKeyBoardLayout();

        for (int i = 0; i < numberOfBoosters; i++) {
            getActionMap().put("ToggleBooster" + i,
                    new ToggleBoosterKeyAction(i));
            kbl.assignKeys(inputMap, "ToggleBooster" + i);
        }

        ctx.getMainApp().registerEscapeKey(new AllBoostersOffAction());
    }

    private void toogleBooster(final int boosterNumber, final boolean on) {

        final PowerSupply bus1Supply = getPowerSupply();

        if (bus1Supply == null) {
            return;
        }
        final PowerController powerController = ctx.getPowerControl();
        powerController.toggleBooster(bus1Supply.getBooster(boosterNumber));
    }

    private PowerSupply getPowerSupply() {
        final PowerController powerController = ctx.getPowerControl();
        return powerController.getPowerSupply(1);
    }

    @Override
    public void powerChanged(final PowerSupply supply) {
        int activeBoosterCount = 0;
        for (final Booster booster : supply.getBoosters()) {
            final int boosterNumber = booster.getBoosterNumber();
            final BoosterState bs = booster.getState();
            if (boosterNumber != -1) {
                final JToggleButton button = numberToPowerToggleButtons
                        .get(boosterNumber);

                if (button == null) {
                    return;
                }
                button.removeActionListener(numberToActionListener
                        .get(boosterNumber));

                if (bs.equals(BoosterState.ACTIVE)) {
                    button.setSelected(true);
                    button.setIcon(goIcon);
                    activeBoosterCount++;
                } else {
                    button.setSelected(false);
                    if (bs.equals(BoosterState.SHORTCUT)) {
                        button.setIcon(shortcutIcon);
                        activeBoosterCount++;
                    } else {
                        button.setIcon(stopIcon);
                    }
                }
                button.addActionListener(numberToActionListener
                        .get(boosterNumber));
            }
        }
        ctx.setActiveBoosterCount(activeBoosterCount);
    }

    class AllBoostersOnAction extends AbstractAction {

        public AllBoostersOnAction() {
            super();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final PowerController powerController = ctx.getPowerControl();
            final PowerSupply powerSupply = powerController.getPowerSupply(1);
            powerController.powerOn(powerSupply);

        }
    }

    class AllBoostersOffAction extends AbstractAction {

        public AllBoostersOffAction() {
            super();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final PowerController powerController = ctx.getPowerControl();
            final PowerSupply powerSupply = powerController.getPowerSupply(1);
            powerController.powerOff(powerSupply);
        }
    }

    class ToggleBoosterAction extends AbstractAction {

        public ToggleBoosterAction() {
            super();
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final JToggleButton source = (JToggleButton) arg0.getSource();

            final int boosterNumber = powerToggleButtonsToNumber.get(source);

            toogleBooster(boosterNumber, source.isSelected());
        }
    }

    class ToggleBoosterKeyAction extends AbstractAction {

        private final int boosterNumber;

        public ToggleBoosterKeyAction(final int number) {
            super();
            this.boosterNumber = number;
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {

            final JToggleButton source = numberToPowerToggleButtons
                    .get(boosterNumber);
            toogleBooster(boosterNumber, !source.isSelected());
        }
    }
}
