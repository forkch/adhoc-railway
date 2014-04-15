package ch.fork.AdHocRailway.controllers.impl.dummy;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyLocomotiveController extends LocomotiveController {

    @Override
    public boolean isLocked(final Locomotive object) {
        return false;
    }

    @Override
    public boolean isLockedByMe(final Locomotive object) {
        return true;
    }

    @Override
    public boolean acquireLock(final Locomotive object) {
        return true;
    }

    @Override
    public boolean releaseLock(final Locomotive object) {
        return true;
    }

    @Override
    public void toggleDirection(final Locomotive locomotive) {
        locomotive.setCurrentDirection(locomotive.getToggledDirection());
        informListeners(locomotive);
    }

    @Override
    public void setSpeed(final Locomotive locomotive, final int speed,
                         final boolean[] functions) {
        locomotive.setCurrentSpeed(speed);
        locomotive.setCurrentFunctions(functions);
        informListeners(locomotive);

    }

    @Override
    public void setFunction(final Locomotive locomotive,
                            final int functionNumber, final boolean state,
                            final int deactivationDelay) {
        boolean[] currentFunctions = locomotive.getCurrentFunctions();
        currentFunctions[functionNumber] = state;
        locomotive.setCurrentFunctions(currentFunctions);
        informListeners(locomotive);
    }

    @Override
    public void emergencyStop(final Locomotive locomotive) {
        setFunction(locomotive, locomotive.getEmergencyStopFunction(), true, 0);
        setSpeed(locomotive, 0, locomotive.getCurrentFunctions());
        informListeners(locomotive);
    }
}
