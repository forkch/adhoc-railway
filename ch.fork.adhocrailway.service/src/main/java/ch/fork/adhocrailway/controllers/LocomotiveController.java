/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotiveControlface.java 297 2013-04-14 20:45:23Z fork_ch $
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

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class LocomotiveController implements
        LockController<Locomotive> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocomotiveController.class);

    private final Map<Locomotive, List<LocomotiveChangeListener>> listeners = new HashMap<Locomotive, List<LocomotiveChangeListener>>();
    private final Set<Locomotive> activeLocomotives = new HashSet<Locomotive>();
    private TaskExecutor taskExecutor;

    public LocomotiveController(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected void enqueueTask(Runnable runnable) {
        taskExecutor.enqueueTask(runnable);
    }

    protected void enqueueEmergencyTask(Runnable runnable) {
        taskExecutor.enqueueEmergencyTask(runnable);
    }

    public void cancelTasks() {
        taskExecutor.cancelTasks();
    }

    protected int pendingTasksCount() {
        return taskExecutor.pendingTaskCount();
    }


    protected void aquireRateLock() {
        taskExecutor.aquireRateLock();
    }

    public abstract void toggleDirection(final Locomotive locomotive);

    public abstract void setSpeed(final Locomotive locomotive, final int speed,
                                  final boolean[] functions);

    public abstract void terminateAllLocomotives();

    public abstract void terminateLocomotive(Locomotive locomotive);

    public abstract void setFunction(final Locomotive locomotive,
                                     final int functionNumber, final boolean state,
                                     final int deactivationDelay);

    public abstract void emergencyStop(final Locomotive locomotive);

    public void activateLoco(final Locomotive locomotive) {
        if (isLocomotiveInactive(locomotive)) {
            this.activeLocomotives.add(locomotive);

            if (locomotive.getCurrentFunctions() == null) {
                locomotive.setCurrentFunctions(new boolean[locomotive
                        .getFunctions().size()]);
            }
            final boolean[] functions = locomotive.getCurrentFunctions();
            final int emergencyStopFunction = locomotive
                    .getEmergencyStopFunctionNumber();
            if (emergencyStopFunction != -1
                    && functions.length > emergencyStopFunction) {
                functions[emergencyStopFunction] = false;
            }
            setSpeed(locomotive, 0, functions);
        }
    }

    public boolean isLocomotiveInactive(final Locomotive locomotive) {
        return !activeLocomotives.contains(locomotive);
    }

    public void deactivateLoco(final Locomotive locomotive) {
        emergencyStop(locomotive);
        this.activeLocomotives.remove(locomotive);
    }

    public void emergencyStopActiveLocos() {
        for (final Locomotive locomotive : activeLocomotives) {
            try {
                emergencyStop(locomotive);
            } catch (Exception e) {
                // moving on
                LOGGER.warn("could not stop loco " + locomotive);
            }
        }
    }

    public void removeLocomotiveChangeListener(final Locomotive locomotive,
                                               final LocomotiveChangeListener listener) {
        if (listeners.get(locomotive) == null) {
            listeners
                    .put(locomotive, new ArrayList<LocomotiveChangeListener>());
        }
        listeners.get(locomotive).remove(listener);
    }

    public void addLocomotiveChangeListener(final Locomotive locomotive,
                                            final LocomotiveChangeListener listener) {
        if (listeners.get(locomotive) == null) {
            listeners
                    .put(locomotive, new ArrayList<LocomotiveChangeListener>());
        }
        listeners.get(locomotive).add(listener);
    }

    public void removeAllLocomotiveChangeListener() {
        listeners.clear();
    }

    protected List<LocomotiveChangeListener> getListenersForLocomotive(
            final Locomotive changedLocomotive) {
        final List<LocomotiveChangeListener> ll = listeners
                .get(changedLocomotive);
        if (ll == null) {
            return new LinkedList<LocomotiveChangeListener>();
        }
        return ll;
    }

    protected void informListeners(final Locomotive changedLocomotive) {
        LOGGER.debug("locomotiveChanged(" + changedLocomotive.getName() + ")");
        final List<LocomotiveChangeListener> ll = getListenersForLocomotive(changedLocomotive);

        // concurrent mod exception
        for (final LocomotiveChangeListener scl : Lists.newArrayList(ll)) {
            scl.locomotiveChanged(changedLocomotive);
        }
    }

    public void increaseSpeed(final Locomotive locomotive, int step) {
        if (locomotive.getCurrentOrTargetSpeed() + step <= locomotive.getType()
                .getDrivingSteps()) {
            locomotive.setTargetSpeed(locomotive.getCurrentOrTargetSpeed() + step);
        } else {
            locomotive.setTargetSpeed(locomotive.getType()
                    .getDrivingSteps());
        }
        setSpeed(locomotive, locomotive.getTargetSpeed(),
                locomotive.getCurrentFunctions());
    }

    public void decreaseSpeed(final Locomotive locomotive, int step) {
        if (locomotive.getCurrentOrTargetSpeed() - step > 0) {
            locomotive.setTargetSpeed(locomotive.getCurrentOrTargetSpeed() - step);
        } else {
            locomotive.setTargetSpeed(0);

        }
        setSpeed(locomotive, locomotive.getTargetSpeed(),
                locomotive.getCurrentFunctions());

    }

    protected void startFunctionDeactivationThread(final Locomotive locomotive,
                                                   final int functionNumber, final int deactivationDelay) {
        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(deactivationDelay * 1000);
                    LOGGER.info("deactivating function (due to delay): "
                            + functionNumber);
                    setFunction(locomotive, functionNumber, false, -1);

                } catch (final InterruptedException e) {
                    e.printStackTrace();
                } catch (final ControllerException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();
    }
}
