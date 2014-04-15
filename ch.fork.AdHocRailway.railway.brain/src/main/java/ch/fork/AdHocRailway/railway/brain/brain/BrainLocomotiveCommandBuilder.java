package ch.fork.AdHocRailway.railway.brain.brain;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.SimulatedMFXLocomotivesHelper;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;

import java.util.Arrays;
import java.util.List;

/**
 * Created by fork on 3/22/14.
 */
public class BrainLocomotiveCommandBuilder {

    public String getLocomotiveCommand(final Locomotive locomotive,
                                       final int speed, final boolean[] functions) {
        return getLocomotiveCommand(locomotive, locomotive.getAddress1(), speed, functions);
    }

    private String getLocomotiveCommand(final Locomotive locomotive,
                                        final int address, final int speed, final boolean[] functions) {
        if (LocomotiveType.DIGITAL == locomotive.getType() && functions == null && functions.length != 5) {
            throw new ControllerException("invalid function count of locomotive " + locomotive.getName());
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XL ");
        stringBuilder.append(address);
        stringBuilder.append(" ");
        stringBuilder.append(speed);
        stringBuilder.append(" ");
        stringBuilder.append(functions[0] ? "1" : "0");
        stringBuilder.append(" ");
        stringBuilder
                .append(locomotive.getCurrentDirection() == LocomotiveDirection.FORWARD ? "1"
                        : "0");
        stringBuilder.append(" ");
        for (int i = 1; i <= 4; i++) {
            stringBuilder.append(functions[i] ? "1" : "0");
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public List<String> getFunctionsCommands(final Locomotive locomotive
            , int functionNumber, boolean state) {
        if (locomotive == null) {
            throw new IllegalArgumentException("locomotive must not be null");
        }

        boolean[] multipartFunctions = getExpandedFunctionArray(locomotive, functionNumber, state);

        if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {

            final boolean[] functions1 = Arrays.copyOfRange(multipartFunctions, 0, 5);
            final boolean[] functions2 = Arrays.copyOfRange(multipartFunctions, 5, 11);
            final String speedCommand1 = getLocomotiveCommand(locomotive,
                    locomotive.getAddress1(), locomotive.getCurrentSpeed(),
                    functions1);
            final String speedCommand2 = getLocomotiveCommand(locomotive,
                    locomotive.getAddress2(), locomotive.getCurrentSpeed(),
                    functions2);

            return Arrays.asList(speedCommand1, speedCommand2);
        } else {
            return Arrays.asList(getLocomotiveCommand(locomotive, locomotive.getAddress1(),
                    locomotive.getCurrentSpeed(), multipartFunctions));
        }
    }

    private boolean[] getExpandedFunctionArray(Locomotive locomotive, int functionNumber, boolean state) {
        boolean[] copyOfCurrentFunctions = Arrays.copyOf(locomotive.getCurrentFunctions(), locomotive.getCurrentFunctions().length);

        final int hardwareFunctionNumber = SimulatedMFXLocomotivesHelper
                .computeMultipartFunctionNumber(locomotive.getType(),
                        functionNumber);
        boolean[] multipartFunctions = SimulatedMFXLocomotivesHelper.convertToMultipartFunctions(locomotive.getType(), copyOfCurrentFunctions);
        multipartFunctions[hardwareFunctionNumber] = state;
        return multipartFunctions;
    }

}
