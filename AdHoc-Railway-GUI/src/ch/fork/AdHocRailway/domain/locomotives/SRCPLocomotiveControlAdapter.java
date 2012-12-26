package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class SRCPLocomotiveControlAdapter implements LocomotiveControlface,
		SRCPLocomotiveChangeListener, SRCPLockChangeListener {
	private static Logger logger = Logger
			.getLogger(SRCPLocomotiveControlAdapter.class);

	private static SRCPLocomotiveControlAdapter instance;

	private LocomotiveManager persistence;
	private final Map<Locomotive, SRCPLocomotive> locomotiveSRCPLocomotiveMap;
	private final Map<SRCPLocomotive, Locomotive> SRCPLocomotiveLocomotiveMap;
	private final Map<SRCPLocomotive, List<LocomotiveChangeListener>> listeners;

	private final SRCPLocomotiveControl locomotiveControl;

	private SRCPLocomotiveControlAdapter() {
		locomotiveControl = SRCPLocomotiveControl.getInstance();
		locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
		SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();
		listeners = new HashMap<SRCPLocomotive, List<LocomotiveChangeListener>>();
	}

	public static SRCPLocomotiveControlAdapter getInstance() {
		if (instance == null) {
			instance = new SRCPLocomotiveControlAdapter();
		}
		return instance;
	}

	@Override
	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.decreaseSpeed(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}

	}

	@Override
	public void decreaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.decreaseSpeedStep(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}

	}

	@Override
	public int getCurrentSpeed(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		return sLocomotive.getCurrentSpeed();
	}

	@Override
	public SRCPLocomotiveDirection getDirection(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		return sLocomotive.getDirection();
	}

	@Override
	public boolean[] getFunctions(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		return sLocomotive.getFunctions();
	}

	@Override
	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.increaseSpeed(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void increaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.increaseSpeedStep(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void setFunctions(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.setFunctions(sLocomotive, functions);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void setLocomotivePersistence(LocomotiveManager persistence) {
		this.persistence = persistence;

	}

	@Override
	public void setSpeed(Locomotive locomotive, int speed, boolean[] functions)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.setSpeed(sLocomotive, speed, functions);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void emergencyStop(Locomotive locomotive) throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.emergencyStop(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void toggleDirection(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			locomotiveControl.toggleDirection(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	@Override
	public void update() {
		locomotiveSRCPLocomotiveMap.clear();
		SRCPLocomotiveLocomotiveMap.clear();

		SRCPLockControl.getInstance().setLockDuration(
				Preferences.getInstance().getIntValue(
						PreferencesKeys.LOCK_DURATION));

		locomotiveControl.removeLocomotiveChangeListener(this, this);
		for (Locomotive locomotive : persistence.getAllLocomotives()) {
			addLocomotiveToMapping(locomotive);
		}
		locomotiveControl.addLocomotiveChangeListener(this, this);
		locomotiveControl.update(SRCPLocomotiveLocomotiveMap.keySet());

	}

	public SRCPLocomotive addLocomotiveToMapping(Locomotive locomotive) {
		LocomotiveType type = locomotive.getLocomotiveType();
		SRCPLocomotive sLocomotive = null;
		switch (type) {
		case DELTA:
			sLocomotive = new MMDeltaLocomotive();
			break;
		case DIGITAL:
			sLocomotive = new MMDigitalLocomotive();
			break;
		}
		sLocomotive.setBus(locomotive.getBus());
		sLocomotive.setAddress(locomotive.getAddress());

		locomotiveSRCPLocomotiveMap.put(locomotive, sLocomotive);
		SRCPLocomotiveLocomotiveMap.put(sLocomotive, locomotive);
		return sLocomotive;
	}

	public void setSession(SRCPSession session) {
		locomotiveControl.setSession(session);
	}

	@Override
	public boolean acquireLock(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			return locomotiveControl.acquireLock(sLocomotive);
		} catch (SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public boolean isLocked(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		if (locomotiveControl.getSession() == null) {
			return false;
		}
		try {
			return locomotiveControl.isLocked(sLocomotive);
		} catch (SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public boolean isLockedByMe(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		if (locomotiveControl.getSession() == null) {
			return false;
		}
		try {
			return locomotiveControl.isLockedByMe(sLocomotive);

		} catch (SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public boolean releaseLock(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
		try {
			return locomotiveControl.releaseLock(sLocomotive);
		} catch (SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	@Override
	public void addLocomotiveChangeListener(Locomotive locomotive,
			LocomotiveChangeListener listener) {
		SRCPLocomotive sLocomotive = getSrcpLocomotive(locomotive);
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
	public void locomotiveChanged(SRCPLocomotive changedLocomotive) {
		informListeners(changedLocomotive);
	}

	@Override
	public void lockChanged(Object changedLock, boolean locked) {
		SRCPLocomotive changedLocomotive = (SRCPLocomotive) changedLock;
		informListeners(changedLocomotive);
	}

	private void informListeners(SRCPLocomotive changedLocomotive) {
		logger.debug("locomotiveChanged(" + changedLocomotive + ")");
		List<LocomotiveChangeListener> ll = listeners.get(changedLocomotive);
		if (ll == null) {
			return;
		}

		Locomotive locomotive = SRCPLocomotiveLocomotiveMap
				.get(changedLocomotive);
		for (LocomotiveChangeListener scl : ll) {
			scl.locomotiveChanged(locomotive);
		}
	}

	public SRCPLocomotive getSrcpLocomotive(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		if (sLocomotive == null) {
			logger.info("added locomotive " + locomotive + " to mapping");
			return addLocomotiveToMapping(locomotive);
		}
		return sLocomotive;
	}

}
