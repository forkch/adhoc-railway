package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locking.LockingException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.locomotives.MMDeltaLocomotive;
import de.dermoba.srcp.model.locomotives.MMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveChangeListener;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveControl;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveDirection;
import de.dermoba.srcp.model.turnouts.DoubleMMDigitalLocomotive;

public class SRCPLocomotiveControlAdapter implements LocomotiveControlface,
		SRCPLocomotiveChangeListener, SRCPLockChangeListener {
	private static Logger logger = Logger
			.getLogger(SRCPLocomotiveControlAdapter.class);

	private static LocomotiveControlface instance;

	private final Map<Locomotive, SRCPLocomotive> locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
	private final Map<SRCPLocomotive, Locomotive> SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();
	private final Map<SRCPLocomotive, List<LocomotiveChangeListener>> listeners = new HashMap<SRCPLocomotive, List<LocomotiveChangeListener>>();

	private final SRCPLocomotiveControl locomotiveControl;

	private final Set<Locomotive> activeLocomotives = new HashSet<Locomotive>();

	private SRCPLocomotiveControlAdapter() {
		locomotiveControl = SRCPLocomotiveControl.getInstance();

		locomotiveControl.addLocomotiveChangeListener(this, this);
		reloadConfiguration();
	}

	public static LocomotiveControlface getInstance() {
		if (instance == null) {
			instance = new SRCPLocomotiveControlAdapter();
		}
		return instance;
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
	public void decreaseSpeedStep(final Locomotive locomotive)
			throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.decreaseSpeedStep(sLocomotive);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}

	}

	@Override
	public int getCurrentSpeed(final Locomotive locomotive) {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		return sLocomotive.getCurrentSpeed();
	}

	@Override
	public SRCPLocomotiveDirection getDirection(final Locomotive locomotive) {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		return sLocomotive.getDirection();
	}

	@Override
	public boolean[] getFunctions(final Locomotive locomotive) {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		return sLocomotive.getFunctions();
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
	public void increaseSpeedStep(final Locomotive locomotive)
			throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.increaseSpeedStep(sLocomotive);
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
		functions[functionNumber] = state;

		setFunctions(locomotive, functions);

		if (deactivationDelay > 0) {
			startFunctionDeactivationThread(locomotive, functionNumber,
					deactivationDelay);
		}
	}

	@Override
	public void setFunctions(final Locomotive locomotive,
			final boolean[] functions) throws LocomotiveException {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.setFunctions(sLocomotive, functions);
		} catch (final SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
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
			locomotiveControl.emergencyStop(sLocomotive, emergencyStopFunction);
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
		SRCPLockControl.getInstance().setLockDuration(
				Preferences.getInstance().getIntValue(
						PreferencesKeys.LOCK_DURATION));
	}

	@Override
	public void addOrUpdateLocomotive(final Locomotive locomotive) {
		final SRCPLocomotive srcpLocomotive = getSrcpLocomotive(locomotive);
		if (srcpLocomotive != null) {
			SRCPLocomotiveLocomotiveMap.remove(srcpLocomotive);
			locomotiveSRCPLocomotiveMap.remove(locomotive);
		}
		final SRCPLocomotive sLocomotive = createSRCPLocomotive(locomotive);

		locomotiveSRCPLocomotiveMap.put(locomotive, sLocomotive);
		SRCPLocomotiveLocomotiveMap.put(sLocomotive, locomotive);
	}

	public SRCPLocomotive createSRCPLocomotive(final Locomotive locomotive) {
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
			break;
		}
		sLocomotive.setBus(locomotive.getBus());
		sLocomotive.setAddress(locomotive.getAddress1());
		return sLocomotive;
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
	public void removeLocomotiveChangeListener(final Locomotive locomotive,
			final LocomotiveChangeListener listener) {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		if (listeners.get(sLocomotive) == null) {
			listeners.put(sLocomotive,
					new ArrayList<LocomotiveChangeListener>());
		}
		listeners.get(sLocomotive).remove(listener);
	}

	@Override
	public void addLocomotiveChangeListener(final Locomotive locomotive,
			final LocomotiveChangeListener listener) {
		final SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		if (listeners.get(sLocomotive) == null) {
			listeners.put(sLocomotive,
					new ArrayList<LocomotiveChangeListener>());
		}
		listeners.get(sLocomotive).add(listener);
	}

	@Override
	public void removeAllLocomotiveChangeListener() {
		listeners.clear();
	}

	@Override
	public void locomotiveChanged(final SRCPLocomotive changedLocomotive) {
		informListeners(changedLocomotive);
	}

	@Override
	public void lockChanged(final Object changedLock, final boolean locked) {
		final SRCPLocomotive changedLocomotive = (SRCPLocomotive) changedLock;
		informListeners(changedLocomotive);
	}

	private void informListeners(final SRCPLocomotive changedLocomotive) {
		logger.debug("locomotiveChanged(" + changedLocomotive + ")");
		final List<LocomotiveChangeListener> ll = getListenersForSRCPLocomotive(changedLocomotive);

		final Locomotive locomotive = SRCPLocomotiveLocomotiveMap
				.get(changedLocomotive);

		for (final LocomotiveChangeListener scl : ll) {
			scl.locomotiveChanged(locomotive);
		}
	}

	private List<LocomotiveChangeListener> getListenersForSRCPLocomotive(
			final SRCPLocomotive changedLocomotive) {
		final List<LocomotiveChangeListener> ll = listeners
				.get(changedLocomotive);
		if (ll == null) {
			return new ArrayList<LocomotiveChangeListener>();
		}
		return ll;
	}

	public SRCPLocomotive getSrcpLocomotive(final Locomotive locomotive) {
		return locomotiveSRCPLocomotiveMap.get(locomotive);
	}

	@Override
	public void activateLoco(final Locomotive locomotive,
			final boolean[] functions) throws LocomotiveException {
		this.activeLocomotives.add(locomotive);
		setSpeed(locomotive, 0, functions);
	}

	@Override
	public void deactivateLoco(final Locomotive locomotive)
			throws LocomotiveException {
		emergencyStop(locomotive);
		this.activeLocomotives.remove(locomotive);
	}

	@Override
	public void emergencyStopActiveLocos() throws LocomotiveException {
		for (final Locomotive locomotive : activeLocomotives) {
			emergencyStop(locomotive);
		}
	}

	private void startFunctionDeactivationThread(final Locomotive locomotive,
			final int functionNumber, final int deactivationDelay) {
		final Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(deactivationDelay * 1000);

					setFunction(locomotive, functionNumber, false, -1);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				} catch (final LocomotiveException e) {
					e.printStackTrace();
				}

			}
		});
		t.start();
	}

}
