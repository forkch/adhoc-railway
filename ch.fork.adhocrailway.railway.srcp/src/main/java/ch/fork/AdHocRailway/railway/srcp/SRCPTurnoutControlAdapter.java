package ch.fork.AdHocRailway.railway.srcp;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.TaskExecutor;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.model.turnouts.TurnoutType;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.turnouts.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.dermoba.srcp.model.turnouts.SRCPTurnoutState.*;

public class SRCPTurnoutControlAdapter extends TurnoutController implements
        SRCPTurnoutChangeListener {
    private static final Logger LOGGER = Logger
            .getLogger(SRCPTurnoutControlAdapter.class);

    private final Map<Turnout, SRCPTurnout> turnoutsSRCPTurnoutsMap = new HashMap<Turnout, SRCPTurnout>();
    private final Map<SRCPTurnout, Turnout> SRCPTurnoutsTurnoutsMap = new HashMap<SRCPTurnout, Turnout>();

    SRCPTurnoutControl turnoutControl;

    SRCPTurnout sTurnoutTemp;

    Turnout turnoutTemp;

    public SRCPTurnoutControlAdapter(TaskExecutor taskExecutor) {
        super(taskExecutor);

        turnoutControl = SRCPTurnoutControl.getInstance();
        turnoutControl.removeAllTurnoutChangeListener();

        reloadConfiguration();
    }

    @Override
    public void setCurvedLeft(final Turnout turnout) {
        final SRCPTurnout sTurnout = getOrCreateSRCPTurnout(turnout);
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                try {
                    turnoutControl.setCurvedLeft(sTurnout);
                } catch (final SRCPModelException e) {
                    throw new ControllerException("could not set turnout to curved left",
                            e);
                }
            }
        });

    }

    @Override
    public void setCurvedRight(final Turnout turnout) {
        final SRCPTurnout sTurnout = getOrCreateSRCPTurnout(turnout);
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                try {
                    turnoutControl.setCurvedRight(sTurnout);
                } catch (final SRCPModelException e) {
                    throw new ControllerException("could not set turnout to curved right",
                            e);
                }
            }
        });

    }

    @Override
    public void setDefaultState(final Turnout turnout) {
        final SRCPTurnout sTurnout = getOrCreateSRCPTurnout(turnout);
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                try {

                    turnoutControl.setDefaultState(sTurnout);
                } catch (final SRCPModelException e) {
                    throw new ControllerException(
                            "could not set turnout to default state", e);
                }
            }
        });

    }

    @Override
    public void setStraight(final Turnout turnout) {
        final SRCPTurnout sTurnout = getOrCreateSRCPTurnout(turnout);
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                try {
                    turnoutControl.setStraight(sTurnout);
                } catch (final SRCPModelException e) {
                    throw new ControllerException("could not set turnout to straight", e);
                }
            }
        });
    }

    @Override
    public void toggleTest(final Turnout turnout) {

        turnoutControl.removeTurnout(sTurnoutTemp);
        if (turnoutTemp == null || !turnoutTemp.equals(turnout)) {
            if (sTurnoutTemp != null) {
                turnoutControl.removeTurnout(sTurnoutTemp);
            }
            turnoutTemp = turnout;
            // just create a temporary SRCPTurnout
            sTurnoutTemp = new MMTurnout();
        } else {
            applyNewSettings(sTurnoutTemp, turnout);
            sTurnoutTemp.setInitialized(false);
        }
        turnoutControl.addTurnout(sTurnoutTemp);
        try {
            turnoutControl.toggle(sTurnoutTemp);
        } catch (final SRCPModelException e) {
            throw new ControllerException("could not toggle turnout", e);
        }
    }

    @Override
    public void toggle(final Turnout turnout) {
        final SRCPTurnout sTurnout = getOrCreateSRCPTurnout(turnout);
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                try {
                    turnoutControl.toggle(sTurnout);
                } catch (final SRCPModelException e) {
                    throw new ControllerException("could not toggle turnout", e);
                }
            }
        });

    }

    @Override
    public void reloadConfiguration() {
    }

    @Override
    public TurnoutState getStateFromDevice(Turnout turnout) {
        final SRCPTurnout sTurnout = getOrCreateSRCPTurnout(turnout);
        SRCPTurnoutState srcpTurnoutState = turnoutControl.getTurnoutState(sTurnout);
        return getTurnoutStateFromSRCPTurnoutState(srcpTurnoutState);

    }

    @Override
    public void setCutterSleepTime(int cutterSleepTime) {
        turnoutControl.setCutterSleepTime(cutterSleepTime);
    }

    @Override
    public void turnoutChanged(final SRCPTurnout changedTurnout,
                               final SRCPTurnoutState newState) {
        informListeners(changedTurnout);
    }

    public void setSession(final SRCPSession session) {
        turnoutControl.addTurnoutChangeListener(this);
        turnoutControl.setSession(session);
    }

    SRCPTurnout getOrCreateSRCPTurnout(final Turnout turnout) {
        if (turnout == null) {
            throw new IllegalArgumentException("turnout must not be null");
        }
        SRCPTurnout srcpTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
        if (srcpTurnout == null) {
            srcpTurnout = new MMTurnout();
        }
        applyNewSettings(srcpTurnout, turnout);
        updateCaches(turnout, srcpTurnout);

        return srcpTurnout;
    }

    private void updateCaches(Turnout turnout, SRCPTurnout srcpTurnout) {
        turnoutsSRCPTurnoutsMap.put(turnout, srcpTurnout);
        SRCPTurnoutsTurnoutsMap.put(srcpTurnout, turnout);
        turnoutControl.addTurnout(srcpTurnout);
    }


    private void applyNewSettings(SRCPTurnout sTurnout, final Turnout turnout) {
        sTurnout.setBus1(turnout.getBus1());
        sTurnout.setBus2(turnout.getBus2());
        sTurnout.setAddress1(turnout.getAddress1());
        sTurnout.setAddress2(turnout.getAddress2());
        sTurnout.setAddress1Switched(turnout.isAddress1Switched());
        sTurnout.setAddress2Switched(turnout.isAddress2Switched());
        setSRCPTurnoutDefaultState(sTurnout, turnout);
        setSRCPTurnoutType(turnout, sTurnout);
    }

    private void setSRCPTurnoutType(final Turnout turnout,
                                    final SRCPTurnout sTurnout) {
        switch (turnout.getType()) {
            case DEFAULT_LEFT:
            case DEFAULT_RIGHT:
                sTurnout.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
                break;
            case DOUBLECROSS:
                sTurnout.setTurnoutType(SRCPTurnoutTypes.DOUBLECROSS);
                break;
            case CUTTER:
                sTurnout.setTurnoutType(SRCPTurnoutTypes.CUTTER);
                break;
            case THREEWAY:
                sTurnout.setTurnoutType(SRCPTurnoutTypes.THREEWAY);
                break;
            default:
                sTurnout.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
        }
    }

    private void setSRCPTurnoutDefaultState(final SRCPTurnout sTurnout,
                                            final Turnout turnout) {
        switch (turnout.getDefaultState()) {
            case STRAIGHT:
                sTurnout.setDefaultState(STRAIGHT);
                break;
            case LEFT:
                sTurnout.setDefaultState(LEFT);
                break;
            case RIGHT:
                sTurnout.setDefaultState(RIGHT);
                break;
            case UNDEF:
                sTurnout.setDefaultState(UNDEF);
                break;
        }
    }

    private void informListeners(final SRCPTurnout changedTurnout) {
        Turnout turnout = SRCPTurnoutsTurnoutsMap.get(changedTurnout);
        if (turnout == null) {
            turnout = turnoutTemp;
        }
        if (turnout == null) {
            return;
        }

        turnout.setActualState(getTurnoutStateFromSRCPTurnoutState(changedTurnout
                .getTurnoutState()));
        informListeners(turnout);
        LOGGER.debug("turnoutChanged(" + changedTurnout + ")");

    }

    private TurnoutState getTurnoutStateFromSRCPTurnoutState(
            final SRCPTurnoutState turnoutState) {
        switch (turnoutState) {
            case LEFT:
                return TurnoutState.LEFT;
            case RIGHT:
                return TurnoutState.RIGHT;
            case STRAIGHT:

                return TurnoutState.STRAIGHT;
            case UNDEF:
            default:
                return TurnoutState.UNDEF;
        }
    }

    @Override
    public void setTurnoutWithAddress(final int address,
                                      final TurnoutState state) {
        try {
            turnoutControl.setTurnoutWithAddress(address,
                    getSRCPTurnoutStateFromTurnoutState(state));
        } catch (final SRCPException e) {
            throw new ControllerException("failed to set turnout with address "
                    + address + " " + state);
        }

    }

    private SRCPTurnoutState getSRCPTurnoutStateFromTurnoutState(
            final TurnoutState state) {
        switch (state) {
            case LEFT:
                return LEFT;
            case RIGHT:
                return RIGHT;
            case STRAIGHT:

                return STRAIGHT;
            case UNDEF:
            default:
                return UNDEF;
        }
    }

    public void registerTurnouts(final List<Turnout> allTurnouts) {
        for (final Turnout turnout : allTurnouts) {
            getOrCreateSRCPTurnout(turnout);
        }
    }
}
