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
                                       final int speed, LocomotiveDirection newDirection, final boolean[] functions, boolean withFunctions) {
        return getLocomotiveCommand(locomotive, locomotive.getAddress1(), speed, newDirection, functions, withFunctions);
    }

    private String getLocomotiveCommand(final Locomotive locomotive,
                                        final int address, final int speed, LocomotiveDirection newDirection, final boolean[] functions, boolean withFunctions) {
        if (LocomotiveType.DIGITAL == locomotive.getType() && functions == null && functions.length != 5) {
            throw new ControllerException("invalid function count of locomotive " + locomotive.getName());
        }
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XL ");
        stringBuilder.append(address);
        stringBuilder.append(" ");
        stringBuilder.append(speed);
        stringBuilder.append(" ");
        stringBuilder.append(newDirection.code);

        if (withFunctions) {
            stringBuilder.append(" ");
            int functionCount;
            if (locomotive.getType() == LocomotiveType.SIMULATED_MFX) {
                functionCount = 5;
            } else {
                functionCount = locomotive.getFunctions().size();
            }
            for (int i = 0; i < functionCount; i++) {
                stringBuilder.append(functions[i] ? "1" : "0");
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString().trim();
    }

    public List<String> getFunctionsCommands(final Locomotive locomotive
            , int functionNumber, boolean state) {
        if (locomotive == null) {
            throw new IllegalArgumentException("locomotive must not be null");
        }

        if (functionNumber < 0) {
            throw new IllegalArgumentException("function number must be >= 0");
        }
        boolean[] multipartFunctions = getExpandedFunctionArray(locomotive, functionNumber, state);

        if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {

            final boolean[] functions1 = Arrays.copyOfRange(multipartFunctions, 0, 5);
            final boolean[] functions2 = Arrays.copyOfRange(multipartFunctions, 5, 11);
            final String speedCommand1 = getLocomotiveCommand(locomotive,
                    locomotive.getAddress1(), locomotive.getCurrentSpeed(), locomotive.getCurrentDirection(),
                    functions1, true);
            final String speedCommand2 = getLocomotiveCommand(locomotive,
                    locomotive.getAddress2(), locomotive.getCurrentSpeed(), locomotive.getCurrentDirection(),
                    functions2, true);

            return Arrays.asList(speedCommand1, speedCommand2);
        } else {
            return Arrays.asList(getLocomotiveCommand(locomotive, locomotive.getAddress1(),
                    locomotive.getCurrentSpeed(),  locomotive.getCurrentDirection(), multipartFunctions, true));
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
