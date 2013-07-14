package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveDirection;

public class BrainLocomotiveControlAdapter extends LocomotiveController {

	@Override
	public boolean isLocked(Locomotive object) throws LockingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLockedByMe(Locomotive object) throws LockingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean acquireLock(Locomotive object) throws LockingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean releaseLock(Locomotive object) throws LockingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void toggleDirection(Locomotive locomotive)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public SRCPLocomotiveDirection getDirection(Locomotive locomotive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCurrentSpeed(Locomotive locomotive) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSpeed(Locomotive locomotive, int speed, boolean[] functions)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFunction(Locomotive locomotive, int functionNumber,
			boolean state, int deactivationDelay) throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean[] getFunctions(Locomotive locomotive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void emergencyStop(Locomotive myLocomotive)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void activateLoco(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivateLoco(Locomotive locomotive)
			throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void emergencyStopActiveLocos() throws LocomotiveException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addOrUpdateLocomotive(Locomotive locomotive) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeLocomotiveChangeListener(Locomotive locomotive,
			LocomotiveChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllLocomotiveChangeListener() {
		// TODO Auto-generated method stub

	}

}
