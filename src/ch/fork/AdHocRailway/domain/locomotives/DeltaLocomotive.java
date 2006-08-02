
package ch.fork.AdHocRailway.domain.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;

public class DeltaLocomotive extends Locomotive {
    private static final int DRIVING_STEPS = 14;
    private static final int STEPPING      = 2;
    private static final int FUNCTIONCOUNT = 1;

    public DeltaLocomotive(String name, int bus, int address, String desc) {
        super(name, bus, address, DRIVING_STEPS, desc, FUNCTIONCOUNT);
    }

    protected void increaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed() + STEPPING);
    }

    protected void decreaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed() - STEPPING);
    }

    @Override
    public Locomotive clone() {
        DeltaLocomotive clone = new DeltaLocomotive(name, bus, address, desc);
        clone.functions = functions;
        clone.initialized = initialized;
        clone.currentSpeed = currentSpeed;
        clone.direction = direction;
        clone.params = params;
        return clone;
    }
}
