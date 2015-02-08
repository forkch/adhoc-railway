package ch.fork.AdHocRailway.controllers.impl.dummy;

import ch.fork.AdHocRailway.controllers.TaskExecutor;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyTurnoutController extends TurnoutController {

    public DummyTurnoutController() {
        super(null);
    }

    @Override
    public void toggle(final Turnout turnout) {
        turnout.setActualState(turnout.getToggledState());
        informListeners(turnout);
    }

    @Override
    public void toggleTest(final Turnout turnout) {
        turnout.setActualState(turnout.getToggledState());
        informListeners(turnout);
    }

    @Override
    public void setDefaultState(final Turnout turnout) {
        turnout.setActualState(turnout.getDefaultState());
        informListeners(turnout);
    }

    @Override
    public void setStraight(final Turnout turnout) {
        turnout.setActualState(TurnoutState.STRAIGHT);
        informListeners(turnout);
    }

    @Override
    public void setCurvedLeft(final Turnout turnout) {
        turnout.setActualState(TurnoutState.LEFT);
        informListeners(turnout);
    }

    @Override
    public void setCurvedRight(final Turnout turnout) {
        turnout.setActualState(TurnoutState.RIGHT);
        informListeners(turnout);
    }

    @Override
    public void setTurnoutWithAddress(final int address,
                                      final TurnoutState state) {
    }

    @Override
    public void reloadConfiguration() {
    }

    @Override
    public TurnoutState getStateFromDevice(Turnout turnout) {
        return TurnoutState.UNDEF;
    }

    @Override
    public void setCutterSleepTime(int intValue) {

    }

}