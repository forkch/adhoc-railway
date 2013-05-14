package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.domain.power.Booster;
import ch.fork.AdHocRailway.domain.power.PowerSupply;

public interface PowerController {

	public abstract void addOrUpdatePowerSupply(PowerSupply supply);

	public abstract void boosterOn(Booster booster);

	public abstract void boosterOff(Booster booster);

	public abstract void toggleBooster(Booster booster);

	void powerOn(PowerSupply supply);

	public abstract void powerOff(PowerSupply supply);

	public abstract void addPowerChangeListener(PowerChangeListener listener);

	public abstract void removePowerChangeListener(PowerChangeListener listener);

	public abstract void removeAllPowerChangeListener();

	PowerSupply getPowerSupply(int busNumber);

}
