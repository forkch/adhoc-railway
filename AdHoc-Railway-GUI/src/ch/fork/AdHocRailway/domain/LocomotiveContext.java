package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;

public interface LocomotiveContext extends ControllerContext {

	public abstract LocomotiveControlface getLocomotiveControl();

	public abstract LocomotiveManager getLocomotiveManager();

}