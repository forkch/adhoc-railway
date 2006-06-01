package ch.fork.RailControl.domain.locomotives;

import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;

public class NoneLocomotive extends Locomotive {

    private static final int DRIVING_STEPS = 0;

    private static final int FUNCTIONCOUNT = 0;

    public NoneLocomotive() {
        super(null, "NONE", 0, 0, DRIVING_STEPS, "Dummy", FUNCTIONCOUNT);
    }

    @Override
    public void decreaseSpeed() throws LocomotiveException {

    }

    @Override
    public int getCurrentSpeed() {
        return 0;
    }

    @Override
    public void increaseSpeed() throws LocomotiveException {

    }

    @Override
    public void init() throws LocomotiveException {

    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void setInitialized(boolean initialized) {

    }

    @Override
    public void setSpeed(int speed) throws LocomotiveException {

    }

    @Override
    protected void increaseSpeedStep() throws LocomotiveException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void decreaseSpeedStep() throws LocomotiveException {
        // TODO Auto-generated method stub

    }

    @Override
    public Locomotive clone() {
        return this;
    }

}
