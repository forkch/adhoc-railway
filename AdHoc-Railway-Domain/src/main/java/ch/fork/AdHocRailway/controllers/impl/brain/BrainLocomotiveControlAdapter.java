package ch.fork.AdHocRailway.controllers.impl.brain;

import java.util.Arrays;
import java.util.Set;

import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.SimulatedMFXLocomotivesHelper;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveHelper;

import com.google.common.collect.Sets;

public class BrainLocomotiveControlAdapter extends LocomotiveController {

	private final BrainController brain;

	private final Set<Locomotive> activeLocomotives = Sets.newHashSet();

	public BrainLocomotiveControlAdapter(final BrainController brain) {
		this.brain = brain;
	}

	@Override
	public void toggleDirection(final Locomotive locomotive)
			throws LocomotiveException {
		LocomotiveHelper.toggleDirection(locomotive);
		setSpeed(locomotive, locomotive.getCurrentSpeed(),
				locomotive.getCurrentFunctions());
		informListeners(locomotive);
	}

	@Override
	public void setSpeed(final Locomotive locomotive, final int speed,
			final boolean[] functions) throws LocomotiveException {

		initLocomotive(locomotive);

		try {
			final String command = getSpeedCommand(locomotive,
					locomotive.getAddress1(), speed, functions);

			brain.write(command);
			locomotive.setCurrentSpeed(speed);
			locomotive.setCurrentFunctions(functions);
			informListeners(locomotive);
		} catch (final BrainException e) {
			throw new LocomotiveException("error setting speed", e);
		}

	}

	private void initLocomotive(final Locomotive locomotive)
			throws LocomotiveException {
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
			throw new LocomotiveException("error initializing locomotive", e);
		}
	}

	@Override
	public void setFunction(final Locomotive locomotive,
			final int functionNumber, final boolean state,
			final int deactivationDelay) throws LocomotiveException {
		final boolean[] functions = locomotive.getCurrentFunctions();

		if (functionNumber >= functions.length) {
			return;
		}

		final int hardwareFunctionNumber = SimulatedMFXLocomotivesHelper
				.computeMultipartFunctionNumber(locomotive.getType(),
						functionNumber);

		functions[hardwareFunctionNumber] = state;

		setFunctions(locomotive, functions);
		locomotive.setCurrentFunctions(functions);

		informListeners(locomotive);

		if (deactivationDelay > 0) {
			startFunctionDeactivationThread(locomotive, functionNumber,
					deactivationDelay);
		}
	}

	private void setFunctions(final Locomotive locomotive,
			final boolean[] functions) {
		if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {

			final boolean[] functions1 = Arrays.copyOfRange(functions, 0, 5);
			final boolean[] functions2 = Arrays.copyOfRange(functions, 5, 10);
			final String speedCommand1 = getSpeedCommand(locomotive,
					locomotive.getAddress1(), locomotive.getCurrentSpeed(),
					functions1);
			final String speedCommand2 = getSpeedCommand(locomotive,
					locomotive.getAddress1(), locomotive.getCurrentSpeed(),
					functions2);

			brain.write(speedCommand1);
			brain.write(speedCommand2);
		} else {
			brain.write(getSpeedCommand(locomotive, locomotive.getAddress1(),
					locomotive.getCurrentSpeed(), functions));
		}
		locomotive.setCurrentFunctions(functions);
	}

	@Override
	public void emergencyStop(final Locomotive myLocomotive)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addOrUpdateLocomotive(final Locomotive locomotive) {

	}

	/**
	 * Locking is not supported for BrainLocomotives
	 */
	@Override
	public boolean isLocked(final Locomotive object) throws LockingException {
		return false;
	}

	/**
	 * Locking is not supported for BrainLocomotives
	 */
	@Override
	public boolean isLockedByMe(final Locomotive object)
			throws LockingException {
		return true;
	}

	/**
	 * Locking is not supported for BrainLocomotives
	 */
	@Override
	public boolean acquireLock(final Locomotive object) throws LockingException {
		return true;
	}

	/**
	 * Locking is not supported for BrainLocomotives
	 */
	@Override
	public boolean releaseLock(final Locomotive object) throws LockingException {
		return true;
	}

	private String getSpeedCommand(final Locomotive locomotive,
			final int address, final int speed, final boolean[] functions) {
		if (functions.length != 5) {
			throw new LocomotiveException("invalid function count");
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
		for (int i = 1; i < functions.length; i++) {
			stringBuilder.append(functions[i] ? "1" : "0");
			stringBuilder.append(" ");
		}
		return stringBuilder.toString().trim();
	}

	private String getInitCommand(final Locomotive locomotive, final int address) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("XLS ");
		stringBuilder.append(address);
		stringBuilder.append(" ");
		if (locomotive.getType().equals(LocomotiveType.DELTA)) {
			stringBuilder.append("mm");
		} else {
			stringBuilder.append("mm2");
		}

		final String initCommand = stringBuilder.toString().trim();
		return initCommand;
	}
}
