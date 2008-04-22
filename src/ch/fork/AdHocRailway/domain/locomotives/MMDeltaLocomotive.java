package ch.fork.AdHocRailway.domain.locomotives;

public class MMDeltaLocomotive extends MMLocomotive {

	public final static int 	DRIVING_STEPS	= 14;
	public final static int	FUNCTION_COUNT	= 4;

	public MMDeltaLocomotive() {
		params[1] = "" + DRIVING_STEPS;
		params[2] = "" + FUNCTION_COUNT;
		functions = new boolean[FUNCTION_COUNT];
		drivingSteps = DRIVING_STEPS;
	}

}
