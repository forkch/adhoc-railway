package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import de.dermoba.srcp.client.SRCPSession;

public interface LocomotiveContext {

	public abstract SRCPSession getSession();

	public abstract LocomotiveControlface getLocomotiveControl();

	public abstract LocomotiveManager getLocomotiveManager();

}