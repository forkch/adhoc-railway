package ch.fork.AdHocRailway.model.locomotives;

public enum LocomotiveDirection {
    FORWARD("1"), REVERSE("0"), EMERGENCY_STOP("2"), UNDEF("-1");

    public final String code;


    LocomotiveDirection(String code) {

        this.code = code;
    }
}
