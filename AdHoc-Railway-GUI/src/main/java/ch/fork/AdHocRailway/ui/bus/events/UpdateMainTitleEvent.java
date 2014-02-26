package ch.fork.AdHocRailway.ui.bus.events;

public class UpdateMainTitleEvent {

    private final String title;

    public UpdateMainTitleEvent(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
