package ch.fork.AdHocRailway.railway.brain.brain;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.TaskExecutor;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.utils.LocomotiveHelper;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class BrainLocomotiveControlAdapter extends LocomotiveController implements BrainListener {

    private final BrainController brain;

    private final Set<Locomotive> activeLocomotives = Sets.newHashSet();
    private final BrainLocomotiveCommandBuilder brainLocomotiveCommandBuilder;

    public BrainLocomotiveControlAdapter(TaskExecutor taskExecutor, final BrainController brain) {
        super(taskExecutor);
        this.brain = brain;
        brain.addBrainListener(this);
        brainLocomotiveCommandBuilder = new BrainLocomotiveCommandBuilder();
    }

    @Override
    public void toggleDirection(final Locomotive locomotive) {
        LocomotiveHelper.toggleDirection(locomotive);
        setSpeed(locomotive, locomotive.getCurrentSpeed(),
                locomotive.getCurrentFunctions());
        informListeners(locomotive);
    }

    @Override
    public void setSpeed(final Locomotive locomotive, final int speed,
                         final boolean[] functions) {

        initLocomotive(locomotive);

        try {
            final String command = brainLocomotiveCommandBuilder.getLocomotiveCommand(locomotive, speed, locomotive.getCurrentDirection(), functions, false);

            brain.write(command);
            locomotive.setCurrentSpeed(speed);
            locomotive.setCurrentFunctions(functions);
            informListeners(locomotive);
        } catch (final BrainException e) {
            throw new ControllerException("error setting speed", e);
        }

    }

    @Override
    public void terminateAllLocomotives() {
        brain.write("XLOCCLEAR");
    }

    @Override
    public void terminateLocomotive(final Locomotive locomotive) {
        try {
            if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {
                final String termCommand1 = getTermCommand(locomotive,
                        locomotive.getAddress1());
                final String termCommand2 = getTermCommand(locomotive,
                        locomotive.getAddress2());
                brain.write(termCommand1);
                brain.write(termCommand2);
            } else {
                final String termCommand = getTermCommand(locomotive,
                        locomotive.getAddress1());
                brain.write(termCommand);
            }
            activeLocomotives.remove(locomotive);
        } catch (final BrainException e) {
            throw new ControllerException("error initializing locomotive", e);
        }
    }

    private String getTermCommand(Locomotive locomotive, int address) {
        return String.format("XLOCREMOVE %d", address);
    }

    private void initLocomotive(final Locomotive locomotive) {
        try {
            if (!activeLocomotives.contains(locomotive)) {
                if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {
                    final String initCommand1 = getInitCommand(locomotive,
                            locomotive.getAddress1());
                    final String initCommand2 = getInitCommand(locomotive,
                            locomotive.getAddress2());
                    brain.write(initCommand1);
                    brain.write(initCommand2);
                } else {
                    final String initCommand = getInitCommand(locomotive,
                            locomotive.getAddress1());
                    brain.write(initCommand);
                }
                activeLocomotives.add(locomotive);
            }
        } catch (final BrainException e) {
            throw new ControllerException("error initializing locomotive", e);
        }
    }

    @Override
    public void setFunction(final Locomotive locomotive,
                            final int functionNumber, final boolean state,
                            final int deactivationDelay) {
        final boolean[] currentFunctions = locomotive.getCurrentFunctions();

        if (functionNumber >= currentFunctions.length) {
            return;
        }

        setFunctions(locomotive, functionNumber, state);
        currentFunctions[functionNumber] = state;
        locomotive.setCurrentFunctions(currentFunctions);

        informListeners(locomotive);

        if (deactivationDelay > 0) {
            startFunctionDeactivationThread(locomotive, functionNumber,
                    deactivationDelay);
        }
    }

    private void setFunctions(Locomotive locomotive, int functionNumber, boolean state) {
        List<String> functionsCommands = brainLocomotiveCommandBuilder.getFunctionsCommands(locomotive, functionNumber, state);

        for (String functionsCommand : functionsCommands) {
            brain.write(functionsCommand);
        }
    }

    @Override
    public void emergencyStop(final Locomotive locomotive) {

        initLocomotive(locomotive);
        locomotive.setTargetSpeed(-1);

        try {
            final String command = brainLocomotiveCommandBuilder.getLocomotiveCommand(locomotive, 0, LocomotiveDirection.EMERGENCY_STOP, new boolean[0], false);
            brain.write(command);
            locomotive.setCurrentSpeed(0);
            informListeners(locomotive);
        } catch (final BrainException e) {
            throw new ControllerException("error setting speed", e);
        }

    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean isLocked(final Locomotive object) {
        return false;
    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean isLockedByMe(final Locomotive object) {
        return true;
    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean acquireLock(final Locomotive object) {
        return true;
    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean releaseLock(final Locomotive object) {
        return true;
    }


    private String getInitCommand(final Locomotive locomotive, final int address) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XLS ");
        stringBuilder.append(address);
        stringBuilder.append(" ");
        if (locomotive.getType() == LocomotiveType.DELTA) {
            stringBuilder.append("mm");
        } else if (locomotive.getType() == LocomotiveType.DIGITAL) {
            stringBuilder.append("mm2");
        } else if (locomotive.getType() == LocomotiveType.MFX) {
            stringBuilder.append("mfx");
            stringBuilder.append(" ");
            stringBuilder.append(locomotive.getMfxUUID());
        } else if (locomotive.getType() == LocomotiveType.DCC) {
            stringBuilder.append("dcc");
        }

        final String initCommand = stringBuilder.toString().trim();
        return initCommand;
    }

    @Override
    public void sentMessage(String sentMessage) {

    }

    @Override
    public void receivedMessage(String receivedMessage) {

    }

    @Override
    public void brainReset(String receivedMessage) {
        activeLocomotives.clear();
    }
}
