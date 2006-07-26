package ch.fork.RailControl.domain.locomotives;

import ch.fork.RailControl.domain.Constants;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveLockedException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;

public abstract class Locomotive implements Constants, Comparable {
    protected String name;

    protected String desc;

    protected int address;

    protected int bus;

    public enum Direction {
        FORWARD, REVERSE, UNDEF
    };

    protected Direction direction = Direction.UNDEF;

    protected final int PROTOCOL_VERSION = 2;

    protected final String PROTOCOL = "M";

    protected final String FORWARD_DIRECTION = "1";

    protected final String REVERSE_DIRECTION = "0";

    protected int drivingSteps;

    protected int currentSpeed;

    protected SRCPSession session;

    private GL gl;

    protected boolean[] functions;

    protected String[] params;

    protected boolean initialized = false;

    protected abstract void increaseSpeedStep() throws LocomotiveException;

    protected abstract void decreaseSpeedStep() throws LocomotiveException;

    public abstract Locomotive clone();

    public Locomotive(String name, int bus, int address, int drivingSteps,
        String desc, int functionCount) {
        this(null, name, bus, address, drivingSteps, desc, functionCount);

    }

    public Locomotive(SRCPSession session, String name, int bus,
        int address, int drivingSteps, String desc, int functionCount) {
        this.session = session;
        this.name = name;
        this.bus = bus;
        this.address = address;
        this.drivingSteps = drivingSteps;
        this.desc = desc;
        params = new String[3];
        params[0] = Integer.toString(PROTOCOL_VERSION);
        params[1] = Integer.toString(drivingSteps);
        params[2] = Integer.toString(functionCount);
        functions = new boolean[] { false, false, false, false, false };
    }

    public void init() throws LocomotiveException {
        try {
            if (session == null) {
                throw new LocomotiveException(ERR_NO_SESSION);
            }
            gl = new GL(session);
            gl.init(bus, address, PROTOCOL, params);
            initialized = true;
        } catch (SRCPException x) {
            if (x instanceof SRCPDeviceLockedException) {
                throw new LocomotiveLockedException(ERR_LOCKED);
            } else {
                throw new LocomotiveException(ERR_INIT_FAILED, x);
            }
        }
    }

    protected void reinit() throws LocomotiveException {
        try {
            if (gl != null) {
                gl.term();
            }
            if (session != null) {
                init();
            }
        } catch (SRCPException e) {
            throw new LocomotiveException(ERR_REINIT_FAILED, e);
        }
    }

    protected void term() throws LocomotiveException {
        try {
            if (gl != null) {
                gl.term();
            }
        } catch (SRCPException e) {
            throw new LocomotiveException(ERR_TERM_FAILED, e);
        }
    }

    protected void setSpeed(int speed) throws LocomotiveException {
        try {
            if (speed < 0
                || speed > drivingSteps) {
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

    protected void increaseSpeed() throws LocomotiveException {
        int newSpeed = currentSpeed + 1;
        if (newSpeed <= drivingSteps) {
            setSpeed(newSpeed);
        }
    }

    protected void decreaseSpeed() throws LocomotiveException {
        int newSpeed = currentSpeed - 1;
        if (newSpeed >= 0) {
            setSpeed(newSpeed);
        }
    }

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

    protected void setFunctions(boolean[] functions)
        throws LocomotiveException {
        this.functions = functions;
        setSpeed(currentSpeed);
    }

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

    protected void locomotiveInitialized(int pBus, int pAddress,
        String protocol, String[] params) {
        gl = new GL(session);
        this.address = pAddress;
        this.bus = pBus;
        gl.setBus(bus);
        gl.setAddress(address);
        initialized = true;
    }

    protected void locomotiveTerminated() {
        gl = null;
        initialized = false;
    }

    public boolean equals(Locomotive l) {
        if (address == l.getAddress()
            && bus == l.getBus()) {
            return true;
        } else {
            return false;
        }
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<Locomotive name=\""
            + name + "\" type=\"" + getType() + "\" bus=\"" + bus
            + "\" address=\"" + address + "\" desc=\"" + desc + "\" />\n");
        return sb.toString();
    }

    public int compareTo(Object o) {
        if (o instanceof Locomotive) {
            Locomotive anotherLocomotive = (Locomotive) o;
            return (name.compareTo(anotherLocomotive.getName()));
        }
        return 0;
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public SRCPSession getSession() {
        return session;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
    }

    public int getDrivingSteps() {
        return drivingSteps;
    }

    public String getName() {
        return name;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public String toString() {
        return name;
    }

    public int getAddress() {
        return address;
    }

    public int getBus() {
        return bus;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

    public void setAddress(int address) throws LocomotiveException {
        this.address = address;
        reinit();
    }

    public void setBus(int bus) {
        this.bus = bus;
    }

    public boolean[] getFunctions() {
        return functions;
    }

}
