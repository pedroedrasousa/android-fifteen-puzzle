package com.pedroedrasousa.fifteenpuzzle;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.pedroedrasousa.engine.EngineApplication;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class FifteenPuzzleApplication extends EngineApplication {

    /**
     * Analytics tracker.
     */
    private Tracker tracker;



    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.string.global_tracker);
        }
        return tracker;
    }
}