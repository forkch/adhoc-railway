package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;

public interface LocomotiveContext extends ControllerContext {

	public abstract LocomotiveController getLocomotiveControl();

	public abstract LocomotiveManager getLocomotiveManager();

}