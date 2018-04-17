package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.model.power.PowerSupply;

public interface PowerChangeListener {

    public void powerChanged(PowerSupply supply);

    void reset(String resetMessage);
}
