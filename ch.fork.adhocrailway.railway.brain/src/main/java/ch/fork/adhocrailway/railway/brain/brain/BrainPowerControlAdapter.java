package ch.fork.adhocrailway.railway.brain.brain;

import ch.fork.adhocrailway.controllers.ControllerException;
import ch.fork.adhocrailway.controllers.PowerController;
import ch.fork.adhocrailway.model.power.Booster;
import ch.fork.adhocrailway.model.power.BoosterState;
import ch.fork.adhocrailway.model.power.PowerSupply;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BrainPowerControlAdapter extends PowerController implements
        BrainListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrainPowerControlAdapter.class);
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
            throw new ControllerException("error turning on booster "
                    + booster.getBoosterNumber(), e);
        }
    }

    @Override
    public void boosterOff(final Booster booster) {
        try {
            brain.write("XSTOP " + booster.getBoosterNumber());
        } catch (final BrainException e) {
            throw new ControllerException("error turning on booster "
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
            throw new ControllerException("error turning on power ", e);
        }
    }

    @Override
    public void powerOff(final PowerSupply supply) {
        try {
            brain.write("X.");
        } catch (final BrainException e) {
            throw new ControllerException("error turning off power ", e);
        }
    }

    @Override
    public PowerSupply getPowerSupply(final int busNumber) {
        return supplies.get(busNumber);
    }

    @Override
    public void sentMessage(String sentMessage) {

    }

    @Override
    public void receivedMessage(final String receivedMessage) {
        LOGGER.info("received power message from brain: " + receivedMessage);

        if (StringUtils.startsWith(receivedMessage, "XBS")) {
            processBoosterMessage(receivedMessage);
        }
    }

    @Override
    public void brainReset(String resetMessage) {
        informListenersAboutReset(resetMessage);
    }

    @Override
    public void brainMessage(String message) {
        informListenersAboutMessage(message);

    }

    private void processBoosterMessage(String receivedMessage) {
        final Scanner scanner = new Scanner(receivedMessage);
        scanner.useDelimiter(" ");

        final String command = scanner.next();

        final PowerSupply supply = supplies.get(1);
        if (!scanner.hasNext()) {
            LOGGER.warn("received an invalid XBS command from the brain: " + receivedMessage);
            scanner.close();
            return;
        }

        for (int i = 0; i < 8; i++) {
            if (!scanner.hasNext()) {
                LOGGER.warn("received an invalid XBS command from the brain: " + receivedMessage);
                scanner.close();
                return;
            }
            final String boosterState = scanner.next();
            if (StringUtils.equalsIgnoreCase("A", boosterState)) {
                supply.getBooster(i).setState(BoosterState.ACTIVE);
            } else if (StringUtils.equalsIgnoreCase("O", boosterState)) {
                supply.getBooster(i).setState(BoosterState.INACTIVE);
            } else if (StringUtils.equalsIgnoreCase("S", boosterState)) {
                supply.getBooster(i).setState(BoosterState.SHORTCUT);
            }
        }

        informListeners(supply);
        scanner.close();
        return;

    }


}
