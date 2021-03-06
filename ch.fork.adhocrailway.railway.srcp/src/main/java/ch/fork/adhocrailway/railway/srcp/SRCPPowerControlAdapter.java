package ch.fork.adhocrailway.railway.srcp;

import ch.fork.adhocrailway.controllers.ControllerException;
import ch.fork.adhocrailway.controllers.PowerController;
import ch.fork.adhocrailway.model.power.Booster;
import ch.fork.adhocrailway.model.power.BoosterState;
import ch.fork.adhocrailway.model.power.PowerSupply;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.power.SRCPPowerControl;
import de.dermoba.srcp.model.power.SRCPPowerState;
import de.dermoba.srcp.model.power.SRCPPowerSupply;
import de.dermoba.srcp.model.power.SRCPPowerSupplyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class SRCPPowerControlAdapter extends PowerController implements
        SRCPPowerSupplyChangeListener {
    private final Logger LOGGER = LoggerFactory.getLogger(SRCPPowerControlAdapter.class);

    private final Map<PowerSupply, SRCPPowerSupply> powerSupplyToSRCPPowerSupply = new HashMap<>();
    private final Map<SRCPPowerSupply, PowerSupply> srcpPowerSupplyToPowerSupply = new HashMap<>();
    private final SRCPPowerControl powerControl;
    private final Map<Integer, PowerSupply> busToPowerSupply = new HashMap<>();

    public SRCPPowerControlAdapter() {
        powerControl = SRCPPowerControl.getInstance();
        powerControl.removeAllPowerSupplyChangeListener();
    }

    @Override
    public PowerSupply getPowerSupply(final int busNumber) {
        return busToPowerSupply.get(busNumber);
    }

    @Override
    public void addOrUpdatePowerSupply(final PowerSupply supply) {
        final SRCPPowerSupply srcpPowerSupply = createSRCPPowerSupply(supply);

        powerSupplyToSRCPPowerSupply.put(supply, srcpPowerSupply);
        srcpPowerSupplyToPowerSupply.put(srcpPowerSupply, supply);
        busToPowerSupply.put(supply.getBus(), supply);
    }

    @Override
    public void boosterOn(final Booster booster) {
        final SRCPPowerSupply srcpSupply = getSRCPPowerSupply(booster
                .getSupply());

        try {
            powerControl.setState(srcpSupply, SRCPPowerState.ON,
                    "" + booster.getBoosterNumber());
        } catch (final SRCPModelException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    @Override
    public void boosterOff(final Booster booster) {
        final SRCPPowerSupply srcpSupply = getSRCPPowerSupply(booster
                .getSupply());

        try {
            powerControl.setState(srcpSupply, SRCPPowerState.OFF,
                    "" + booster.getBoosterNumber());
        } catch (final SRCPModelException e) {
            throw new ControllerException(e.getMessage());
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
                boosterOn(booster);
                break;
            default:
                break;

        }
    }

    @Override
    public void powerOn(final PowerSupply supply) {
        try {
            powerControl.setAllStates(SRCPPowerState.ON);
        } catch (final SRCPModelException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    @Override
    public void powerOff(final PowerSupply supply) {
        try {
            powerControl.setAllStates(SRCPPowerState.OFF);
        } catch (final SRCPModelException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    public void setSession(final SRCPSession session) {
        powerControl.addPowerSupplyChangeListener(this);
        powerControl.setSession(session);
    }

    @Override
    public void powerSupplyChanged(final SRCPPowerSupply srcpPowerSupply,
                                   final String freeText) {
        if (freeText == null || freeText.isEmpty()) {
            return;
        }
        if (freeText.toUpperCase().contains("AUTO")) {
            return;
        }

        LOGGER.info("received new powerstate: " + freeText);

        final Map<Integer, BoosterState> boosterStates = new HashMap<>();
        if (freeText.contains("XRS")) {
            LOGGER.warn(String.format("BRAIN RESET: %s", freeText));
            informListenersAboutMessage(freeText.replace("XRS ", ""));
            informListenersAboutReset(freeText);

        } else if (freeText.contains("XMSG")) {
            LOGGER.info(String.format("BRAIN MESSAGE: %s", freeText));
            informListenersAboutMessage(freeText.replace("XMSG ", ""));
        } else {

            final StringTokenizer tokenizer = new StringTokenizer(freeText);
            while (tokenizer.hasMoreTokens()) {
                int boosterNumber = -1;
                if (tokenizer.hasMoreTokens()) {
                    final String t = tokenizer.nextToken().trim();
                    try {
                        boosterNumber = Integer.parseInt(t);
                    } catch (final NumberFormatException x) {
                        LOGGER.error("could not parse booster number", x);
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

            final PowerSupply supply = srcpPowerSupplyToPowerSupply
                    .get(srcpPowerSupply);

            for (final Entry<Integer, BoosterState> boosterState : boosterStates
                    .entrySet()) {
                final Booster booster = supply.getBooster(boosterState.getKey());
                booster.setState(boosterState.getValue());
            }

//        LOGGER.info("new booster state: " + boosterStates);
//        LOGGER.info("all booster states: " + supply.getBoosters());
            informListeners(supply);
        }

    }


    private SRCPPowerSupply createSRCPPowerSupply(final PowerSupply supply) {

        final SRCPPowerSupply srcpSupply = new SRCPPowerSupply(supply.getBus());
        srcpSupply.setState(SRCPPowerState.OFF);
        return srcpSupply;
    }

    private SRCPPowerSupply getSRCPPowerSupply(final PowerSupply supply) {
        return powerSupplyToSRCPPowerSupply.get(supply);
    }

}
