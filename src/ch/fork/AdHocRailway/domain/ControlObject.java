
package ch.fork.AdHocRailway.domain;

import de.dermoba.srcp.client.SRCPSession;


public abstract class ControlObject {

    protected SRCPSession session;
    protected Address[]   addresses;
    protected boolean     initialized  = false;

    public ControlObject(Address[] addresses) {
        this.addresses = addresses;
    }
    public abstract String getDeviceGroup();

    protected SRCPSession getSession() {
        return session;
    }

    protected void setSession(SRCPSession session) {
        this.session = session;
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

}
