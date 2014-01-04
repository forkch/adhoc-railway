package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.SimulatedMFXLocomotivesHelper;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveDirection;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveHelper;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.locomotives.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fork
 * 
 */
public class SRCPLocomotiveControlAdapter extends LocomotiveController
		implements SRCPLocomotiveChangeListener, SRCPLockChangeListener {
	private final Map<Locomotive, SRCPLocomotive> locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
	private final Map<SRCPLocomotive, Locomotive> SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();

	private final SRCPLocomotiveControl locomotiveControl;

	public SRCPLocomotiveControlAdapter() {
		locomotiveControl = SRCPLocomotiveControl.getInstance();

		reloadConfiguration();
	}

	@Override
	public void setFunction(final Locomotive locomotive,
			final int functionNumber, final boolean state,
			final int deactivationDelay) throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		final boolean[] srcpFunctions = locomotiveControl
				.getFunctions(sLocomotive);

		if (functionNumber >= srcpFunctions.length) {
			return;
		}

		final int srcpFunctionNumber = SimulatedMFXLocomotivesHelper
				.computeMultipartFunctionNumber(locomotive.getType(),
						functionNumber);

		srcpFunctions[srcpFunctionNumber] = state;

		setFunctions(locomotive, srcpFunctions);

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
			locomotiveControl.setSpeed(sLocomotive, speed,
					SimulatedMFXLocomotivesHelper.convertToMultipartFunctions(
							locomotive.getType(), functions));
			locomotive.setCurrentSpeed(speed);
			locomotive.setCurrentFunctions(functions);
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

			final int srcpEmergencyStopFunction = SimulatedMFXLocomotivesHelper
					.computeMultipartFunctionNumber(locomotive.getType(),
							emergencyStopFunction);
			locomotiveControl.emergencyStop(sLocomotive,
					srcpEmergencyStopFunction);
			locomotive.setCurrentSpeed(0);
			locomotive.setCurrentFunctions(locomotive.getCurrentFunctions());
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
			LocomotiveHelper.toggleDirection(locomotive);

		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void reloadConfiguration() {
		SRCPLockControl.getInstance().setLockDuration(0);
	}

	public void setSession(final SRCPSession session) {
		locomotiveControl.addLocomotiveChangeListener(this, this);
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
	public void locomotiveChanged(final SRCPLocomotive changedSRCPLocomotive) {

		final Locomotive locomotive = SRCPLocomotiveLocomotiveMap
				.get(changedSRCPLocomotive);

		locomotive.setCurrentSpeed(changedSRCPLocomotive.getCurrentSpeed());
		switch (changedSRCPLocomotive.getDirection()) {
		case FORWARD:
			locomotive.setCurrentDirection(LocomotiveDirection.FORWARD);
		case REVERSE:
			locomotive.setCurrentDirection(LocomotiveDirection.REVERSE);
		case UNDEF:
			locomotive.setCurrentDirection(LocomotiveDirection.UNDEF);
		}

		final boolean[] newFunctions = changedSRCPLocomotive.getFunctions();

		locomotive.setCurrentFunctions(SimulatedMFXLocomotivesHelper
				.convertFromMultipartFunctions(locomotive.getType(),
						newFunctions));

		informListeners(locomotive);
	}

	@Override
	public void lockChanged(final Object changedLock, final boolean locked) {
		final SRCPLocomotive changedLocomotive = (SRCPLocomotive) changedLock;
		informListeners(SRCPLocomotiveLocomotiveMap.get(changedLocomotive));
	}

	SRCPLocomotive getSrcpLocomotive(final Locomotive locomotive) {
		if (locomotive == null) {
			throw new IllegalArgumentException("locomotive must not be null");
		}
		SRCPLocomotive srcpLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		if (srcpLocomotive == null) {
			srcpLocomotive = createSRCPLocomotive(locomotive);

			locomotiveSRCPLocomotiveMap.put(locomotive, srcpLocomotive);
			SRCPLocomotiveLocomotiveMap.put(srcpLocomotive, locomotive);
		}
		return srcpLocomotive;
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
			final boolean[] srcpFunctions) throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.setFunctions(sLocomotive, srcpFunctions);
			locomotive.setCurrentFunctions(SimulatedMFXLocomotivesHelper
					.convertFromMultipartFunctions(locomotive.getType(),
							srcpFunctions));
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

}
