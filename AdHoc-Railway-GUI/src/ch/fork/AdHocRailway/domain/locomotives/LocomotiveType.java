package ch.fork.AdHocRailway.domain.locomotives;

public enum LocomotiveType {

	DELTA("delta", 0, 14, 1), DIGITAL("digital", 5, 14, 1);

	private final int functionCount;
	private final int drivingSteps;
	private final int stepping;
	private final String name;

	LocomotiveType(final String name, final int functionCount,
			final int drivingSteps, final int stepping) {
		this.name = name;
		this.functionCount = functionCount;
		this.drivingSteps = drivingSteps;
		this.stepping = stepping;

	}

	public int getFunctionCount() {
		return functionCount;
	}

	public int getDrivingSteps() {
		return drivingSteps;
	}

	public int getStepping() {
		return stepping;
	}

	public static LocomotiveType fromString(final String string) {
		for (final LocomotiveType lt : values()) {
			if (lt.name().equalsIgnoreCase(string)) {
				return lt;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}
}
