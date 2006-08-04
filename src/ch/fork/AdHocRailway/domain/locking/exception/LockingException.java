
package ch.fork.AdHocRailway.domain.locking.exception;

public class LockingException extends Exception {
    public LockingException(String msg) {
        super(msg);
    }

    public LockingException(String msg, Exception parent) {
        super(msg, parent);
    }
}
