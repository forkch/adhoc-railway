package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.controllers.PowerController;

public interface PowerContext extends ControllerContext {
    public PowerController getPowerControl();

    public void setActiveBoosterCount(final int activeBoosterCount);
}
