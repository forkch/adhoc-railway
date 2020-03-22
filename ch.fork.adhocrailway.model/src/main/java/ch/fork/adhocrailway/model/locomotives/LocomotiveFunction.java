package ch.fork.adhocrailway.model.locomotives;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
    private boolean isVisible;

    @Expose
    private int deactivationDelay;

    public LocomotiveFunction(final int number, final String description,
                              final boolean isVisible, final int deactivationDelay) {
        super();
        this.number = number;
        this.description = description;
        this.isVisible = isVisible;
        this.setDeactivationDelay(deactivationDelay);
    }

    public static SortedSet<LocomotiveFunction> getDeltaFunctions() {
        final LocomotiveFunction fn = new LocomotiveFunction(0, "Licht", true,
                -1);
        return new TreeSet<LocomotiveFunction>(Arrays.asList(fn));
    }

    public static SortedSet<LocomotiveFunction> getDigitalFunctions() {
        final LocomotiveFunction f1 = new LocomotiveFunction(1, "", true, -1);
        final LocomotiveFunction f2 = new LocomotiveFunction(2, "", true, -1);
        final LocomotiveFunction f3 = new LocomotiveFunction(3, "", true, -1);
        final LocomotiveFunction f4 = new LocomotiveFunction(4, "", true, -1);
        final SortedSet<LocomotiveFunction> fns = getDeltaFunctions();
        fns.addAll(Arrays.asList(f1, f2, f3, f4));
        return fns;

    }

    public static SortedSet<LocomotiveFunction> getSimulatedMfxFunctions() {
        final LocomotiveFunction f5 = new LocomotiveFunction(5, "", false, -1);
        final LocomotiveFunction f6 = new LocomotiveFunction(6, "", false, -1);
        final LocomotiveFunction f7 = new LocomotiveFunction(7, "", false, -1);
        final LocomotiveFunction f8 = new LocomotiveFunction(8, "", false, -1);
        final SortedSet<LocomotiveFunction> fns = getDigitalFunctions();
        fns.addAll(Arrays.asList(f5, f6, f7, f8));
        return fns;
    }

    public static SortedSet<LocomotiveFunction> getMfxFunctions() {
        final LocomotiveFunction f0 = new LocomotiveFunction(0, "", true, -1);
        final LocomotiveFunction f1 = new LocomotiveFunction(1, "", true, -1);
        final LocomotiveFunction f2 = new LocomotiveFunction(2, "", true, -1);
        final LocomotiveFunction f3 = new LocomotiveFunction(3, "", true, -1);
        final LocomotiveFunction f4 = new LocomotiveFunction(4, "", true, -1);
        final LocomotiveFunction f5 = new LocomotiveFunction(5, "", true, -1);
        final LocomotiveFunction f6 = new LocomotiveFunction(6, "", true, -1);
        final LocomotiveFunction f7 = new LocomotiveFunction(7, "", true, -1);
        final LocomotiveFunction f8 = new LocomotiveFunction(8, "", true, -1);
        final LocomotiveFunction f9 = new LocomotiveFunction(9, "", true, -1);
        final LocomotiveFunction f10 = new LocomotiveFunction(10, "", true, -1);
        final LocomotiveFunction f11 = new LocomotiveFunction(11, "", true, -1);
        final LocomotiveFunction f12 = new LocomotiveFunction(12, "", true, -1);
        final LocomotiveFunction f13 = new LocomotiveFunction(13, "", true, -1);
        final LocomotiveFunction f14 = new LocomotiveFunction(14, "", true, -1);
        final LocomotiveFunction f15 = new LocomotiveFunction(15, "", true, -1);
        final LocomotiveFunction f16 = new LocomotiveFunction(15, "", true, -1);
        return Sets.newTreeSet(Lists.newArrayList(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16));
    }

    public static SortedSet<LocomotiveFunction> getDccFunctions() {
        final LocomotiveFunction f0 = new LocomotiveFunction(0, "-", true, -1);
        final LocomotiveFunction f1 = new LocomotiveFunction(1, "-", true, -1);
        final LocomotiveFunction f2 = new LocomotiveFunction(2, "-", true, -1);
        final LocomotiveFunction f3 = new LocomotiveFunction(3, "-", true, -1);
        final LocomotiveFunction f4 = new LocomotiveFunction(4, "-", true, -1);
        final LocomotiveFunction f5 = new LocomotiveFunction(5, "-", true, -1);
        final LocomotiveFunction f6 = new LocomotiveFunction(6, "-", true, -1);
        final LocomotiveFunction f7 = new LocomotiveFunction(7, "-", true, -1);
        final LocomotiveFunction f8 = new LocomotiveFunction(8, "-", true, -1);
        final LocomotiveFunction f9 = new LocomotiveFunction(9, "-", true, -1);
        final LocomotiveFunction f10 = new LocomotiveFunction(10, "-", true, -1);
        final LocomotiveFunction f11 = new LocomotiveFunction(11, "-", true, -1);
        final LocomotiveFunction f12 = new LocomotiveFunction(12, "-", true, -1);
        return Sets.newTreeSet(Lists.newArrayList(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12));
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

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(final boolean isEmergencyBrakeFunction) {
        this.isVisible = isEmergencyBrakeFunction;
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

    public String getShortDescription(boolean f0ToFn) {
        return "F" + (number);
    }

    @Override
    public int compareTo(final LocomotiveFunction o) {

        if (o == null) {
            return 1;
        }
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
