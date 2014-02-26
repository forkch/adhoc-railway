package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class SimulatedMFXLocomotivesHelper {

    /**
     * For SimulatedMFX Locomotives the higher functions of the second address
     * need to be offsetted by 1 since there is no "F0" on the second address
     *
     * @param locomotive
     * @param functionNumber
     * @return
     */
    public static int computeMultipartFunctionNumber(final LocomotiveType type,
                                                     final int functionNumber) {
        final int multipartFunctionNumber = functionNumber;
        if (type.equals(LocomotiveType.SIMULATED_MFX)) {
            if (functionNumber > 8) {
                throw new IllegalArgumentException(
                        "function number must not be higher than 8 for SIMULATED_MFX locomotives");
            }
            if (functionNumber >= 5) {
                return multipartFunctionNumber + 1;
            }
        }
        if (functionNumber > 4) {
            throw new IllegalArgumentException(
                    "function number must not be higher than 8 for SIMULATED_MFX locomotives");
        }
        return multipartFunctionNumber;
    }

    public static boolean[] convertFromMultipartFunctions(
            final LocomotiveType type, final boolean[] multipartFunctions) {

        if (type.equals(LocomotiveType.SIMULATED_MFX)) {

            final boolean[] functionsForSimulatedMfx = new boolean[9];

            functionsForSimulatedMfx[0] = multipartFunctions[0];
            for (int i = 1; i < 5; i++) {
                functionsForSimulatedMfx[i] = multipartFunctions[i];
                functionsForSimulatedMfx[i + 4] = multipartFunctions[i + 5];
            }
            return functionsForSimulatedMfx;
        } else {
            return multipartFunctions;
        }

    }

    /**
     * this functions generates SRCP function arrays for simulated MFX
     * locomotives (each its seperate 5-position function array)
     *
     * @param locomotive
     * @param functions
     * @return
     */
    public static boolean[] convertToMultipartFunctions(
            final LocomotiveType type, final boolean[] functions) {
        if (type.equals(LocomotiveType.SIMULATED_MFX)) {
            boolean[] multipartFunctions = null;
            multipartFunctions = new boolean[10];
            multipartFunctions[0] = functions[0];
            multipartFunctions[5] = false;
            for (int i = 1; i < 5; i++) {
                multipartFunctions[i] = functions[i];
                multipartFunctions[i + 5] = functions[i + 4];
            }
            return multipartFunctions;
        } else {
            return functions;
        }
    }
}
