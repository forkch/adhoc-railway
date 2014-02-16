package ch.fork.AdHocRailway.controllers;

import java.util.HashSet;
import java.util.Set;

import ch.fork.AdHocRailway.controllers.impl.brain.BrainController;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainPowerControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.domain.power.Booster;
import ch.fork.AdHocRailway.domain.power.PowerSupply;

public abstract class PowerController {

	protected final Set<PowerChangeListener> listeners = new HashSet<PowerChangeListener>();

	public void addPowerChangeListener(final PowerChangeListener listener) {
		listeners.add(listener);
	}

	public void removePowerChangeListener(final PowerChangeListener listener) {
		listeners.remove(listener);
	}

	public void removeAllPowerChangeListener() {
		listeners.clear();
	}

	public abstract void addOrUpdatePowerSupply(final PowerSupply supply);

	public abstract void boosterOn(final Booster booster);

	public abstract void boosterOff(final Booster booster);

	public abstract void toggleBooster(final Booster booster);

	public abstract void powerOn(final PowerSupply supply);

	public abstract void powerOff(final PowerSupply supply);

	public abstract PowerSupply getPowerSupply(final int busNumber);

	public static PowerController createPowerController(
			final RailwayDevice railwayDevice) {
		if (railwayDevice == null) {
			return new NullPowerController();
		}
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainPowerControlAdapter(BrainController.getInstance());
		case SRCP:
			return new SRCPPowerControlAdapter();
		default:
			return new NullPowerController();

		}

	}

	static class NullPowerController extends PowerController {

		@Override
		public void addOrUpdatePowerSupply(final PowerSupply supply) {
			// TODO Auto-generated method stub

		}

		@Override
		public void boosterOn(final Booster booster) {
			// TODO Auto-generated method stub

		}

		@Override
		public void boosterOff(final Booster booster) {
			// TODO Auto-generated method stub

		}

		@Override
		public void toggleBooster(final Booster booster) {
			// TODO Auto-generated method stub

		}

		@Override
		public void powerOn(final PowerSupply supply) {
			// TODO Auto-generated method stub

		}

		@Override
		public void powerOff(final PowerSupply supply) {
			// TODO Auto-generated method stub

		}

		@Override
		public PowerSupply getPowerSupply(final int busNumber) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
