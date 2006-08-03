
package ch.fork.AdHocRailway.domain.locomotives;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;

public class DigitalLocomotive extends Locomotive {
    private static final int DRIVING_STEPS = 28;
    private static final int STEPPING      = 4;
    private static final int FUNCTIONCOUNT = 5;

    public DigitalLocomotive(String name, Address address, String desc) {
        super(name, address, DRIVING_STEPS, desc, FUNCTIONCOUNT);
    }

    protected void increaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed() + STEPPING);
    }

    protected void decreaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed() - STEPPING);
    }

    @Override
    public Locomotive clone() {
        DigitalLocomotive clone = new DigitalLocomotive(name, address, desc);
        clone.functions = functions;
        clone.initialized = initialized;
        clone.currentSpeed = currentSpeed;
        clone.direction = direction;
        clone.params = params;
        return clone;
    }
}
