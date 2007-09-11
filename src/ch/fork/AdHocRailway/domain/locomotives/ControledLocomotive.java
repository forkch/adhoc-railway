/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/Locomotive.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:55:14 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
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


package ch.fork.AdHocRailway.domain.locomotives;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveLockedException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;

/** Basic Locomotive.
 * 
 * @author fork
 *
 */
public abstract class ControledLocomotive extends ControlObject implements Constants {

    public enum Direction {
        FORWARD, REVERSE, UNDEF
    };

    protected Direction    direction         = Direction.UNDEF;
    protected final int    PROTOCOL_VERSION  = 2;
    protected final String PROTOCOL          = "M";
    protected final String FORWARD_DIRECTION = "1";
    protected final String REVERSE_DIRECTION = "0";
    protected int          drivingSteps;
    protected int          currentSpeed;
    private GL             gl;
    private SRCPSession	   session;
    protected boolean[]    functions;
    protected String[]     params;
	private Locomotive locomotive;

    /** Creates a new Locomotive.
     * 
     * @param name The name of the Locomotive
     * @param address The address of the Locomotive
     * @param drivingSteps How many driving-steps does the Locomotive has (e.g. 14 by delta)
     * @param desc A description of the Locmotive
     * @param functionCount How many functions are supported
     */
    public ControledLocomotive(Locomotive locomotive, int drivingSteps, int functionCount) {
        super();
        this.locomotive = locomotive;
        this.drivingSteps = drivingSteps;
        params = new String[3];
        params[0] = Integer.toString(PROTOCOL_VERSION);
        params[1] = Integer.toString(drivingSteps);
        params[2] = Integer.toString(functionCount);
        functions = new boolean[] { false, false, false, false, false };
    }
    
    /** Increases the speed of the Locomotive by a defined speed-step.
     * 
     * @throws LocomotiveException
     */
    protected abstract void increaseSpeedStep() throws LocomotiveException;

    /** Decreases the speed of the Locomotive by a defined speed-step.
     * 
     * @throws LocomotiveException
     */
    protected abstract void decreaseSpeedStep() throws LocomotiveException;

    /** Initializes the Locomotive.
     * 
     */
    protected void init() throws LocomotiveException {
        try {
            gl = new GL(session);
            gl.init(locomotive.getBus(), locomotive.getAddress(), PROTOCOL, params);
            initialized = true;
        } catch (SRCPDeviceLockedException x1) {
            throw new LocomotiveLockedException(ERR_LOCKED, x1);
        } catch (SRCPException x) {
            throw new LocomotiveException(ERR_INIT_FAILED, x);
        }
    }

    /** Reinitializes the Locomotive
     * 
     * @throws LocomotiveException
     */
    protected void reinit() throws LocomotiveException {
        term();
        init();
    }

    /** Terminates the Locomotive
     * 
     * @throws LocomotiveException
     */
    protected void term() throws LocomotiveException {
        try {
            if (gl != null) {
                gl.term();
            }
        } catch (SRCPDeviceLockedException x1) {
            throw new LocomotiveLockedException(ERR_LOCKED, x1);
        } catch (SRCPException e) {
            throw new LocomotiveException(ERR_TERM_FAILED, e);
        }
    }

    /** Sets the speed of the Locomotive
     * 
     * @param speed
     * @throws LocomotiveException
     */
    protected void setSpeed(int speed) throws LocomotiveException {
        try {
            if (speed < 0 || speed > drivingSteps) {
                return;
            }
            switch (direction) {
            case FORWARD:
                gl.set(FORWARD_DIRECTION, speed, drivingSteps, functions);
                break;
            case REVERSE:
                gl.set(REVERSE_DIRECTION, speed, drivingSteps, functions);
                break;
            case UNDEF:
                gl.set(FORWARD_DIRECTION, speed, drivingSteps, functions);
                direction = Direction.FORWARD;
                break;
            }
            currentSpeed = speed;
            // gl.get();
        } catch (SRCPException x) {
            if (x instanceof SRCPDeviceLockedException) {
                throw new LocomotiveLockedException(ERR_LOCKED);
            } else {
                throw new LocomotiveException(ERR_FAILED, x);
            }
        }
    }

    /** Increases speed by one drivingStep
     * 
     * @throws LocomotiveException
     */
    protected void increaseSpeed() throws LocomotiveException {
        int newSpeed = currentSpeed + 1;
        if (newSpeed <= drivingSteps) {
            setSpeed(newSpeed);
        }
    }
    
    /** Decreases speed by one drivingStep
     * 
     * @throws LocomotiveException
     */
    protected void decreaseSpeed() throws LocomotiveException {
        int newSpeed = currentSpeed - 1;
        if (newSpeed >= 0) {
            setSpeed(newSpeed);
        }
    }

    /** Toggles the direction, only applied by the next speed-command.
     * 
     */
    protected void toggleDirection() {
        switch (this.direction) {
        case FORWARD:
            direction = Direction.REVERSE;
            break;
        case REVERSE:
            direction = Direction.FORWARD;
            break;
        }
    }

    /** Sets the functions on or off.
     * 
     * @param functions
     * @throws LocomotiveException
     */
    protected void setFunctions(boolean[] functions) throws LocomotiveException {
    	try {
    		switch (direction) {
    		case FORWARD:
    			gl.set(FORWARD_DIRECTION, currentSpeed, drivingSteps, functions);
            	break;
    		case REVERSE:
    			gl.set(REVERSE_DIRECTION, currentSpeed, drivingSteps, functions);
    			break;
    		case UNDEF:
    			gl.set(FORWARD_DIRECTION, currentSpeed, drivingSteps, functions);
    			direction = Direction.FORWARD;
    			break;
    		}
    	} catch (SRCPException x) {
    		if (x instanceof SRCPDeviceLockedException) {
    			throw new LocomotiveLockedException(ERR_LOCKED);
    		} else {
    			throw new LocomotiveException(ERR_FAILED, x);
    		}
    	}
    	this.functions = functions;
        setSpeed(currentSpeed);
    }

    /** Another client has changed the Locomotive.
     * 
     * @param pDrivemode
     * @param v
     * @param vMax
     * @param functions
     */
    protected void locomotiveChanged(String pDrivemode, int v, int vMax,
        boolean[] functions) {
        if (pDrivemode.equals(FORWARD_DIRECTION)) {
            direction = Direction.FORWARD;
        } else if (pDrivemode.equals(REVERSE_DIRECTION)) {
            direction = Direction.REVERSE;
        }
        currentSpeed = v;
        this.functions = functions;
    }

    /** Another client has initialized the Locomotive.
     * 
     * @param pAddress
     * @param protocol
     * @param params
     */
    protected void locomotiveInitialized(Address pAddress, String protocol,
        String[] params) {
        gl = new GL(session);
        gl.setBus(locomotive.getBus());
        gl.setAddress(locomotive.getAddress());
        initialized = true;
    }

    /** Another client has terminated the Locomotive.
     * 
     *
     */
    protected void locomotiveTerminated() {
        gl = null;
        initialized = false;
    }
    
    public int getDrivingSteps() {
        return drivingSteps;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean[] getFunctions() {
        return functions;
    }

    public String getDeviceGroup() {
        return "GL";
    }

}
