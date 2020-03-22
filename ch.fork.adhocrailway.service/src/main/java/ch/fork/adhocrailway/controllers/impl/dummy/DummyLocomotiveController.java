package ch.fork.adhocrailway.controllers.impl.dummy;

import ch.fork.adhocrailway.controllers.LocomotiveController;
import ch.fork.adhocrailway.model.locomotives.Locomotive;

import java.util.Arrays;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyLocomotiveController extends LocomotiveController {

    private DummyRailwayController dummyRailwayController;

    public DummyLocomotiveController(DummyRailwayController dummyRailwayController) {
        super(null);
        this.dummyRailwayController = dummyRailwayController;
    }

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
        dummyRailwayController.informDummyListeners("toggeled direction of locomotive " + locomotive.getName() + " to " + locomotive.getCurrentDirection());
    }

    @Override
    public void setSpeed(final Locomotive locomotive, final int speed,
                         final boolean[] functions) {
        locomotive.setCurrentSpeed(speed);
        locomotive.setCurrentFunctions(functions);
        informListeners(locomotive);
        locomotive.setTargetSpeed(-1);

        dummyRailwayController.informDummyListeners("set speed of locomotive " + locomotive.getName() + " to " + speed);
    }

    @Override
    public void terminateAllLocomotives() {

    }

    @Override
    public void terminateLocomotive(Locomotive locomotive) {

    }

    @Override
    public void setFunction(final Locomotive locomotive,
                            final int functionNumber, final boolean state,
                            final int deactivationDelay) {
        boolean[] currentFunctions = locomotive.getCurrentFunctions();
        currentFunctions[functionNumber] = state;
        locomotive.setCurrentFunctions(currentFunctions);
        informListeners(locomotive);
        dummyRailwayController.informDummyListeners("set function " + functionNumber + " of locomotive " + locomotive.getName() + " to " + (state ? "ON" : "OFF") + " (" + Arrays.toString(locomotive.getCurrentFunctions()) + ")");
    }

    @Override
    public void emergencyStop(final Locomotive locomotive) {
        locomotive.setTargetSpeed(-1);
        setFunction(locomotive, locomotive.getEmergencyStopFunctionNumber(), true, 0);
        setSpeed(locomotive, 0, locomotive.getCurrentFunctions());
        locomotive.setTargetSpeed(-1);
        informListeners(locomotive);
        dummyRailwayController.informDummyListeners("emergeny stop of locomotive " + locomotive.getName());
    }
}
