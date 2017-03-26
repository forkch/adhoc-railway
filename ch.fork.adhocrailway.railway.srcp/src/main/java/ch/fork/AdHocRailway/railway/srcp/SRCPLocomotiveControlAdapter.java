package ch.fork.AdHocRailway.railway.srcp;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.SimulatedMFXLocomotivesHelper;
import ch.fork.AdHocRailway.controllers.TaskExecutor;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.utils.LocomotiveHelper;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.locomotives.*;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author fork
 */
public class SRCPLocomotiveControlAdapter extends LocomotiveController
        implements SRCPLocomotiveChangeListener, SRCPLockChangeListener {
    private static final Logger LOGGER = Logger.getLogger(SRCPLocomotiveControlAdapter.class);

    private static final Map<LocomotiveType, SRCPLocomotiveCreateStrategy> TYPE_TO_CREATE_STRATEGY_MAP = new HashMap<LocomotiveType, SRCPLocomotiveCreateStrategy>();

    static {
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.DELTA, new SRCPLocomotiveCreateStrategy.DeltaLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.DIGITAL, new SRCPLocomotiveCreateStrategy.DigitalLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.SIMULATED_MFX, new SRCPLocomotiveCreateStrategy.SimulatedMFXLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.MFX, new SRCPLocomotiveCreateStrategy.MFXLocomotiveCreateStrategy());
        TYPE_TO_CREATE_STRATEGY_MAP.put(LocomotiveType.DCC, new SRCPLocomotiveCreateStrategy.DCCLocomotiveCreateStrategy());
    }

    private final Map<Locomotive, SRCPLocomotive> locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
    private final Map<SRCPLocomotive, Locomotive> SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();

    private final SRCPLocomotiveControl locomotiveControl;
    private EmergencyStopState emergencyStopState = EmergencyStopState.NONE;

    public SRCPLocomotiveControlAdapter(TaskExecutor taskExecutor) {
        super(taskExecutor);
        locomotiveControl = SRCPLocomotiveControl.getInstance();
        locomotiveControl.removeAllLocomotiveChangeListener();

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

        LOGGER.debug("pending speed commands: " + pendingTasksCount());
        if (pendingTasksCount() == 0 && emergencyStopState == EmergencyStopState.PENDING) {
            emergencyStopState = EmergencyStopState.NONE;
        }
        if (emergencyStopState != EmergencyStopState.NONE) {
            LOGGER.warn("emergency stop is not yet executed therefore ignoring this command");
            return;
        }
        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                try {
                    if (emergencyStopState != EmergencyStopState.NONE) {
                        LOGGER.debug("cancelling speed command: " + speed);
                    }

                    LOGGER.debug("waiting to execute speed command");
                    aquireRateLock();
                    synchronized (emergencyStopState) {
                        if (emergencyStopState == EmergencyStopState.NONE) {
                            LOGGER.debug("executing speed command: " + speed);
                            locomotiveControl.setSpeed(sLocomotive, speed,
                                    SimulatedMFXLocomotivesHelper.convertToMultipartFunctions(
                                            locomotive.getType(), functions)

                            );
                            locomotive.setCurrentSpeed(speed);
                            locomotive.setCurrentFunctions(functions);
                        } else {
                            LOGGER.debug("cancelling speed command: " + speed);
                        }
                    }

                } catch (SRCPModelException e) {
                    e.printStackTrace();
                }

            }
        });
        LOGGER.debug("scheduled speed command " + speed);
    }

    @Override
    public void terminateAllLocomotives() {
        throw new NotImplementedException();
    }

    @Override
    public void terminateLocomotive(Locomotive locomotive) {

        LOGGER.info("terminating loco: " + locomotive.getAddress1() + " " + locomotive.getName());

        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
        try {
            locomotiveControl.terminate(sLocomotive);
        } catch (SRCPModelException e) {
            throw new ControllerException("Locomotive Error", e);
        }
    }

    @Override
    public void emergencyStop(final Locomotive locomotive) {

        enqueueEmergencyTask(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (emergencyStopState) {
                        LOGGER.info(">>>>>EMERGENCY STOP<<<<<");
                        cancelTasks();
                        final SRCPLocomotive sLocomotive = getOrCreateSrcpLocomotive(locomotive);
                        emergencyStopState = EmergencyStopState.PENDING;
                        final int emergencyStopFunction = locomotive
                                .getEmergencyStopFunctionNumber();
                        final int srcpEmergencyStopFunction = SimulatedMFXLocomotivesHelper
                                .computeMultipartFunctionNumber(locomotive.getType(),
                                        emergencyStopFunction);
                        locomotive.setCurrentSpeed(0);
                        locomotive.setTargetSpeed(-1);

                        locomotiveControl.emergencyStop(sLocomotive,
                                srcpEmergencyStopFunction);
                        locomotive.setCurrentFunctions(locomotive.getCurrentFunctions());
                    }
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

        final boolean[] currentFunctions = SimulatedMFXLocomotivesHelper
                .convertFromMultipartFunctions(locomotive.getType(),
                        newFunctions);
        locomotive.setCurrentFunctions(currentFunctions);

        if ((emergencyStopState == EmergencyStopState.PENDING && changedSRCPLocomotive.getCurrentSpeed() != 0)) {
            locomotive.setCurrentSpeed(0);
            informListeners(locomotive);
        } else {
            if (locomotive.getTargetSpeed() == -1 || locomotive.getCurrentSpeed() == locomotive.getTargetSpeed()) {
                informListeners(locomotive);
                locomotive.setTargetSpeed(-1);
            }
        }
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
            srcpLocomotive = createNewSRCPLocomotive(locomotive, type);
        } else {
            if (!typesMatch(locomotive.getType(), srcpLocomotive)) {
                locomotiveSRCPLocomotiveMap.remove(locomotive);
                SRCPLocomotiveLocomotiveMap.remove(srcpLocomotive);
                srcpLocomotive = createNewSRCPLocomotive(locomotive, type);
            }
            srcpLocomotive = TYPE_TO_CREATE_STRATEGY_MAP.get(type).updateSRCPLocomotive(srcpLocomotive, locomotive);
        }

        LOGGER.info(srcpLocomotive);
        return srcpLocomotive;
    }

    private SRCPLocomotive createNewSRCPLocomotive(Locomotive locomotive, LocomotiveType type) {
        SRCPLocomotive srcpLocomotive;
        srcpLocomotive = TYPE_TO_CREATE_STRATEGY_MAP.get(type).createSRCPLocomotive(locomotive);
        locomotiveSRCPLocomotiveMap.put(locomotive, srcpLocomotive);
        SRCPLocomotiveLocomotiveMap.put(srcpLocomotive, locomotive);
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
                return srcpLocomotive instanceof MfxLocomotive;
            case DCC:
                return srcpLocomotive instanceof DCCLocomotive;
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

    private enum EmergencyStopState {
        PENDING, EXECUTED, NONE
    }

}
