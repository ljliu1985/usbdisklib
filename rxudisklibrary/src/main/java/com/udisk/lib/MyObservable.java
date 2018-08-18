package com.udisk.lib;

import android.util.Log;

import java.util.ArrayList;

/**
 * Provides methods for registering or unregistering arbitrary observers in an {@link ArrayList}.
 * <p>
 * This abstract class is intended to be subclassed and specialized to maintain
 * a registry of observers of specific types and dispatch notifications to them.
 */
public abstract class MyObservable<T> {
    /**
     * The list of observers.  An observer can be in the list at most
     * once and will never be null.
     */
    protected final ArrayList<T> mObservers = new ArrayList<T>();

    private String TAG = this.getClass().getSimpleName();

    /**
     * Adds an observer to the list. The observer cannot be null and it must not already
     * be registered.
     *
     * @param observer the observer to register
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException    the observer is already registered
     */
    public void registerObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                Log.i(TAG, "Observer " + observer + " is already registered.");
                return;
            }
            mObservers.add(observer);
        }
    }

    /**
     * Removes a previously registered observer. The observer must not be null and it
     * must already have been registered.
     *
     * @param observer the observer to unregister
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException    the observer is not yet registered
     */
    public void unregisterObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            int index = mObservers.indexOf(observer);
            if (index == -1) {
                Log.i(TAG, "Observer " + observer + " was not registered.");
                return;
            }
            mObservers.remove(index);
        }
    }

    /**
     * Remove all registered observers.
     */
    public void unregisterAll() {
        synchronized (mObservers) {
            mObservers.clear();
        }
    }
}
