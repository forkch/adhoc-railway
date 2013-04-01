package ch.fork.AdHocRailway.domain.locomotives;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LocomotiveFunction implements Comparable<LocomotiveFunction> {

	private final int number;

	private String description;
	private boolean isEmergencyBrakeFunction;

	public LocomotiveFunction(final int number, final String description,
			final boolean isEmergencyBrakeFunction) {
		super();
		this.number = number;
		this.description = description;
		this.isEmergencyBrakeFunction = isEmergencyBrakeFunction;
	}

	public boolean isEmergencyBrakeFunction() {
		return isEmergencyBrakeFunction;
	}

	public String getDescription() {
		return description;
	}

	public int getNumber() {
		return number;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setEmergencyBrakeFunction(final boolean isEmergencyBrakeFunction) {
		this.isEmergencyBrakeFunction = isEmergencyBrakeFunction;
	}

	public String getShortDescription() {
		if (number == 0) {
			return "Fn";
		}
		return "Fn" + (number);
	}

	public static SortedSet<LocomotiveFunction> getDeltaFunctions() {
		final LocomotiveFunction fn = new LocomotiveFunction(0, "Licht", false);
		return new TreeSet<LocomotiveFunction>(Arrays.asList(fn));
	}

	public static SortedSet<LocomotiveFunction> getDigitalFunctions() {
		final LocomotiveFunction f1 = new LocomotiveFunction(1, "F1", false);
		final LocomotiveFunction f2 = new LocomotiveFunction(2, "F2", false);
		final LocomotiveFunction f3 = new LocomotiveFunction(3, "F3", false);
		final LocomotiveFunction f4 = new LocomotiveFunction(4, "F4", true);
		final SortedSet<LocomotiveFunction> fns = getDeltaFunctions();
		fns.addAll(Arrays.asList(f1, f2, f3, f4));
		return fns;

	}

	public static SortedSet<LocomotiveFunction> getSimulatedMfxFunctions() {
		final LocomotiveFunction f5 = new LocomotiveFunction(5, "F5", false);
		final LocomotiveFunction f6 = new LocomotiveFunction(6, "F6", false);
		final LocomotiveFunction f7 = new LocomotiveFunction(7, "F7", false);
		final LocomotiveFunction f8 = new LocomotiveFunction(8, "F8", false);
		final SortedSet<LocomotiveFunction> fns = getDigitalFunctions();
		fns.addAll(Arrays.asList(f5, f6, f7, f8));
		return fns;
	}

	@Override
	public int compareTo(final LocomotiveFunction o) {
		return Integer.valueOf(number)
				.compareTo(Integer.valueOf(o.getNumber()));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SIMPLE_STYLE);
	}

}
