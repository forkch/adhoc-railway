package ch.fork.RailControl.domain.locomotives.exception;

public class LocomotiveException extends Exception {

    public LocomotiveException(String msg) {
        super(msg);
    }

    public LocomotiveException(String msg, Exception parent) {
        super(msg, parent);
    }
}
