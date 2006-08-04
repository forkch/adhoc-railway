
package ch.fork.AdHocRailway.domain.switches.exception;

public class SwitchLockedException extends SwitchException {
    public SwitchLockedException(String msg) {
        super(msg);
    }

    public SwitchLockedException(String msg, Exception parent) {
        super(msg, parent);
    }
}
