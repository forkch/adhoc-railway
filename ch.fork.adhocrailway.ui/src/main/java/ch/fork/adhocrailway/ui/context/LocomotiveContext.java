package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.controllers.LocomotiveController;
import ch.fork.adhocrailway.manager.LocomotiveManager;
import ch.fork.adhocrailway.ui.RailwayDeviceManager;

import java.io.File;

public interface LocomotiveContext extends ControllerContext {

    @Override
    RailwayDeviceManager getRailwayDeviceManager();

    public abstract LocomotiveController getLocomotiveControl();

    public abstract LocomotiveManager getLocomotiveManager();

    File getPreviousLocoDir();

    void setPreviousLocoDir(File previousLocodir);
}
