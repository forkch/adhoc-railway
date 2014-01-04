/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotiveControlface.java 297 2013-04-14 20:45:23Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.controllers.impl.brain.BrainController;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainLocomotiveControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;

import org.apache.log4j.Logger;

import java.util.*;

public abstract class LocomotiveController implements
		LockController<Locomotive> {

	private static final Logger LOGGER = Logger
			.getLogger(LocomotiveController.class);

	private final Map<Locomotive, List<LocomotiveChangeListener>> listeners = new HashMap<Locomotive, List<LocomotiveChangeListener>>();
	private final Set<Locomotive> activeLocomotives = new HashSet<Locomotive>();

	public void activateLoco(final Locomotive locomotive)
			throws LocomotiveException {
		if (!isLocomotiveActive(locomotive)) {
			this.activeLocomotives.add(locomotive);

			if (locomotive.getCurrentFunctions() == null) {
				locomotive.setCurrentFunctions(new boolean[locomotive
						.getFunctions().size()]);
			}
			final boolean[] functions = locomotive.getCurrentFunctions();
			final int emergencyStopFunction = locomotive
					.getEmergencyStopFunction();
			if (emergencyStopFunction != -1
					&& functions.length > emergencyStopFunction) {
				functions[emergencyStopFunction] = false;
			}
			setSpeed(locomotive, 0, functions);
		}
	}

	public boolean isLocomotiveActive(final Locomotive locomotive) {
		return activeLocomotives.contains(locomotive);
	}

	public void deactivateLoco(final Locomotive locomotive)
			throws LocomotiveException {
		emergencyStop(locomotive);
		this.activeLocomotives.remove(locomotive);
	}

	public void emergencyStopActiveLocos() throws LocomotiveException {
		for (final Locomotive locomotive : activeLocomotives) {
			emergencyStop(locomotive);
		}
	}

	public void removeLocomotiveChangeListener(final Locomotive locomotive,
			final LocomotiveChangeListener listener) {
		if (listeners.get(locomotive) == null) {
			listeners
					.put(locomotive, new ArrayList<LocomotiveChangeListener>());
		}
		listeners.get(locomotive).remove(listener);
	}

	public void addLocomotiveChangeListener(final Locomotive locomotive,
			final LocomotiveChangeListener listener) {
		if (listeners.get(locomotive) == null) {
			listeners
					.put(locomotive, new ArrayList<LocomotiveChangeListener>());
		}
		listeners.get(locomotive).add(listener);
	}

	public void removeAllLocomotiveChangeListener() {
		listeners.clear();
	}

	protected List<LocomotiveChangeListener> getListenersForLocomotive(
			final Locomotive changedLocomotive) {
		final List<LocomotiveChangeListener> ll = listeners
				.get(changedLocomotive);
		if (ll == null) {
			return new LinkedList<>();
		}
		return ll;
	}

	protected void informListeners(final Locomotive changedLocomotive) {
		LOGGER.debug("locomotiveChanged(" + changedLocomotive + ")");
		final List<LocomotiveChangeListener> ll = getListenersForLocomotive(changedLocomotive);

		for (final LocomotiveChangeListener scl : ll) {
			scl.locomotiveChanged(changedLocomotive);
		}
	}

	public void increaseSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		if (locomotive.getCurrentSpeed() < locomotive.getType()
				.getDrivingSteps()) {
			setSpeed(locomotive, locomotive.getCurrentSpeed() + 1,
					locomotive.getCurrentFunctions());
		}
	}

	public void decreaseSpeed(final Locomotive locomotive)
			throws LocomotiveException {
		if (locomotive.getCurrentSpeed() > 0) {
			setSpeed(locomotive, locomotive.getCurrentSpeed() - 1,
					locomotive.getCurrentFunctions());
		}

	}

	protected void startFunctionDeactivationThread(final Locomotive locomotive,
			final int functionNumber, final int deactivationDelay) {
		final Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(deactivationDelay * 1000);
					LOGGER.info("deactivating function (due to delay): "
							+ functionNumber);
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

	/**
	 * Toggles the direction of the Locomotive
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void toggleDirection(final Locomotive locomotive)
			throws LocomotiveException;

	/**
	 * Sets the speed of the Locomotive
	 * 
	 * @param locomotive
	 * @param speed
	 * @throws LocomotiveException
	 */
	public abstract void setSpeed(final Locomotive locomotive, final int speed,
			final boolean[] functions) throws LocomotiveException;

	public abstract void setFunction(final Locomotive locomotive,
			final int functionNumber, final boolean state,
			final int deactivationDelay) throws LocomotiveException;

	public abstract void emergencyStop(final Locomotive myLocomotive)
			throws LocomotiveException;

	public static LocomotiveController createLocomotiveController(
			final RailwayDevice railwayDevice) {
		if (railwayDevice == null) {
			return new NullLocomotiveController();
		}
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainLocomotiveControlAdapter(
					BrainController.getInstance());
		case SRCP:
			return new SRCPLocomotiveControlAdapter();
		default:
			return new NullLocomotiveController();

		}
	}

    public void removeLocomotiveChangeListener(LocomotiveChangeListener listener) {
        listeners.entrySet().remove(listener);
    }

    static class NullLocomotiveController extends LocomotiveController {

		@Override
		public boolean isLocked(final Locomotive object)
				throws LockingException {
			return false;
		}

		@Override
		public boolean isLockedByMe(final Locomotive object)
				throws LockingException {
			return true;
		}

		@Override
		public boolean acquireLock(final Locomotive object)
				throws LockingException {
			return true;
		}

		@Override
		public boolean releaseLock(final Locomotive object)
				throws LockingException {
			return true;
		}

		@Override
		public void toggleDirection(final Locomotive locomotive)
				throws LocomotiveException {
            locomotive.setCurrentDirection(locomotive.getToggledDirection());
            informListeners(locomotive);
		}

		@Override
		public void setSpeed(final Locomotive locomotive, final int speed,
				final boolean[] functions) throws LocomotiveException {
            locomotive.setCurrentSpeed(speed);
            locomotive.setCurrentFunctions(functions);
            informListeners(locomotive);

		}

		@Override
		public void setFunction(final Locomotive locomotive,
				final int functionNumber, final boolean state,
				final int deactivationDelay) throws LocomotiveException {
            boolean[] currentFunctions = locomotive.getCurrentFunctions();
            currentFunctions[functionNumber] = state;
            locomotive.setCurrentFunctions(currentFunctions);
            informListeners(locomotive);
		}

		@Override
		public void emergencyStop(final Locomotive locomotive)
				throws LocomotiveException {
            setFunction(locomotive, locomotive.getEmergencyStopFunction(), true, 0);
            setSpeed(locomotive, 0, locomotive.getCurrentFunctions());
            informListeners(locomotive);
		}
	}
}