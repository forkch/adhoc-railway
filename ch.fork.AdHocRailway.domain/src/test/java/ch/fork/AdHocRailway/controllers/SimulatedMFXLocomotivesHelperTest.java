package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimulatedMFXLocomotivesHelperTest {

    @Test
    public void normal_digital_locomotive_convert_to_multipart() {
        // given
        final boolean[] functions = new boolean[]{false, true, false, true,
                false};

        // when
        final boolean[] convertedFunctions = SimulatedMFXLocomotivesHelper
                .convertToMultipartFunctions(LocomotiveType.DIGITAL, functions);

        // then
        assertArrayEquals(functions, convertedFunctions);

    }

    @Test
    public void normal_delta_locomotive_convert_to_multipart() {
        // given
        final boolean[] functions = new boolean[]{false};

        // when
        final boolean[] convertedFunctions = SimulatedMFXLocomotivesHelper
                .convertToMultipartFunctions(LocomotiveType.DELTA, functions);

        // then
        assertArrayEquals(functions, convertedFunctions);

    }

    @Test
    public void simulated_mfx_locomotive_convert_to_multipart() {
        // given
        final boolean[] functions = new boolean[]{false, true, true, true,
                true, true, true, true, true};

        // when
        final boolean[] convertedFunctions = SimulatedMFXLocomotivesHelper
                .convertToMultipartFunctions(LocomotiveType.SIMULATED_MFX,
                        functions);

        // then
        assertArrayEquals(new boolean[]{false, true, true, true, true, false,
                true, true, true, true}, convertedFunctions);

    }

    @Test
    public void normal_digital_locomotive_convert_from_multipart() {
        // given
        final boolean[] functions = new boolean[]{false, true, false, true,
                false};

        // when
        final boolean[] convertedFunctions = SimulatedMFXLocomotivesHelper
                .convertToMultipartFunctions(LocomotiveType.DIGITAL, functions);

        // then
        assertArrayEquals(functions, convertedFunctions);

    }

    @Test
    public void normal_delta_locomotive_convert_from_multipart() {
        // given
        final boolean[] functions = new boolean[]{false};

        // when
        final boolean[] convertedFunctions = SimulatedMFXLocomotivesHelper
                .convertToMultipartFunctions(LocomotiveType.DELTA, functions);

        // then
        assertArrayEquals(functions, convertedFunctions);

    }

    @Test
    public void simulated_mfx_locomotive_convert_from_multipart() {
        // given
        final boolean[] functions = new boolean[]{false, true, true, true,
                true, false, true, true, true, true};

        // when
        final boolean[] convertedFunctions = SimulatedMFXLocomotivesHelper
                .convertFromMultipartFunctions(LocomotiveType.SIMULATED_MFX,
                        functions);

        // then
        assertArrayEquals(new boolean[]{false, true, true, true, true, true,
                true, true, true}, convertedFunctions);

    }

    @Test
    public void normal_digital_locomotive_multipart_function_number() {
        assertEquals(4,
                SimulatedMFXLocomotivesHelper.computeMultipartFunctionNumber(
                        LocomotiveType.DIGITAL, 4)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void normal_digital_locomotive_multipart_function_number_too_big_throws_exception() {
        assertEquals(5,
                SimulatedMFXLocomotivesHelper.computeMultipartFunctionNumber(
                        LocomotiveType.DIGITAL, 5)
        );
    }

    @Test
    public void simulated_mfxlocomotive_multipart_function_number() {
        assertEquals(9,
                SimulatedMFXLocomotivesHelper.computeMultipartFunctionNumber(
                        LocomotiveType.SIMULATED_MFX, 8)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void simulated_mfx_locomotive_multipart_function_number_too_big_throws_exception() {
        assertEquals(10,
                SimulatedMFXLocomotivesHelper.computeMultipartFunctionNumber(
                        LocomotiveType.SIMULATED_MFX, 9)
        );
    }

    private void assertArrayEquals(final boolean[] expected,
                                   final boolean[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }
}
