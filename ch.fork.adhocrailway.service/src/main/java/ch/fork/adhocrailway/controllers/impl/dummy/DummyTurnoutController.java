package ch.fork.adhocrailway.controllers.impl.dummy;

import ch.fork.adhocrailway.controllers.TurnoutController;
import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutState;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyTurnoutController extends TurnoutController {

    private DummyRailwayController dummyRailwayController;

    public DummyTurnoutController(DummyRailwayController dummyRailwayController) {
        super(null);
        this.dummyRailwayController = dummyRailwayController;
    }


    @Override
    public void toggle(final Turnout turnout) {
        turnout.setActualState(turnout.getToggledState());
        informListeners(turnout);
        dummyRailwayController.informDummyListeners("toggled turnout " + turnout.getNumber() + " to " + turnout.getActualState());
    }


    @Override
    public void toggleTest(final Turnout turnout) {
        turnout.setActualState(turnout.getToggledState());
        informListeners(turnout);
        dummyRailwayController.informDummyListeners("test toggeled turnout " + turnout.getNumber() + " to " + turnout.getActualState());
    }

    @Override
    public void setDefaultState(final Turnout turnout) {
        turnout.setActualState(turnout.getDefaultState());
        informListeners(turnout);
        dummyRailwayController.informDummyListeners("set turnout " + turnout.getNumber() + " to DEFAULT state");
    }

    @Override
    public void setStraight(final Turnout turnout) {
        turnout.setActualState(TurnoutState.STRAIGHT);
        informListeners(turnout);
        dummyRailwayController.informDummyListeners("set turnout " + turnout.getNumber() + " to STRAIGHT");
    }

    @Override
    public void setCurvedLeft(final Turnout turnout) {
        turnout.setActualState(TurnoutState.LEFT);
        informListeners(turnout);
        dummyRailwayController.informDummyListeners("set turnout " + turnout.getNumber() + " to LEFT");
    }

    @Override
    public void setCurvedRight(final Turnout turnout) {
        turnout.setActualState(TurnoutState.RIGHT);
        informListeners(turnout);
        dummyRailwayController.informDummyListeners("set turnout " + turnout.getNumber() + " to RIGHT");
    }

    @Override
    public void setTurnoutWithAddress(final int address,
                                      final TurnoutState state) {
        dummyRailwayController.informDummyListeners("set address " + address + " to " + state.toString());
    }

    @Override
    public void reloadConfiguration() {
    }

    @Override
    public TurnoutState getStateFromDevice(Turnout turnout) {
        return TurnoutState.UNDEF;
    }

    @Override
    public void setCutterSleepTime(int cutterSleepTimeMillis) {
        dummyRailwayController.informDummyListeners("set cutter sleep time to " + cutterSleepTimeMillis);
    }

}
