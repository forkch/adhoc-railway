package ch.fork.AdHocRailway.controllers.impl.brain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.PowerException;
import ch.fork.AdHocRailway.domain.power.Booster;
import ch.fork.AdHocRailway.domain.power.PowerSupply;

public class BrainPowerControlAdapter extends PowerController implements
		BrainListener {

	private final Map<Integer, PowerSupply> supplies = new HashMap<Integer, PowerSupply>();

	private final BrainController brain;

	public BrainPowerControlAdapter(final BrainController brain) {
		this.brain = brain;
	}

	@Override
	public void addOrUpdatePowerSupply(final PowerSupply supply) {
		supplies.put(supply.getBus(), supply);

	}

	@Override
	public void boosterOn(final Booster booster) {
		try {
			brain.write("X! " + booster.getBoosterNumber());
		} catch (final IOException e) {
			throw new PowerException("error turning on booster "
					+ booster.getBoosterNumber(), e);
		}
	}

	@Override
	public void boosterOff(final Booster booster) {
		try {
			brain.write("X. " + booster.getBoosterNumber());
		} catch (final IOException e) {
			throw new PowerException("error turning on booster "
					+ booster.getBoosterNumber(), e);
		}
	}

	@Override
	public void toggleBooster(final Booster booster) {
		switch (booster.getState()) {
		case ACTIVE:
			boosterOff(booster);
			break;
		case INACTIVE:
		case SHORTCUT:
		default:
			boosterOff(booster);
			break;

		}
	}

	@Override
	public void powerOn(final PowerSupply supply) {
		try {
			brain.write("X!");
		} catch (final IOException e) {
			throw new PowerException("error turning on power ", e);
		}
	}

	@Override
	public void powerOff(final PowerSupply supply) {
		try {
			brain.write("X.");
		} catch (final IOException e) {
			throw new PowerException("error turning on power ", e);
		}
	}

	@Override
	public PowerSupply getPowerSupply(final int busNumber) {
		return supplies.get(busNumber);
	}

	@Override
	public void receivedMessage(final String receivedString) {
		System.out.println(receivedString);
	}

}
