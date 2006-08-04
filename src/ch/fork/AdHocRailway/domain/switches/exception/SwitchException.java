
package ch.fork.AdHocRailway.domain.switches.exception;

import ch.fork.AdHocRailway.domain.exception.ControlException;

public class SwitchException extends ControlException {
    public SwitchException(String msg) {
        super(msg);
    }

    public SwitchException(String msg, Exception parent) {
        super(msg, parent);
    }
}
