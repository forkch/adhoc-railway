
package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import de.dermoba.srcp.client.SRCPSession;


public class Control {

    protected SRCPSession session = null; 
    protected Control() {}
    public void checkControlObject(ControlObject co) throws ControlException {
        if (co.getSession() == null) {
            throw new ControlException(Constants.ERR_NO_SESSION);
        }
        for (Address a : co.getAddresses()) {
            if (a.getAddress() == 0) {
                throw new ControlException(Constants.ERR_INVALID_ADDRESS);
            }
        }
    }    
    
    public void setSessionOnControlObject(ControlObject co) {
        co.setSession(session);
    }
}
