package ch.fork.AdHocRailway.domain.exception;

import ch.fork.AdHocRailway.domain.Constants;

public class NoSessionException extends ControlException {

    public NoSessionException() {
        super(Constants.ERR_NO_SESSION + "\nConnect to the SRCP-Server");
    }
}
