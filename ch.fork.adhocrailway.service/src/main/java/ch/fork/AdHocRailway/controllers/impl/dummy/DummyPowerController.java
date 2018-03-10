package ch.fork.AdHocRailway.controllers.impl.dummy;

import ch.fork.AdHocRailway.controllers.PowerChangeListener;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.model.power.Booster;
import ch.fork.AdHocRailway.model.power.BoosterState;
import ch.fork.AdHocRailway.model.power.PowerSupply;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyPowerController extends PowerController {

    private DummyRailwayController dummyRailwayController;
    private PowerSupply powerSupply;

    public DummyPowerController(DummyRailwayController dummyRailwayController) {
        this.dummyRailwayController = dummyRailwayController;
        powerSupply = new PowerSupply(0);
    }

    @Override
    public void addOrUpdatePowerSupply(final PowerSupply supply) {

    }

    @Override
    public void boosterOn(final Booster booster) {
        booster.setState(BoosterState.ACTIVE);
        informListeners(powerSupply);
        dummyRailwayController.informDummyListeners("booster " + booster.getBoosterNumber() + " ON");
    }

    @Override
    public void boosterOff(final Booster booster) {
        booster.setState(BoosterState.INACTIVE);
        informListeners(powerSupply);
        dummyRailwayController.informDummyListeners("booster " + booster.getBoosterNumber() + " OFF");
    }

    @Override
    public void toggleBooster(final Booster booster) {
        booster.setState(booster.getToggeledState());
        informListeners(powerSupply);
        dummyRailwayController.informDummyListeners("toggeled booster " + booster.getBoosterNumber() + " to " + booster.getState());
    }

    @Override
    public void powerOn(final PowerSupply supply) {

        for (Booster booster : supply.getBoosters()) {
            boosterOn(booster);
        }
        informListeners(powerSupply);
        dummyRailwayController.informDummyListeners("power ON");
    }

    @Override
    public void powerOff(final PowerSupply supply) {

        for (Booster booster : supply.getBoosters()) {
            boosterOff(booster);
        }
        informListeners(powerSupply);
        dummyRailwayController.informDummyListeners("power OFF");
    }

    @Override
    public PowerSupply getPowerSupply(final int busNumber) {

        return powerSupply;
    }

}