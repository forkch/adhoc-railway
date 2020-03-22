package ch.fork.adhocrailway.controllers;

import ch.fork.adhocrailway.model.power.PowerSupply;

public interface PowerChangeListener {

    public void powerChanged(PowerSupply supply);

    void reset(String resetMessage);

    void message(String receivedMessage);
}
