package ch.fork.AdHocRailway.domain.locomotives;

public enum LocomotiveType {

	DELTA("delta", "Delta", 0, 14, 1), DIGITAL("digital", "Digital", 5, 14, 1), SIMULATED_MFX(
			"simulated-mfx", "Simulated MFX (2x digital)", 9, 14, 1);

	private final int functionCount;
	private final int drivingSteps;
	private final int stepping;
	private final String id;
	private final String humanName;

	LocomotiveType(final String id, final String humanName,
			final int functionCount, final int drivingSteps, final int stepping) {
		this.id = id;
		this.humanName = humanName;
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
			if (lt.getId().equalsIgnoreCase(string)) {
				return lt;
			}
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public String getHumanName() {
		return humanName;
	}

	@Override
	public String toString() {
		return humanName;
	}

}
