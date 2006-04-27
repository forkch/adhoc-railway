package ch.fork.RailControl.domain.locomotives;

import de.dermoba.srcp.client.SRCPSession;

public class DigitalLocomotive extends Locomotive {

	private static final int DRIVING_STEPS = 28;

	public DigitalLocomotive(SRCPSession session, String name, int bus,
			int address, String desc) {
		super(session, name, bus, address, DRIVING_STEPS, desc);
	}
}
