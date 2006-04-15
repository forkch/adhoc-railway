/*------------------------------------------------------------------------
 * 
 * <DefaultSwitch.java>  -  <A standard switch>
 * 
 * begin     : Tue Jan  3 21:26:08 CET 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : bm@fork.ch
 * language  : java
 * version   : $Id$
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

package ch.fork.RailControl.domain.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class DefaultSwitch extends Switch {
	private GA ga;
	private int STRAIGHT_PORT = 0;
	private int CURVED_PORT = 1;

	protected enum SwitchState {
		STRAIGHT, CURVED, UNDEF
	};
	protected SwitchState switchState = SwitchState.STRAIGHT;

	public DefaultSwitch(int pNumber, String pDesc) {
		this(pNumber, pDesc, 0, new Address(0, 0));
	}
	public DefaultSwitch(int pNumber, String pDesc, int pBus, Address pAddress) {
		super(pNumber, pDesc, pBus, pAddress);

	}

	public void init(SRCPSession pSession) throws SwitchException {
		try {
			session = pSession;
			ga = new GA(session);
			ga.init(bus, address.getAddress1(), "M");
			// TODO: immediately a get to determine state !!!!
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_SWITCH_LOCKED);
			} else {
				throw new SwitchException(ERR_INIT_FAILED, x);
			}
		}
	}

	protected void toggle() throws SwitchException {
		if (session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
		try {
			switch (switchState) {
				case STRAIGHT :
					ga.set(CURVED_PORT, SWITCH_ACTION, SWITCH_DELAY);
					// FIXME
					switchState = SwitchState.CURVED;
					break;
				case CURVED :
					ga.set(STRAIGHT_PORT, SWITCH_ACTION, SWITCH_DELAY);
					// FIXME
					switchState = SwitchState.STRAIGHT;
					break;
				case UNDEF :
					return;
			}
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_SWITCH_LOCKED);
			} else {
				throw new SwitchException(ERR_TOGGLE_FAILED, x);
			}
		}
	}

	protected boolean switchChanged(Address pAddress, int pActivatedPort) {
		if (address == pAddress) {
			if (pActivatedPort == STRAIGHT_PORT) {
				switchState = SwitchState.STRAIGHT;
			} else if (pActivatedPort == CURVED_PORT) {
				switchState = SwitchState.CURVED;
			} else {
				return false;
			}
		} else {
			// should not happen
			return false;
		}
		return true;
	}
	@Override
	public Image getImage(ImageObserver obs) {
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		g.drawImage(createImageIcon("icons/default_switch.png", "", this)
				.getImage(), 0, 0, obs);
		switch (switchState) {
			case STRAIGHT :
				g.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, obs);
				break;
			case CURVED :
				g.drawImage(createImageIcon("icons/LED_middle_yellow.png", "",
						this).getImage(), 28, 0, obs);
				g.drawImage(createImageIcon("icons/LED_up_white.png", "", this)
						.getImage(), 28, 0, obs);
				break;
			case UNDEF :
				g.drawImage(createImageIcon("icons/LED_up_white.png", "", this)
						.getImage(), 28, 0, obs);
				g.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, obs);
				break;
		}
		g.drawImage(createImageIcon("icons/LED_middle_white.png", "", this)
				.getImage(), 0, 0, obs);
		return img;
	}
	@Override
	protected void setStraight() throws SwitchException {
		try {
			ga.set(STRAIGHT_PORT, SWITCH_ACTION, SWITCH_DELAY);
			// TODO: resolve get
			switchState = SwitchState.STRAIGHT;
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}
	@Override
	protected void setCurvedLeft() throws SwitchException {
		try {
			ga.set(CURVED_PORT, SWITCH_ACTION, SWITCH_DELAY);
			// TODO: resolve get
			switchState = SwitchState.CURVED;
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}
	@Override
	protected void setCurvedRight() throws SwitchException {
		setCurvedLeft();
	}
}
