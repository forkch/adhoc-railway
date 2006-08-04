
package ch.fork.AdHocRailway.domain.locking;

import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.ControlObject;
import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.locking.exception.LockingException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.LOCK;

public class LockControl extends Control implements Constants {

    private static LockControl instance = null;
    private LOCK               locker;
    private Map<Address, LOCK> lockedAddressed;

    private LockControl() {
        lockedAddressed = new HashMap<Address, LOCK>();
    }

    public static LockControl getInstance() {
        if (instance == null) {
            instance = new LockControl();
        }
        return instance;
    }

    public void setSession(SRCPSession session) {
        this.session = session;
    }

    public void acquireLock(ControlObject object) throws LockingException {
        try {
            checkControlObject(object);

            for (Address address : object.getAddresses()) {
                LOCK lock = null;
                if (!lockedAddressed.containsKey(address)) {
                    lock = new LOCK(session, address.getBus());
                    lockedAddressed.put(address, lock);
                } else {
                    lockedAddressed.get(address);
                }
                try {
                    lock.set(object.getDeviceGroup(), address.getAddress(),
                        Preferences.getInstance().getIntValue(
                            PreferencesKeys.LOCK_DURATION));
                } catch (SRCPDeviceLockedException e) {
                    lockedAddressed.remove(address);
                    throw new LockingException(ERR_LOCKED, e);
                } catch (SRCPException e) {
                    lockedAddressed.remove(address);
                    throw new LockingException(ERR_FAILED, e);
                }
            }
        } catch (ControlException e1) {
            throw new LockingException(ERR_NOT_CONNECTED, e1);
        }
    }

    public void releaseLock(ControlObject object) throws LockingException {
        for (Address address : object.getAddresses()) {
            LOCK lock = null;
            if (!lockedAddressed.containsKey(address)) {
                lock = new LOCK(session, address.getBus());
                lockedAddressed.put(address, lock);
            } else {
                lockedAddressed.get(address);
            }
            try {
                lock.set(object.getDeviceGroup(), address.getAddress(), 0);
            } catch (SRCPDeviceLockedException e) {
                lockedAddressed.remove(address);
                throw new LockingException(ERR_LOCKED, e);
            } catch (SRCPException e) {
                lockedAddressed.remove(address);
                throw new LockingException(ERR_FAILED, e);
            }
        }
    }

    public boolean hasLock(ControlObject object) {
        for (Address address : object.getAddresses()) {
            if (lockedAddressed.containsKey(address)) {
                return true;
            }
        }
        return false;
    }
}
