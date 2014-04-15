package ch.fork.AdHocRailway.model.locomotives;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class LocomotiveFunction implements Comparable<LocomotiveFunction>,
        Serializable {


    @Expose
    private final int number;

    @Expose
    private String description;

    @Expose
    private boolean isEmergencyBrakeFunction;

    @Expose
    private int deactivationDelay;

    public LocomotiveFunction(final int number, final String description,
                              final boolean isEmergencyBrakeFunction, final int deactivationDelay) {
        super();
        this.number = number;
        this.description = description;
        this.isEmergencyBrakeFunction = isEmergencyBrakeFunction;
        this.setDeactivationDelay(deactivationDelay);
    }

    public static SortedSet<LocomotiveFunction> getDeltaFunctions() {
        final LocomotiveFunction fn = new LocomotiveFunction(0, "Licht", false,
                -1);
        return new TreeSet<LocomotiveFunction>(Arrays.asList(fn));
    }

    public static SortedSet<LocomotiveFunction> getDigitalFunctions() {
        final LocomotiveFunction f1 = new LocomotiveFunction(1, "-", false, -1);
        final LocomotiveFunction f2 = new LocomotiveFunction(2, "-", false, -1);
        final LocomotiveFunction f3 = new LocomotiveFunction(3, "-", false, -1);
        final LocomotiveFunction f4 = new LocomotiveFunction(4, "ABV", true, -1);
        final SortedSet<LocomotiveFunction> fns = getDeltaFunctions();
        fns.addAll(Arrays.asList(f1, f2, f3, f4));
        return fns;

    }

    public static SortedSet<LocomotiveFunction> getSimulatedMfxFunctions() {
        final LocomotiveFunction f5 = new LocomotiveFunction(5, "-", false, -1);
        final LocomotiveFunction f6 = new LocomotiveFunction(6, "-", false, -1);
        final LocomotiveFunction f7 = new LocomotiveFunction(7, "-", false, -1);
        final LocomotiveFunction f8 = new LocomotiveFunction(8, "-", false, -1);
        final SortedSet<LocomotiveFunction> fns = getDigitalFunctions();
        fns.addAll(Arrays.asList(f5, f6, f7, f8));
        return fns;
    }

    public static SortedSet<LocomotiveFunction> getFunctionsForType(LocomotiveType type) {
        switch (type) {

            case DELTA:
                return getDeltaFunctions();
            case DIGITAL:
                return getDigitalFunctions();
            case SIMULATED_MFX:
                return getSimulatedMfxFunctions();
            case MFX:
                return getDigitalFunctions();
        }
        return getDeltaFunctions();
    }

    public boolean isEmergencyBrakeFunction() {
        return isEmergencyBrakeFunction;
    }

    public void setEmergencyBrakeFunction(final boolean isEmergencyBrakeFunction) {
        this.isEmergencyBrakeFunction = isEmergencyBrakeFunction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getNumber() {
        return number;
    }

    public String getShortDescription() {
        if (number == 0) {
            return "Fn";
        }
        return "Fn" + (number);
    }

    @Override
    public int compareTo(final LocomotiveFunction o) {
        return Integer.valueOf(number)
                .compareTo(o.getNumber());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SIMPLE_STYLE);
    }

    public int getDeactivationDelay() {
        return deactivationDelay;
    }

    public void setDeactivationDelay(final int deactivationDelay) {
        this.deactivationDelay = deactivationDelay;
    }

}
