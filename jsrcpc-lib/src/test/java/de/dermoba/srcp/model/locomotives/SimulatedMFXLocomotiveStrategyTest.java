package de.dermoba.srcp.model.locomotives;


import de.dermoba.srcp.model.locomotives.DoubleMMDigitalLocomotive;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimulatedMFXLocomotiveStrategyTest {

    private SimulatedMFXLocomotiveStrategy testee;

    @Before
    public void setup() {
        testee = new SimulatedMFXLocomotiveStrategy();
    }

    @Test
    public void get_function_array_for_emergency_stop() {
        // given
        DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = new DoubleMMDigitalLocomotive(1, 2, 3);


        // when
        final boolean[] emergencyStopFunctions = testee.getEmergencyStopFunctions(doubleMMDigitalLocomotive, 9);

        // then
        assertEquals(10, emergencyStopFunctions.length);

        assertFunctions(emergencyStopFunctions, false, false, false, false, false, false, false, false, false, true);
    }

    @Test
    public void merge_functions_from_first_part_locomotive() {
        // given
        DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = new DoubleMMDigitalLocomotive(1, 2, 3);
        boolean[] originalFunctions = new boolean[]{false, false, false, false, false, true, true, true, true, true};
        doubleMMDigitalLocomotive.setFunctions(originalFunctions);

        // when
        testee.mergeFunctions(doubleMMDigitalLocomotive, 2, new boolean[]{true, true, false, true, false});

        // then
        final boolean[] mergedFunctions = doubleMMDigitalLocomotive.getFunctions();
        assertFunctions(mergedFunctions, true, true, false, true, false, true, true, true, true, true);


    }

    @Test
    public void merge_functions_from_second_part_locomotive() {
        // given
        DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = new DoubleMMDigitalLocomotive(1, 2, 3);
        boolean[] originalFunctions = new boolean[]{false, false, false, false, false, true, true, true, true, true};
        doubleMMDigitalLocomotive.setFunctions(originalFunctions);

        // when
        testee.mergeFunctions(doubleMMDigitalLocomotive, 3, new boolean[]{false, false,true, true, false});

        // then
        final boolean[] mergedFunctions = doubleMMDigitalLocomotive.getFunctions();
        assertFunctions(mergedFunctions,false, false, false, false, false, false, false,true, true, false);


    }

    private void assertFunctions(boolean[] mergedFunctions, boolean... expected) {
        assertEquals(mergedFunctions.length, expected.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("function " + i + " is not correct", expected[i], mergedFunctions[i]);
        }
    }


}