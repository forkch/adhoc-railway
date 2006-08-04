package ch.fork.AdHocRailway.domain.locking;

import ch.fork.AdHocRailway.domain.ControlObject;

public interface LockChangeListener {

    public void lockChanged(ControlObject changedLock);
}
