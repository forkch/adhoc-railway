package ch.fork.adhocrailway.railway.brain.brain.brain;

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveDirection;
import ch.fork.adhocrailway.railway.brain.brain.BrainLocomotiveCommandBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by fork on 3/22/14.
 */
public class BrainLocomotiveCommandBuilderTest extends BrainTestSupport {

    private BrainLocomotiveCommandBuilder testee;

    @Before
    public void setup() {
        testee = new BrainLocomotiveCommandBuilder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_functions_command_null_locomotive() throws IOException {

        testee.getFunctionsCommands(null, 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_functions_command_digital_locomotive_illegal_function() throws IOException {
        Locomotive digitalLocomotive = createDigitalLocomotive();

        testee.getFunctionsCommands(digitalLocomotive, 6, true);
    }

    @Test
    public void set_speed_digital_locomotive() throws IOException {
        Locomotive digitalLocomotive = createDigitalLocomotive();
        digitalLocomotive.setCurrentDirection(LocomotiveDirection.FORWARD);

        String locomotiveCommand = testee.getLocomotiveCommand(digitalLocomotive, 2, digitalLocomotive.getCurrentDirection(), digitalLocomotive.getCurrentFunctions(), false);

        assertEquals("XL 1 2 1", locomotiveCommand);
    }

    @Test
    public void set_speed_digital_locomotive_reverse() throws IOException {
        Locomotive digitalLocomotive = createDigitalLocomotive();
        digitalLocomotive.setCurrentDirection(LocomotiveDirection.REVERSE);

        String locomotiveCommand = testee.getLocomotiveCommand(digitalLocomotive, 2, digitalLocomotive.getCurrentDirection(), digitalLocomotive.getCurrentFunctions(), false);

        assertEquals("XL 1 2 0", locomotiveCommand);
    }

    @Test
    public void get_functions_command_digital_locomotive_light_on() throws IOException {
        Locomotive digitalLocomotive = createDigitalLocomotive();

        List<String> locomotiveCommands = testee.getFunctionsCommands(digitalLocomotive, 0, true);
        assertEquals(1, locomotiveCommands.size());
        assertEquals("XL 1 0 1 1 0 0 0 0", locomotiveCommands.get(0));
    }

    @Test
    public void get_functions_command_digital_locomotive_functions() throws IOException {
        Locomotive digitalLocomotive = createDigitalLocomotive();

        List<String> locomotiveCommands = testee.getFunctionsCommands(digitalLocomotive, 0, true);
        assertEquals(1, locomotiveCommands.size());
        assertEquals("XL 1 0 1 1 0 0 0 0", locomotiveCommands.get(0));

        locomotiveCommands = testee.getFunctionsCommands(digitalLocomotive, 1, true);
        assertEquals(1, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 1 0 0 0", locomotiveCommands.get(0));

        locomotiveCommands = testee.getFunctionsCommands(digitalLocomotive, 2, true);
        assertEquals(1, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 1 0 0", locomotiveCommands.get(0));

        locomotiveCommands = testee.getFunctionsCommands(digitalLocomotive, 3, true);
        assertEquals(1, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 1 0", locomotiveCommands.get(0));

        locomotiveCommands = testee.getFunctionsCommands(digitalLocomotive, 4, true);
        assertEquals(1, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 0 1", locomotiveCommands.get(0));

    }

    @Test
    public void get_functions_command_simulated_mfx_locomotive() throws IOException {
        Locomotive simulatedMfxLocomotive = createSimulatedMfxLocomotive();

        List<String> locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 0, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 1 0 0 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 0 0", locomotiveCommands.get(1));
    }

    @Test
    public void get_functions_command_simulated_mfx_functions() throws IOException {
        Locomotive simulatedMfxLocomotive = createSimulatedMfxLocomotive();

        List<String> locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 0, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 1 0 0 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 1, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 1 0 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 2, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 1 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 3, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 1 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 4, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 0 1", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 5, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 1 0 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 6, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 1 0 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 7, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 1 0 1 0 0 0 0 0", locomotiveCommands.get(0));
        assertEquals("XL 2 0 1 0 0 0 1 0", locomotiveCommands.get(1));

        locomotiveCommands = testee.getFunctionsCommands(simulatedMfxLocomotive, 8, true);
        assertEquals(2, locomotiveCommands.size());
        assertEquals("XL 2 0 1 0 0 0 0 1", locomotiveCommands.get(1));
        assertEquals("XL 1 0 1 0 0 0 0 0", locomotiveCommands.get(0));
    }
}
