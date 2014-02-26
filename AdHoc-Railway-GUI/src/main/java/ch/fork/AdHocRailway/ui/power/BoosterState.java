package ch.fork.AdHocRailway.ui.power;

import org.apache.commons.lang3.StringUtils;

public enum BoosterState {
    ACTIVE("A"), SHORTCUT("S"), INACTIVE("O");

    private final String srcpState;

    private BoosterState(final String srcpState) {
        this.srcpState = srcpState;

    }

    public String getSrcpState() {
        return srcpState;
    }

    public static boolean isActive(final String srcpState) {
        return StringUtils.equalsIgnoreCase(ACTIVE.getSrcpState(), srcpState);
    }

    public static boolean isInActive(final String srcpState) {
        return StringUtils.equalsIgnoreCase(INACTIVE.getSrcpState(), srcpState);
    }

    public static boolean isShortcut(final String srcpState) {
        return StringUtils.equalsIgnoreCase(SHORTCUT.getSrcpState(), srcpState);
    }
}
