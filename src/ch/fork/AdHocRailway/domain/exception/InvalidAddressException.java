package ch.fork.AdHocRailway.domain.exception;

import ch.fork.AdHocRailway.domain.Constants;

public class InvalidAddressException extends ControlException {

    public InvalidAddressException() {
        super(Constants.ERR_INVALID_ADDRESS + "\nEnter a correct address for this device");
    }
    
}
