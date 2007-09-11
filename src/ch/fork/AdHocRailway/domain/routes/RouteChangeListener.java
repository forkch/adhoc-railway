package ch.fork.AdHocRailway.domain.routes;


public interface RouteChangeListener {
    public void nextSwitchRouted();
    public void nextSwitchDerouted();
    public void routeChanged(RouteOld r);
}
