package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.locomotives.*;

import java.util.HashMap;
import java.util.Map;

public class SRCPLocomotiveControlAdapter extends LocomotiveController
		implements SRCPLocomotiveChangeListener, SRCPLockChangeListener {
	private final Map<Locomotive, SRCPLocomotive> locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
	private final Map<SRCPLocomotive, Locomotive> SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();

	private final SRCPLocomotiveControl locomotiveControl;

	public SRCPLocomotiveControlAdapter() {
		locomotiveControl = SRCPLocomotiveControl.getInstance();

		locomotiveControl.addLocomotiveChangeListener(this, this);
		reloadConfiguration();
	}

	@Override
	public void decreaseSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.decreaseSpeed(sLocomotive);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}

	}

	@Override
	public void increaseSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.increaseSpeed(sLocomotive);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void setFunction(final Locomotive locomotive,
			final int functionNumber, final boolean state,
			final int deactivationDelay) throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		final boolean[] functions = locomotiveControl.getFunctions(sLocomotive);

		if (functionNumber >= functions.length) {
			return;
		}

		final int srcpFunctionNumber = computeHardwareFunctionNumber(
				locomotive, functionNumber);

		functions[srcpFunctionNumber] = state;

		setFunctions(locomotive, functions);

		if (deactivationDelay > 0) {
			startFunctionDeactivationThread(locomotive, functionNumber,
					deactivationDelay);
		}
	}

	@Override
	public void setSpeed(final Locomotive locomotive, final int speed,
			final boolean[] functions) throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.setSpeed(sLocomotive, speed, functions);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void emergencyStop(final Locomotive locomotive)
			throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			final int emergencyStopFunction = locomotive
					.getEmergencyStopFunction();

			final int srcpEmergencyStopFunction = computeHardwareFunctionNumber(
					locomotive, emergencyStopFunction);
			locomotiveControl.emergencyStop(sLocomotive,
					srcpEmergencyStopFunction);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void toggleDirection(final Locomotive locomotive)
			throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.toggleDirection(sLocomotive);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void reloadConfiguration() {
		SRCPLockControl.getInstance().setLockDuration(0);
	}

	@Override
	public void addOrUpdateLocomotive(final Locomotive locomotive) {
		if (locomotive == null) {
			throw new IllegalArgumentException("locomotive must not be null");
		}
		final SRCPLocomotive srcpLocomotive = getSrcpLocomotive(locomotive);
		if (srcpLocomotive != null) {
			SRCPLocomotiveLocomotiveMap.remove(srcpLocomotive);
			locomotiveSRCPLocomotiveMap.remove(locomotive);
		}
		final SRCPLocomotive sLocomotive = createSRCPLocomotive(locomotive);

		locomotiveSRCPLocomotiveMap.put(locomotive, sLocomotive);
		SRCPLocomotiveLocomotiveMap.put(sLocomotive, locomotive);
	}

	public void setSession(final SRCPSession session) {
		locomotiveControl.setSession(session);
	}

	@Override
	public boolean acquireLock(final Locomotive locomotive)
			throws LockingException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			return locomotiveControl.acquireLock(sLocomotive);
		} catch (final SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (final SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public boolean isLocked(final Locomotive locomotive)
			throws LockingException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		if (locomotiveControl.getSession() == null) {
			return false;
		}
		try {
			return locomotiveControl.isLocked(sLocomotive);
		} catch (final SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (final SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public boolean isLockedByMe(final Locomotive locomotive)
			throws LockingException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		if (locomotiveControl.getSession() == null) {
			return false;
		}
		try {
			return locomotiveControl.isLockedByMe(sLocomotive);

		} catch (final SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (final SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public boolean releaseLock(final Locomotive locomotive)
			throws LockingException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			return locomotiveControl.releaseLock(sLocomotive);
		} catch (final SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (final SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public void locomotiveChanged(final SRCPLocomotive changedLocomotive) {

		final Locomotive locomotive = SRCPLocomotiveLocomotiveMap
				.get(changedLocomotive);

		locomotive.setCurrentSpeed(changedLocomotive.getCurrentSpeed());
		switch (changedLocomotive.getDirection()) {
		case FORWARD:

			locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
		case REVERSE:

			locomotive.setCurrentDirection(LocomotiveDirection.REVERSE);
		case UNDEF:

			locomotive.setCurrentDirection(LocomotiveDirection.UNDEF);

		}
		final boolean[] functions = changedLocomotive.getFunctions();

		if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {

			final boolean[] functionsForSimulatedMfx = new boolean[9];

			for (int i = 0; i < 5; i++) {
				functionsForSimulatedMfx[i] = functions[i];
			}

			for (int i = 5; i < 9; i++) {
				functionsForSimulatedMfx[i] = functions[i + 1];
			}
			locomotive.setCurrentFunctions(functionsForSimulatedMfx);
		} else {
			locomotive.setCurrentFunctions(functions);
		}

		informListeners(locomotive);
	}

	@Override
	public void lockChanged(final Object changedLock, final boolean locked) {
		final SRCPLocomotive changedLocomotive = (SRCPLocomotive) changedLock;
		informListeners(SRCPLocomotiveLocomotiveMap.get(changedLocomotive));
	}

	public SRCPLocomotive getSrcpLocomotive(final Locomotive locomotive) {
		return locomotiveSRCPLocomotiveMap.get(locomotive);
	}

	private SRCPLocomotive createSRCPLocomotive(final Locomotive locomotive) {
		final LocomotiveType type = locomotive.getType();
		SRCPLocomotive sLocomotive = null;
		switch (type) {
		case DELTA:
			sLocomotive = new MMDeltaLocomotive();
			break;
		case DIGITAL:
			sLocomotive = new MMDigitalLocomotive();
			break;
		case SIMULATED_MFX:
			final DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = new DoubleMMDigitalLocomotive();
			doubleMMDigitalLocomotive.setAddress2(locomotive.getAddress2());
			sLocomotive = doubleMMDigitalLocomotive;
			break;
		default:
			return null;
		}
		sLocomotive.setBus(locomotive.getBus());
		sLocomotive.setAddress(locomotive.getAddress1());
		return sLocomotive;
	}

	private void setFunctions(final Locomotive locomotive,
			final boolean[] functions) throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.setFunctions(sLocomotive, functions);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

}
