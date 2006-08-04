
package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import de.dermoba.srcp.client.SRCPSession;


public abstract class Control {

    protected SRCPSession session = null;


    public void checkControlObject(ControlObject co) throws NoSessionException,
        InvalidAddressException {
        if (co.getSession() == null) {
            throw new NoSessionException();
        }
        for (Address a : co.getAddresses()) {
            if (a.getAddress() == 0) {
                throw new InvalidAddressException();
            }
        }
    }

    public void setSessionOnControlObject(ControlObject co) {
        co.setSession(session);
    }

    protected void initControlObject(ControlObject object)
        throws ControlException {
        if (!object.isInitialized()) {
            object.init();
        }
    }
    
}
