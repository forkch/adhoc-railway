package ch.fork.AdHocRailway.controllers.impl.brain;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;

public class BrainLocomotiveControlAdapter extends LocomotiveController {

	private final BrainController brain;

	private final Set<Locomotive> activeLocomotives = Sets.newHashSet();

	public BrainLocomotiveControlAdapter(final BrainController brain) {
		this.brain = brain;
	}

	@Override
	public void toggleDirection(final Locomotive locomotive)
			throws LocomotiveException {

		if (locomotive.getCurrentDirection() == LocomotiveDirection.FORWARD) {
			locomotive.setCurrentDirection(LocomotiveDirection.REVERSE);
		} else {
			locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
		}
		setSpeed(locomotive, locomotive.getCurrentSpeed(),
				locomotive.getCurrentFunctions());
	}

	@Override
	public void setSpeed(final Locomotive locomotive, final int speed,
			final boolean[] functions) throws LocomotiveException {

		initLocomotive(locomotive);

		try {
			if (functions.length != 5) {
				throw new LocomotiveException("invalid function count");
			}
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("XL ");
			stringBuilder.append(locomotive.getAddress1());
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

			brain.write(stringBuilder.toString().trim());
			locomotive.setCurrentSpeed(speed);
			locomotive.setCurrentFunctions(functions);
		} catch (final IOException e) {
			throw new LocomotiveException("error setting speed", e);
		}

	}

	private void initLocomotive(final Locomotive locomotive)
			throws LocomotiveException {
		try {
			if (!activeLocomotives.contains(locomotive)) {
				final StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("XLS ");
				stringBuilder.append(locomotive.getAddress1());
				stringBuilder.append(" ");
				if (locomotive.getType().equals(LocomotiveType.DELTA)) {
					stringBuilder.append("mm");
				} else {
					stringBuilder.append("mm2");
				}

				brain.write(stringBuilder.toString());
				activeLocomotives.add(locomotive);
			}
		} catch (final IOException e) {
			throw new LocomotiveException("error initializing locomotive", e);
		}
	}

	@Override
	public void increaseSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		if (locomotive.getCurrentSpeed() < locomotive.getType()
				.getDrivingSteps()) {
			setSpeed(locomotive, locomotive.getCurrentSpeed() + 1,
					locomotive.getCurrentFunctions());
		}
	}

	@Override
	public void decreaseSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		if (locomotive.getCurrentSpeed() > 0) {
			setSpeed(locomotive, locomotive.getCurrentSpeed() - 1,
					locomotive.getCurrentFunctions());
		}

	}

	@Override
	public void setFunction(final Locomotive locomotive,
			final int functionNumber, final boolean state,
			final int deactivationDelay) throws LocomotiveException {
		// final boolean[] functions = locomotive.getCurrentFunctions();
		//
		// if (functionNumber >= functions.length) {
		// return;
		// }
		//
		// final int srcpFunctionNumber = computeHardwareFunctionNumber(
		// locomotive, functionNumber);
		//
		// functions[srcpFunctionNumber] = state;
		//
		// //setFunctions(locomotive, functions);
		//
		// if (deactivationDelay > 0) {
		// startFunctionDeactivationThread(locomotive, functionNumber,
		// deactivationDelay);
		// }
	}

	@Override
	public void emergencyStop(final Locomotive myLocomotive)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addOrUpdateLocomotive(final Locomotive locomotive) {
		// TODO Auto-generated method stub

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
}
