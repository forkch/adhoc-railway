package ch.fork.AdHocRailway.controllers.impl.dummy;

import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.domain.power.Booster;
import ch.fork.AdHocRailway.domain.power.PowerSupply;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyPowerController extends PowerController {

    @Override
    public void addOrUpdatePowerSupply(final PowerSupply supply) {

    }

    @Override
    public void boosterOn(final Booster booster) {

    }

    @Override
    public void boosterOff(final Booster booster) {

    }

    @Override
    public void toggleBooster(final Booster booster) {

    }

    @Override
    public void powerOn(final PowerSupply supply) {

    }

    @Override
    public void powerOff(final PowerSupply supply) {

    }

    @Override
    public PowerSupply getPowerSupply(final int busNumber) {
        return null;
    }

}