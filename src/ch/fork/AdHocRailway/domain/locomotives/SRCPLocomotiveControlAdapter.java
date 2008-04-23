package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locking.LockingException;
import de.dermoba.srcp.client.SRCPSession;

public class SRCPLocomotiveControlAdapter implements LocomotiveControlface,
		SRCPLocomotiveChangeListener {
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
		locomotiveControl.decreaseSpeed(sLocomotive);
	}

	public void decreaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		locomotiveControl.decreaseSpeedStep(sLocomotive);

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
		locomotiveControl.increaseSpeed(sLocomotive);
	}

	public void increaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		locomotiveControl.increaseSpeedStep(sLocomotive);
	}

	public void setFunctions(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		locomotiveControl.setFunctions(sLocomotive, functions);
	}

	public void setLocomotivePersistence(LocomotivePersistenceIface persistence) {
		this.persistence = persistence;

	}

	public void setSpeed(Locomotive locomotive, int speed, boolean[] functions)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		locomotiveControl.setSpeed(sLocomotive, speed, functions);
	}

	public void toggleDirection(Locomotive locomotive)
			throws LocomotiveException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		locomotiveControl.toggleDirection(sLocomotive);
	}

	public void update() {
		locomotiveSRCPLocomotiveMap.clear();
		SRCPLocomotiveLocomotiveMap.clear();
		locomotiveControl.removeLocomotiveChangeListener(this);
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
		locomotiveControl.addLocomotiveChangeListener(this);

		locomotiveControl.update(SRCPLocomotiveLocomotiveMap.keySet());

	}

	public void setSession(SRCPSession session) {
		locomotiveControl.setSession(session);
	}

	public boolean acquireLock(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return locomotiveControl.acquireLock(sLocomotive);
	}

	public boolean isLocked(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return locomotiveControl.isLocked(sLocomotive);
	}

	public boolean isLockedByMe(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return locomotiveControl.isLockedByMe(sLocomotive);
	}

	public boolean releaseLock(Locomotive locomotive) throws LockingException {
		SRCPLocomotive sLocomotive = locomotiveSRCPLocomotiveMap
				.get(locomotive);
		return locomotiveControl.releaseLock(sLocomotive);
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

	private void informListeners(SRCPLocomotive changedLocomotive) {
		List<LocomotiveChangeListener> ll = listeners.get(changedLocomotive);
		if (ll == null)
			return;

		Locomotive locomotive = SRCPLocomotiveLocomotiveMap
				.get(changedLocomotive);
		for (LocomotiveChangeListener scl : ll)
			scl.locomotiveChanged(locomotive);
		logger.debug("locomotiveChanged(" + changedLocomotive + ")");

	}

}
