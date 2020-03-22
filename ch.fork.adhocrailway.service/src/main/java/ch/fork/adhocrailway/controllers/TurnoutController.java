/*------------------------------------------------------------------------
 *
 * copyright : (C) 2008 by Benjamin Mueller
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutControlIface.java 248 2012-12-28 17:08:16Z fork_ch $
 *
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.adhocrailway.controllers;

import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutState;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public abstract class TurnoutController {

    protected final Map<Turnout, Set<TurnoutChangeListener>> listeners = Maps
            .newHashMap();
    protected final Set<TurnoutChangeListener> generalListeners = Sets.newHashSet();

    private TaskExecutor taskExecutor;

    public TurnoutController(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;

    }

    protected void enqueueTask(Runnable runnable) {
        taskExecutor.enqueueTask(runnable);
    }

    protected void aquireRateLock() {
        taskExecutor.aquireRateLock();
    }

    public abstract void toggle(final Turnout turnout);

    public abstract void toggleTest(final Turnout turnout);

    public abstract void setDefaultState(final Turnout turnout);

    public abstract void setStraight(final Turnout turnout);

    public abstract void setCurvedLeft(final Turnout turnout);

    public abstract void setCurvedRight(final Turnout turnout);

    public abstract void setTurnoutWithAddress(final int address,
                                               final TurnoutState state);

    public abstract void reloadConfiguration();

    public void addGeneralTurnoutChangeListener(
            final TurnoutChangeListener listener) {
        generalListeners.add(listener);
    }

    public void addTurnoutChangeListener(final Turnout turnout,
                                         final TurnoutChangeListener listener) {
        Set<TurnoutChangeListener> turnoutChangeListeners = listeners
                .get(turnout);
        if (turnoutChangeListeners == null) {
            turnoutChangeListeners = Sets.newHashSet();
            listeners.put(turnout, turnoutChangeListeners);
        }
        turnoutChangeListeners.add(listener);
    }

    public void removeAllTurnoutChangeListener() {
        listeners.clear();
    }

    public void removeTurnoutChangeListener(final Turnout turnout,
                                            final TurnoutChangeListener listener) {
        final Set<TurnoutChangeListener> listenersForTurnout = listeners
                .get(turnout);
        if (listenersForTurnout != null) {
            listenersForTurnout.remove(listener);
        }
    }

    protected void informListeners(final Turnout turnout) {
        for (final TurnoutChangeListener scl : generalListeners) {
            scl.turnoutChanged(turnout);
        }

        final Set<TurnoutChangeListener> turnoutChangeListeners = listeners
                .get(turnout);
        if (turnoutChangeListeners != null) {
            // concurrent mod exception
            for (final TurnoutChangeListener scl : Lists.newArrayList(turnoutChangeListeners)) {
                scl.turnoutChanged(turnout);
            }
        }
    }

    public void setNonDefaultState(final Turnout turnout) {
        if (turnout.isThreeWay()) {
            // noop
        } else if (turnout.isCutter()) {
            toggle(turnout);
        } else {
            switch (turnout.getDefaultState()) {
                case LEFT:
                case RIGHT:
                    setStraight(turnout);
                    break;
                case STRAIGHT:
                    setCurvedLeft(turnout);
                    break;
                default:
                    break;

            }
        }
    }

    public void removeGeneralTurnoutChangeListener(
            final TurnoutChangeListener listener) {
        generalListeners.remove(listener);
    }


    public abstract TurnoutState getStateFromDevice(Turnout turnout);

    public abstract void setCutterSleepTime(int intValue);
}
