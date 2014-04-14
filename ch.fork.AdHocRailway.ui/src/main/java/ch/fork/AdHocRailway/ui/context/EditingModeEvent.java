package ch.fork.AdHocRailway.ui.context;

public class EditingModeEvent {

    private final boolean editingMode;

    public EditingModeEvent(final boolean editingMode) {
        this.editingMode = editingMode;
    }

    public boolean isEditingMode() {
        return editingMode;
    }
}
