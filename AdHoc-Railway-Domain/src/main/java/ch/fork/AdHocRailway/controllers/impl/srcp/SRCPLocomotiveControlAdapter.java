package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.SimulatedMFXLocomotivesHelper;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.utils.LocomotiveHelper;
import com.google.common.util.concurrent.RateLimiter;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.locomotives.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;

/**
 * @author fork
 */
public class SRCPLocomotiveControlAdapter extends LocomotiveController
        implements SRCPLocomotiveChangeListener, SRCPLockChangeListener {
    private static final Logger LOGGER = Logger.getLogger(SRCPLocomotiveControlAdapter.class);

    private static final Map<LocomotiveType, SRCPLocomotiveCreateStrategy> TYPE_TO_CREATE_STRATEGY_MAP = new HashMap<LocomotiveType, SRCPLocomotiveCreateStrategy>();

    static {
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.DELTA, new DeltaLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.DIGITAL, new DigitalLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.SIMULATED_MFX, new SimulatedMFXLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.MFX, new SimulatedMFXLocomotiveCreateStrategy());
    }

    private final Map<Locomotive, SRCPLocomotive> locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
    private final Map<SRCPLocomotive, Locomotive> SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();

    private final SRCPLocomotiveControl locomotiveControl;
    private final RateLimiter rateLimiter;
    ExecutorService executorService;
    private boolean emergencyStopPending;

    public SRCPLocomotiveControlAdapter() {
        locomotiveControl = SRCPLocomotiveControl.getInstance();
        executorService = SRCPThreadUtils.createExecutorService();
        rateLimiter = SRCPThreadUtils.createRateLimiter();
        reloadConfiguration();
    }

    @Override
    public void setFunction(final Locomotive locomotive,
                            final int functionNumber, final boolean state,
                            final int deactivationDelay) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        final boolean[] srcpFunctions = locomotiveControl
                .getFunctions(sLocomotive);

        if (functionNumber >= srcpFunctions.length) {
            return;
        }

        final int srcpFunctionNumber = SimulatedMFXLocomotivesHelper
                .computeMultipartFunctionNumber(locomotive.getType(),
                        functionNumber);

        srcpFunctions[srcpFunctionNumber] = state;

        setFunctions(locomotive, srcpFunctions);

        if (deactivationDelay > 0) {
            startFunctionDeactivationThread(locomotive, functionNumber,
                    deactivationDelay);
        }
    }

    @Override
    public void setSpeed(final Locomotive locomotive, final int speed,
                         final boolean[] functions) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (!emergencyStopPending) {
                    try {
                        LOGGER.info("waiting to execute speed command");
                        rateLimiter.acquire();
                        LOGGER.info("executing speed command: " + speed);
                        if (!emergencyStopPending) {
                            locomotiveControl.setSpeed(sLocomotive, speed,
                                    SimulatedMFXLocomotivesHelper.convertToMultipartFunctions(
                                            locomotive.getType(), functions)
                            );
                        } else {
                            LOGGER.info("cancelling speed command: " + speed);
                        }
                    } catch (SRCPModelException e) {
                        e.printStackTrace();
                    }
                } else {
                    LOGGER.info("cancelling speed command: " + speed);
                }
            }
        });
        locomotive.setCurrentSpeed(speed);
        locomotive.setCurrentFunctions(functions);
    }

    @Override
    public void emergencyStop(final Locomotive locomotive) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        emergencyStopPending = true;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info(">>>>>EMERGENCY STOP<<<<<");
                    final int emergencyStopFunction = locomotive
                            .getEmergencyStopFunction();

                    final int srcpEmergencyStopFunction = SimulatedMFXLocomotivesHelper
                            .computeMultipartFunctionNumber(locomotive.getType(),
                                    emergencyStopFunction);
                    locomotiveControl.emergencyStop(sLocomotive,
                            srcpEmergencyStopFunction);
                    locomotive.setCurrentSpeed(0);
                    locomotive.setCurrentFunctions(locomotive.getCurrentFunctions());
                    emergencyStopPending = false;
                } catch (SRCPModelException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void toggleDirection(final Locomotive locomotive) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        try {
            locomotiveControl.toggleDirection(sLocomotive);
            LocomotiveHelper.toggleDirection(locomotive);

        } catch (final SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    public void reloadConfiguration() {
        SRCPLockControl.getInstance().setLockDuration(0);
    }

    public void setSession(final SRCPSession session) {
        locomotiveControl.addLocomotiveChangeListener(this, this);
        locomotiveControl.setSession(session);
    }

    @Override
    public boolean acquireLock(final Locomotive locomotive) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        try {
            return locomotiveControl.acquireLock(sLocomotive);
        } catch (final SRCPLockingException e) {
            throw new ControllerException("Locomotive Locked", e);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    @Override
    public boolean isLocked(final Locomotive locomotive) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        if (locomotiveControl.getSession() == null) {
            return false;
        }
        try {
            return locomotiveControl.isLocked(sLocomotive);
        } catch (final SRCPLockingException e) {
            throw new ControllerException("Locomotive Locked", e);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    @Override
    public boolean isLockedByMe(final Locomotive locomotive) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        if (locomotiveControl.getSession() == null) {
            return false;
        }
        try {
            return locomotiveControl.isLockedByMe(sLocomotive);

        } catch (final SRCPLockingException e) {
            throw new ControllerException("Locomotive Locked", e);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    @Override
    public boolean releaseLock(final Locomotive locomotive) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        try {
            return locomotiveControl.releaseLock(sLocomotive);
        } catch (final SRCPLockingException e) {
            throw new ControllerException("Locomotive Locked", e);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    @Override
    public void locomotiveChanged(final SRCPLocomotive changedSRCPLocomotive) {

        final Locomotive locomotive = SRCPLocomotiveLocomotiveMap
                .get(changedSRCPLocomotive);

        locomotive.setCurrentSpeed(changedSRCPLocomotive.getCurrentSpeed());
        switch (changedSRCPLocomotive.getDirection()) {
            case FORWARD:
                locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
            case REVERSE:
                locomotive.setCurrentDirection(LocomotiveDirection.REVERSE);
            case UNDEF:
                locomotive.setCurrentDirection(LocomotiveDirection.UNDEF);
        }

        final boolean[] newFunctions = changedSRCPLocomotive.getFunctions();

        locomotive.setCurrentFunctions(SimulatedMFXLocomotivesHelper
                .convertFromMultipartFunctions(locomotive.getType(),
                        newFunctions));

        informListeners(locomotive);
    }

    @Override
    public void lockChanged(final Object changedLock, final boolean locked) {
        final SRCPLocomotive changedLocomotive = (SRCPLocomotive) changedLock;
        informListeners(SRCPLocomotiveLocomotiveMap.get(changedLocomotive));
    }

    SRCPLocomotive getOrCreateSrcpLocomotive(final Locomotive locomotive) {
        if (locomotive == null) {
            throw new IllegalArgumentException("locomotive must not be null");
        }
        final LocomotiveType type = locomotive.getType();

        SRCPLocomotive srcpLocomotive = locomotiveSRCPLocomotiveMap
                .get(locomotive);
        if (srcpLocomotive == null) {
            srcpLocomotive = TYPE_TO_CREATE_STRATEGY_MAP.get(type).createSRCPLocomotive(locomotive);
            locomotiveSRCPLocomotiveMap.put(locomotive, srcpLocomotive);
            SRCPLocomotiveLocomotiveMap.put(srcpLocomotive, locomotive);
        } else {
            if (!typesMatch(locomotive.getType(), srcpLocomotive)) {
                locomotiveSRCPLocomotiveMap.remove(locomotive);
                SRCPLocomotiveLocomotiveMap.remove(srcpLocomotive);
                srcpLocomotive = TYPE_TO_CREATE_STRATEGY_MAP.get(type).createSRCPLocomotive(locomotive);
                locomotiveSRCPLocomotiveMap.put(locomotive, srcpLocomotive);
                SRCPLocomotiveLocomotiveMap.put(srcpLocomotive, locomotive);
            }
            srcpLocomotive = TYPE_TO_CREATE_STRATEGY_MAP.get(type).updateSRCPLocomotive(srcpLocomotive, locomotive);
        }

        return srcpLocomotive;
    }

    private boolean typesMatch(LocomotiveType type, SRCPLocomotive srcpLocomotive) {
        switch (type) {

            case DELTA:
                return srcpLocomotive instanceof MMDeltaLocomotive;
            case DIGITAL:
                return srcpLocomotive instanceof MMDigitalLocomotive;
            case SIMULATED_MFX:
                return srcpLocomotive instanceof DoubleMMDigitalLocomotive;
            case MFX:
                return srcpLocomotive instanceof MMDigitalLocomotive;
        }
        return false;
    }

    private void setFunctions(final Locomotive locomotive,
                              final boolean[] srcpFunctions) {
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        try {
            locomotiveControl.setFunctions(sLocomotive, srcpFunctions);
            locomotive.setCurrentFunctions(SimulatedMFXLocomotivesHelper
                    .convertFromMultipartFunctions(locomotive.getType(),
                            srcpFunctions));
        } catch (final SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    public void registerLocomotives(final SortedSet<Locomotive> allLocomotives) {
        for (final Locomotive locomotive : allLocomotives) {
            getOrCreateSrcpLocomotive(locomotive);
        }
    }

}
