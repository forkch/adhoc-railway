
package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.LOCK;


public abstract class ControlObject {

    protected SRCPSession session;
    protected LOCK        lock;
    protected boolean     locked      = false;
    protected int         lockedBySession;
    protected int         lockDuration;
    protected Address[]   addresses;
    protected boolean     initialized = false;

    public ControlObject(Address[] addresses) {
        this.addresses = addresses;
    }

    public abstract String getDeviceGroup();

    protected abstract void init() throws ControlException;
    
    protected SRCPSession getSession() {
        return session;
    }

    protected void setSession(SRCPSession session) {
        this.session = session;
        lock = new LOCK(session, addresses[0].getBus());
    }

    public void lockSet(Address addr, int duration, int sessionID) {
        lockDuration = duration;
        lockedBySession = sessionID;
        locked = true;
    }
    
    public void lockTerm(Address addr) {
        lockDuration = 0;
        lockedBySession = 0;
        locked = false;
    }

    public Address[] getAddresses() {
        return addresses;
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public LOCK getLock() {
        return lock;
    }

    public int getLockDuration() {
        return lockDuration;
    }

    public int getLockedBySession() {
        return lockedBySession;
    }

    public boolean isLocked() {
        return locked;
    }
}
