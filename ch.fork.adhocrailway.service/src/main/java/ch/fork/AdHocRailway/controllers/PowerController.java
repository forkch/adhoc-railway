package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.model.power.Booster;
import ch.fork.AdHocRailway.model.power.PowerSupply;

import java.util.HashSet;
import java.util.Set;

public abstract class PowerController {

    protected final Set<PowerChangeListener> listeners = new HashSet<PowerChangeListener>();


    public void addPowerChangeListener(final PowerChangeListener listener) {
        listeners.add(listener);
    }

    public void removePowerChangeListener(final PowerChangeListener listener) {
        listeners.remove(listener);
    }

    public void removeAllPowerChangeListener() {
        listeners.clear();
    }

    protected void informListeners(PowerSupply supply) {
        for (final PowerChangeListener l : listeners) {
            l.powerChanged(supply);
        }
    }


    protected void informListenersAboutReset(String receivedMessage)  {
        for (final PowerChangeListener l : listeners) {
            l.reset(receivedMessage);
        }
    }

    protected void informListenersAboutMessage(String receivedMessage)  {
        for (final PowerChangeListener l : listeners) {
            l.message(receivedMessage);
        }
    }



    public abstract void addOrUpdatePowerSupply(final PowerSupply supply);

    public abstract void boosterOn(final Booster booster);

    public abstract void boosterOff(final Booster booster);

    public abstract void toggleBooster(final Booster booster);

    public abstract void powerOn(final PowerSupply supply);

    public abstract void powerOff(final PowerSupply supply);

    public abstract PowerSupply getPowerSupply(final int busNumber);
}
