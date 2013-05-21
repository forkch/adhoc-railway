package ch.fork.AdHocRailway.controllers;

import java.util.HashSet;
import java.util.Set;

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

	public abstract void addOrUpdatePowerSupply(PowerSupply supply);

	public abstract void boosterOn(Booster booster);

	public abstract void boosterOff(Booster booster);

	public abstract void toggleBooster(Booster booster);

	public abstract void powerOn(PowerSupply supply);

	public abstract void powerOff(PowerSupply supply);

	public abstract PowerSupply getPowerSupply(int busNumber);

	public static PowerController createPowerController(
			final RailwayDevice railwayDevice) {
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainPowerControlAdapter(null);
		case SRCP:
			return new SRCPPowerControlAdapter();
		default:
			throw new IllegalArgumentException("unknown railway-device"
					+ railwayDevice);

		}

	}

}
