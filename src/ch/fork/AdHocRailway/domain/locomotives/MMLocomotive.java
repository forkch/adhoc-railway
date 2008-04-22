package ch.fork.AdHocRailway.domain.locomotives;

public class MMLocomotive extends SRCPLocomotive {

	public static final String	PROTOCOL_VERSION	= "2";

	public static final String	PROTOCOL			= "M";

	public MMLocomotive() {
		params = new String[3];
		params[0] = PROTOCOL_VERSION;

		protocol = PROTOCOL;
	}
}
