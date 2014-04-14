package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.manager.LocomotiveManager;

import java.io.File;

public interface LocomotiveContext extends ControllerContext {

    public abstract LocomotiveController getLocomotiveControl();

    public abstract LocomotiveManager getLocomotiveManager();

    File getPreviousLocoDir();

    void setPreviousLocoDir(File previousLocodir);
}