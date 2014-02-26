package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.PowerChangeListener;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.PowerException;
import ch.fork.AdHocRailway.domain.power.Booster;
import ch.fork.AdHocRailway.domain.power.BoosterState;
import ch.fork.AdHocRailway.domain.power.PowerSupply;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BrainPowerControlAdapter extends PowerController implements
        BrainListener {
    private static final Logger LOGGER = Logger
            .getLogger(BrainPowerControlAdapter.class);
    private final Map<Integer, PowerSupply> supplies = new HashMap<Integer, PowerSupply>();

    private final BrainController brain;

    public BrainPowerControlAdapter(final BrainController brain) {
        this.brain = brain;
        brain.addBrainListener(this);
    }

    @Override
    public void addOrUpdatePowerSupply(final PowerSupply supply) {
        supplies.put(supply.getBus(), supply);
    }

    @Override
    public void boosterOn(final Booster booster) {
        try {
            brain.write("XGO " + booster.getBoosterNumber());
        } catch (final BrainException e) {
            throw new PowerException("error turning on booster "
                    + booster.getBoosterNumber(), e);
        }
    }

    @Override
    public void boosterOff(final Booster booster) {
        try {
            brain.write("XSTOP " + booster.getBoosterNumber());
        } catch (final BrainException e) {
            throw new PowerException("error turning on booster "
                    + booster.getBoosterNumber(), e);
        }
    }

    @Override
    public void toggleBooster(final Booster booster) {
        switch (booster.getState()) {
            case ACTIVE:
                boosterOff(booster);
                break;
            case INACTIVE:
            case SHORTCUT:
            default:
                boosterOn(booster);
                break;

        }
    }

    @Override
    public void powerOn(final PowerSupply supply) {
        try {
            brain.write("X!");
        } catch (final BrainException e) {
            throw new PowerException("error turning on power ", e);
        }
    }

    @Override
    public void powerOff(final PowerSupply supply) {
        try {
            brain.write("X.");
        } catch (final BrainException e) {
            throw new PowerException("error turning off power ", e);
        }
    }

    @Override
    public PowerSupply getPowerSupply(final int busNumber) {
        return supplies.get(busNumber);
    }

    @Override
    public void receivedMessage(final String receivedString) {
        LOGGER.info("received power message from brain: " + receivedString);

        final String receivedStringXBS = StringUtils.substring(receivedString,
                StringUtils.indexOf(receivedString, "XBS"));
        if (!StringUtils.startsWithIgnoreCase(receivedStringXBS, "XBS")) {
            return;
        }
        final Scanner scanner = new Scanner(receivedStringXBS);
        scanner.useDelimiter(" ");
        final String xbs = scanner.next();
        if (!StringUtils.equalsIgnoreCase("XBS", xbs)) {
            scanner.close();
            return;
        }

        final PowerSupply supply = supplies.get(1);

        for (int i = 0; i < 8; i++) {
            final String boosterState = scanner.next();
            if (StringUtils.equalsIgnoreCase("A", boosterState)) {
                supply.getBooster(i).setState(BoosterState.ACTIVE);
            } else if (StringUtils.equalsIgnoreCase("O", boosterState)) {
                supply.getBooster(i).setState(BoosterState.INACTIVE);
            } else if (StringUtils.equalsIgnoreCase("S", boosterState)) {
                supply.getBooster(i).setState(BoosterState.SHORTCUT);
            }
        }

        for (final PowerChangeListener l : listeners) {
            l.powerChanged(supply);
        }
        scanner.close();
    }

}
