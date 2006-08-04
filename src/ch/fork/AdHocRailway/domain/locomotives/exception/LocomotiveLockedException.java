
package ch.fork.AdHocRailway.domain.locomotives.exception;

public class LocomotiveLockedException extends LocomotiveException {
    public LocomotiveLockedException(String msg) {
        super(msg);
    }

    public LocomotiveLockedException(String msg, Exception parent) {
        super(msg, parent);
    }
}
