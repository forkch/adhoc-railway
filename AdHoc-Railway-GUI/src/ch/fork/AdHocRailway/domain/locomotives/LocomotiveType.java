package ch.fork.AdHocRailway.domain.locomotives;

public enum LocomotiveType {

	DELTA(0, 14, 1), DIGITAL(5, 14, 1);

	private final int functionCount;
	private final int drivingSteps;
	private final int stepping;

	LocomotiveType(int functionCount, int drivingSteps, int stepping) {
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
}
