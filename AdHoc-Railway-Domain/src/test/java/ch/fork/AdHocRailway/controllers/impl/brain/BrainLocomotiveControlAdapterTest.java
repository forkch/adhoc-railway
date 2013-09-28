package ch.fork.AdHocRailway.controllers.impl.brain;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;

public class BrainLocomotiveControlAdapterTest {

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
	public void set_speed_digital_locomotive() throws LocomotiveException,
			IOException {
		final Locomotive locomotive = createDigitalLocomotive();

		givenTestee();

		final int speed = 10;
		whenSettingSpeed(locomotive, speed, new boolean[] { false, false,
				false, false, false });

		assertBrainInitLocoCall(locomotive);
		assertBrainSetSpeedCalled(locomotive, speed, "1", "0", "0 0 0 0");
	}

	@Test
	public void increase_speed_digital_locomotive() throws LocomotiveException,
			IOException {
		final Locomotive locomotive = createDigitalLocomotive();
		locomotive.setCurrentSpeed(10);

		givenTestee();

		final int speed = 11;
		whenIncreasingSpeed(locomotive);

		assertBrainInitLocoCall(locomotive);
		assertBrainSetSpeedCalled(locomotive, speed, "1", "0", "0 0 0 0");
	}

	@Test
	public void increase_speed_digital_locomotive_speed_14()
			throws LocomotiveException, IOException {
		final Locomotive locomotive = createDigitalLocomotive();
		locomotive.setCurrentSpeed(14);

		givenTestee();

		whenIncreasingSpeed(locomotive);
		assertNoBrainCall();
	}

	@Test
	public void decrease_speed_digital_locomotive() throws LocomotiveException,
			IOException {
		final Locomotive locomotive = createDigitalLocomotive();
		locomotive.setCurrentSpeed(10);

		givenTestee();

		final int speed = 9;
		whenDecreasingSpeed(locomotive);

		assertBrainInitLocoCall(locomotive);
		assertBrainSetSpeedCalled(locomotive, speed, "1", "0", "0 0 0 0");
	}

	@Test
	public void decrease_speed_digital_locomotive_speed_0()
			throws LocomotiveException, IOException {
		final Locomotive locomotive = createDigitalLocomotive();
		locomotive.setCurrentSpeed(0);

		givenTestee();

		whenDecreasingSpeed(locomotive);
		assertNoBrainCall();
	}

	private void assertNoBrainCall() {
		Mockito.verifyZeroInteractions(brainController);
	}

	private void whenIncreasingSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		testee.increaseSpeed(locomotive);
	}

	private void whenDecreasingSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		testee.decreaseSpeed(locomotive);
	}

	private void assertBrainSetSpeedCalled(final Locomotive locomotive,
			final int speed, final String direction, final String light,
			final String functions) throws IOException {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("XL ");
		stringBuilder.append(locomotive.getAddress1());
		stringBuilder.append(" ");
		stringBuilder.append(speed);
		stringBuilder.append(" " + light);
		stringBuilder.append(" " + direction);
		stringBuilder.append(" " + functions);
		Mockito.verify(brainController).write(stringBuilder.toString());
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
			final boolean[] functions) throws LocomotiveException {
		testee.setSpeed(locomotive, speed, functions);
	}

	private void givenTestee() {
		testee = new BrainLocomotiveControlAdapter(brainController);
	}

	private Locomotive createDigitalLocomotive() {
		final Locomotive locomotive = new Locomotive();
		locomotive.setType(LocomotiveType.DIGITAL);
		locomotive.setAddress1(1);
		return locomotive;
	}

}
