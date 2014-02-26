package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.PowerController;

public interface PowerContext extends ControllerContext {
    public PowerController getPowerControl();

    public void setActiveBoosterCount(final int activeBoosterCount);
}
