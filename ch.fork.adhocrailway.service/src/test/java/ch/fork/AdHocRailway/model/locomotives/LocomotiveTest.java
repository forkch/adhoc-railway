package ch.fork.AdHocRailway.model.locomotives;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocomotiveTest {

    @Test
    public void getting_emergency_function_should_return_correct_function_number_sorted_list() {
        // given
        Locomotive locomotive = new Locomotive();
        locomotive.addLocomotiveFunction(new LocomotiveFunction(0, "0", true, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(1, "1", false, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(2, "2", false, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(3, "3", false, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(4, "4", false, 0));

        // when
        final int emergencyStopFunctionNumber = locomotive.getEmergencyStopFunctionNumber();

        // then
        assertEquals(0, emergencyStopFunctionNumber);
    }

    @Test
    public void getting_emergency_function_should_return_correct_function_number_unsorted_list() {
        // given
        Locomotive locomotive = new Locomotive();
        locomotive.addLocomotiveFunction(new LocomotiveFunction(4, "4", true, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(0, "0", false, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(1, "1", false, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(3, "3", false, 0));
        locomotive.addLocomotiveFunction(new LocomotiveFunction(2, "2", false, 0));

        // when
        final int emergencyStopFunctionNumber = locomotive.getEmergencyStopFunctionNumber();

        // then
        assertEquals(4, emergencyStopFunctionNumber);
    }

}