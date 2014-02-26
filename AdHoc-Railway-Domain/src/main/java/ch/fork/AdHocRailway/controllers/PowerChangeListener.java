package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.domain.power.PowerSupply;

public interface PowerChangeListener {

    public void powerChanged(PowerSupply supply);

}
