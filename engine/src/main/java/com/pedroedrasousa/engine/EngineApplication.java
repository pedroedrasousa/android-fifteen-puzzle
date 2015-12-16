package com.pedroedrasousa.engine;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class EngineApplication extends Application {

    /**
     * Analytics tracker.
     */
    private Tracker tracker;

    /**
     * Google analytics tracker identifier.
     */
    private String trakerID;

    /**
     * AdMob banner ad unit identifier.
     */
    private String bannerAdUnitId;

    /**
     * AdMob interstitial ad unit identifier.
     */
    private String interstitialAdUnitId;

    public void setInterstitialAdUnitId(String interstitialAdUnitId) {
        this.interstitialAdUnitId = interstitialAdUnitId;
    }

    public void setBannerAdUnitId(String bannerAdUnitId) {
        this.bannerAdUnitId = bannerAdUnitId;
    }

    public String getBannerAdUnitId() {
        return bannerAdUnitId;
    }

    public String getInterstitialAdUnitId() {
        return interstitialAdUnitId;
    }

    public void setTrakerID(String trakerID) {
        this.trakerID = trakerID;
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(trakerID);
        }
        return tracker;
    }
}