package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locking.LockingException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import de.dermoba.srcp.model.locomotives.MMDeltaLocomotive;
import de.dermoba.srcp.model.locomotives.MMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveChangeListener;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveControl;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveDirection;

public class SRCPLocomotiveControlAdapter implements LocomotiveControlface,
		SRCPLocomotiveChangeListener, SRCPLockChangeListener {
	private static Logger										logger	= Logger
																				.getLogger(SRCPLocomotiveControlAdapter.class);

	private static SRCPLocomotiveControlAdapter					instance;

	private LocomotivePersistenceIface							persistence;
	private Map<Locomotive, SRCPLocomotive>						locomotiveSRCPLocomotiveMap;
	private Map<SRCPLocomotive, Locomotive>						SRCPLocomotiveLocomotiveMap;
	private Map<SRCPLocomotive, List<LocomotiveChangeListener>>	listeners;

	private SRCPLocomotiveControl								locomotiveControl;

	private SRCPLocomotiveControlAdapter() {
		locomotiveControl = SRCPLocomotiveControl.getInstance();
		locomotiveSRCPLocomotiveMap = new HashMap<Locomotive, SRCPLocomotive>();
		SRCPLocomotiveLocomotiveMap = new HashMap<SRCPLocomotive, Locomotive>();
		listeners = new HashMap<SRCPLocomotive, List<LocomotiveChangeListener>>();
	}

	public static SRCPLocomotiveControlAdapter getInstance() {
		if (instance == null)
			instance = new SRCPLocomotiveControlAdapter();
		return instance;
	}

	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.decreaseSpeed(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}

	}

	public void decreaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.decreaseSpeedStep(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}

	}

	public int getCurrentSpeed(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return sLocomotive.getCurrentSpeed();
	}

	public SRCPLocomotiveDirection getDirection(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return sLocomotive.getDirection();
	}

	public boolean[] getFunctions(Locomotive locomotive) {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return sLocomotive.getFunctions();
	}

	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.increaseSpeed(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void increaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.increaseSpeedStep(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void setFunctions(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.setFunctions(sLocomotive, functions);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void setLocomotivePersistence(LocomotivePersistenceIface persistence) {
		this.persistence = persistence;

	}

	public void setSpeed(Locomotive locomotive, int speed, boolean[] functions)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.setSpeed(sLocomotive, speed, functions);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void toggleDirection(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			locomotiveControl.toggleDirection(sLocomotive);
		} catch (SRCPModelException e) {
			throw new LocomotiveException("Locomotive Error", e);
		}
	}

	public void update() {
		locomotiveSRCPLocomotiveMap.clear();
		SRCPLocomotiveLocomotiveMap.clear();
		locomotiveControl.removeLocomotiveChangeListener(this, this);
		for (Locomotive locomotive : persistence.getAllLocomotives()) {
			LocomotiveType type = locomotive.getLocomotiveType();
			SRCPLocomotive sLocomotive = null;
			if (type.getTypeName().equals("DELTA")) {
				sLocomotive = new MMDeltaLocomotive();
			} else if (type.getTypeName().equals("DIGITAL")) {
				sLocomotive = new MMDigitalLocomotive();
			}
			sLocomotive.setBus(locomotive.getBus());
			sLocomotive.setAddress(locomotive.getAddress());

			locomotiveSRCPLocomotiveMap.put(locomotive, sLocomotive);
			SRCPLocomotiveLocomotiveMap.put(sLocomotive, locomotive);
		}
		locomotiveControl.addLocomotiveChangeListener(this, this);

		locomotiveControl.update(SRCPLocomotiveLocomotiveMap.keySet());

	}

	public void setSession(SRCPSession session) {
		locomotiveControl.setSession(session);
	}

	public boolean acquireLock(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			return locomotiveControl.acquireLock(sLocomotive);
		} catch (SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	public boolean isLocked(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
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

	public boolean isLockedByMe(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
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

	public boolean releaseLock(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		try {
			return locomotiveControl.releaseLock(sLocomotive);
		} catch (SRCPLockingException e) {
			throw new LockingException("Locomotive Locked", e);
		} catch (SRCPModelException e) {
			throw new LockingException("Locomotive Error", e);
		}
	}

	public void addLocomotiveChangeListener(Locomotive locomotive,
			LocomotiveChangeListener listener) {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		if (listeners.get(sLocomotive) == null) {
			listeners.put(sLocomotive,
					new ArrayList<LocomotiveChangeListener>());
		}
		listeners.get(sLocomotive).add(listener);
	}

	public void removeAllLocomotiveChangeListener() {
		listeners.clear();
	}

	public void locomotiveChanged(SRCPLocomotive changedLocomotive) {
		informListeners(changedLocomotive);
	}

	public void lockChanged(Object changedLock, boolean locked) {
		SRCPLocomotive changedLocomotive = (SRCPLocomotive) changedLock;
		informListeners(changedLocomotive);
	}

	private void informListeners(SRCPLocomotive changedLocomotive) {
		logger.debug("locomotiveChanged(" + changedLocomotive + ")");
		List<LocomotiveChangeListener> ll = listeners.get(changedLocomotive);
		if (ll == null)
			return;

		Locomotive locomotive = SRCPLocomotiveLocomotiveMap
				.get(changedLocomotive);
		for (LocomotiveChangeListener scl : ll)
			scl.locomotiveChanged(locomotive);
	}

}
