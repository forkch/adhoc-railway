
package ch.fork.AdHocRailway.domain.locomotives.exception;

import ch.fork.AdHocRailway.domain.exception.ControlException;

public class LocomotiveException extends ControlException {
    public LocomotiveException(String msg) {
        super(msg);
    }

    public LocomotiveException(String msg, Exception parent) {
        super(msg, parent);
    }
}
