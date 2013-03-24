package ch.fork.AdHocRailway.domain.locomotives;

public class LocomotiveFunction implements Comparable<LocomotiveFunction> {

	private final int number;
	private final String description;
	private final boolean isEmergencyBrakeFunction;

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

	public String getShortDescription() {
		return "F" + (number);
	}

	@Override
	public int compareTo(final LocomotiveFunction o) {
		return Integer.valueOf(number)
				.compareTo(Integer.valueOf(o.getNumber()));
	}

}
