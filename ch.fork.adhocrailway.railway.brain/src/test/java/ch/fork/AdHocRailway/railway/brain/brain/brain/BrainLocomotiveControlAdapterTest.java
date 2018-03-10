package ch.fork.AdHocRailway.railway.brain.brain.brain;

import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.railway.brain.brain.BrainController;
import ch.fork.AdHocRailway.railway.brain.brain.BrainLocomotiveControlAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

public class BrainLocomotiveControlAdapterTest extends BrainTestSupport {

    @Mock
    private BrainController brainController;
    @Mock
    private LocomotiveChangeListener listener;

    private BrainLocomotiveControlAdapter testee;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void set_speed_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();

        givenTestee();

        final int speed = 10;
        whenSettingSpeed(locomotive, speed, new boolean[]{false, false,
                false, false, false});

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, speed, "1", "");
    }

    @Test
    public void toggle_direction_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
        locomotive.setCurrentSpeed(10);
        givenTestee();

        whenChangingDirection(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, 10, "0", "");
    }

    @Test
    public void toggle_direction_to_forward_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentDirection(LocomotiveDirection.REVERSE);
        locomotive.setCurrentSpeed(10);
        givenTestee();

        whenChangingDirection(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, 10, "1", "");
    }

    @Test
    public void emergency_stop_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
        locomotive.setCurrentSpeed(10);
        givenTestee();

        whenPerformingEmergencyStop(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, 0, "2", "");
    }

    private void whenPerformingEmergencyStop(Locomotive locomotive) {
        testee.emergencyStop(locomotive);
    }

    @Test
    public void increase_speed_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(10);

        givenTestee();

        final int speed = 11;
        whenIncreasingSpeed(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, speed, "1", "");
    }

    @Test
    public void increase_speed_digital_locomotive_speed_127()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(127);

        givenTestee();

        whenIncreasingSpeed(locomotive);

        assertBrainSetSpeedCalled(locomotive, 127, "1", "");
    }

    @Test
    public void decrease_speed_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(10);

        givenTestee();

        final int speed = 9;
        whenDecreasingSpeed(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, speed, "1", "");
    }

    @Test
    public void decrease_speed_digital_locomotive_speed_0()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(0);

        givenTestee();
        whenDecreasingSpeed(locomotive);

        assertBrainSetSpeedCalled(locomotive, 0, "1", "");
    }

    @Test
    public void set_functions_digital_locomotive()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(0);

        givenTestee();
        whenSettingFunction(locomotive, 0, true);

        assertBrainSetSpeedCalled(locomotive, 0, "1", "1 0 0 0 0");
    }

    @Test
    public void set_function_4_digital_locomotive()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(0);

        givenTestee();
        whenSettingFunction(locomotive, 4, true);

        assertBrainSetSpeedCalled(locomotive, 0, "1", "0 0 0 0 1");
    }

    private void whenSettingFunction(Locomotive locomotive, int i, boolean state) {
        testee.setFunction(locomotive, i, state, -1);
    }

    private void assertNoBrainCall() {
        Mockito.verifyZeroInteractions(brainController);
    }

    private void whenIncreasingSpeed(final Locomotive locomotive) {
        testee.increaseSpeed(locomotive, 1);
    }

    private void whenChangingDirection(final Locomotive locomotive) {
        testee.toggleDirection(locomotive);
    }

    private void whenDecreasingSpeed(final Locomotive locomotive) {
        testee.decreaseSpeed(locomotive, 1);
    }

    private void assertBrainSetSpeedCalled(final Locomotive locomotive,
                                           final int speed, final String direction,
                                           final String functions) throws IOException {
        String brainLocomotiveCommand = createBrainLocomotiveCommand(locomotive, speed, direction, functions);
        Mockito.verify(brainController).write(brainLocomotiveCommand);
    }

    private void assertBrainInitLocoCall(final Locomotive locomotive)
            throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XLS ");
        stringBuilder.append(locomotive.getAddress1());
        stringBuilder.append(" mm2");
        Mockito.verify(brainController).write(stringBuilder.toString());
    }

    private void whenSettingSpeed(final Locomotive locomotive, final int speed,
                                  final boolean[] functions) {
        testee.setSpeed(locomotive, speed, functions);
    }

    private void givenTestee() {
        testee = new BrainLocomotiveControlAdapter(null, brainController);
    }

}
