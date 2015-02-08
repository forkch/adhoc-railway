package ch.fork.AdHocRailway.railway.brain.brain;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.TaskExecutor;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;

public class BrainTurnoutControlAdapter extends TurnoutController {

    private final BrainController brain;
    private long cutterSleepTime = 500;

    public BrainTurnoutControlAdapter(TaskExecutor taskExecutor, final BrainController brain) {
        super(taskExecutor);
        this.brain = brain;
    }

    @Override
    public void toggle(final Turnout turnout) {


        switch (turnout.getActualState()) {
            case LEFT:
                if (turnout.isThreeWay()) {
                    setCurvedRight(turnout);
                } else {
                    setStraight(turnout);
                }
                break;
            case RIGHT:
                setStraight(turnout);
                break;
            case STRAIGHT:
                setCurvedLeft(turnout);
                break;
            case UNDEF:
                setStraight(turnout);
                break;
            default:
                break;
        }

    }


    @Override
    public void setDefaultState(final Turnout turnout) {

        final TurnoutState defaultState = turnout.getDefaultState();
        switch (defaultState) {
            case LEFT:
                setCurvedLeft(turnout);
                break;
            case RIGHT:
                setCurvedRight(turnout);
                break;
            case STRAIGHT:
                setStraight(turnout);
                break;
            case UNDEF:
                throw new IllegalArgumentException(
                        "UNDEF is not a valid default state");
        }
    }

    @Override
    public void setStraight(final Turnout turnout) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                setStraightSync(turnout);
            }
        });
    }

    public void setStraightSync(Turnout turnout) {
        try {
            if (turnout.isThreeWay()) {
                brain.write("XT " + turnout.getAddress1() + " "
                        + getGreenPort(turnout.isAddress1Switched()));
                brain.write("XT " + turnout.getAddress2() + " "
                        + getGreenPort(turnout.isAddress2Switched()));
            } else {
                int reps = 1;
                if (turnout.isCutter()) {
                    reps = 5;
                }
                for (int i = 0; i < reps; i++) {
                    brain.write("XT " + turnout.getAddress1() + " "
                            + getGreenPort(turnout.isAddress1Switched()));
                    if (reps > 1) {
                        try {
                            Thread.sleep(cutterSleepTime);
                        } catch (InterruptedException e) {
                            throw new IllegalStateException("failed to sleep");
                        }
                    }
                }


            }
            if (turnout.isCutter()) {
                turnout.setActualState(TurnoutState.LEFT);
            } else {
                turnout.setActualState(TurnoutState.STRAIGHT);
            }
            informListeners(turnout);
        } catch (final BrainException e) {
            throw new ControllerException("failed to set turnout straight", e);
        }
    }

    @Override
    public void setCurvedLeft(final Turnout turnout) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                setCurvedLeftSync(turnout);
            }
        });
    }

    public void setCurvedLeftSync(Turnout turnout) {
        try {
            if (turnout.isThreeWay()) {
                brain.write("XT " + turnout.getAddress1() + " "
                        + getRedPort(turnout.isAddress1Switched()));
                brain.write("XT " + turnout.getAddress2() + " "
                        + getGreenPort(turnout.isAddress2Switched()));
            } else {
                brain.write("XT " + turnout.getAddress1() + " "
                        + getRedPort(turnout.isAddress1Switched()));
            }
            turnout.setActualState(TurnoutState.LEFT);
            informListeners(turnout);
        } catch (final BrainException e) {
            throw new ControllerException("failed to set turnout curved left", e);
        }
    }

    @Override
    public void setCurvedRight(final Turnout turnout) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                setCurvedRightSync(turnout);
            }

        });
    }

    public void setCurvedRightSync(Turnout turnout) {
        try {
            if (turnout.isThreeWay()) {
                brain.write("XT " + turnout.getAddress1() + " "
                        + getGreenPort(turnout.isAddress1Switched()));
                brain.write("XT " + turnout.getAddress2() + " "
                        + getRedPort(turnout.isAddress2Switched()));
            } else {
                brain.write("XT " + turnout.getAddress1() + " "
                        + getRedPort(turnout.isAddress1Switched()));
            }
            turnout.setActualState(TurnoutState.RIGHT);
            informListeners(turnout);
        } catch (final BrainException e) {
            throw new ControllerException("failed to set turnout curved right", e);
        }
    }

    @Override
    public void toggleTest(final Turnout turnout) {
        toggle(turnout);
    }

    @Override
    public void setTurnoutWithAddress(final int address,
                                      final TurnoutState straight) {
        if (TurnoutState.STRAIGHT == straight) {
            brain.write("XT " + address + " " + "g");
        } else {
            brain.write("XT " + address + " " + "r");

        }
    }

    @Override
    public void reloadConfiguration() {

    }

    @Override
    public TurnoutState getStateFromDevice(Turnout turnout) {
        return TurnoutState.UNDEF;
    }

    @Override
    public void setCutterSleepTime(int cutterSleepTime) {
        this.cutterSleepTime = cutterSleepTime;
    }

    private String getGreenPort(final boolean inverted) {
        return inverted ? "r" : "g";
    }

    private String getRedPort(final boolean inverted) {
        return getGreenPort(!inverted);
    }

    public void setCutterSleepTime(long cutterSleepTime) {
        this.cutterSleepTime = cutterSleepTime;
    }

    public long getCutterSleepTime() {
        return cutterSleepTime;
    }
}
