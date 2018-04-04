package com.example.android.locationassistant;

import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Talks with 3rd party location services and provides periodic locations.
 */
class MockLocationProvider {

    private Timer timer;
    private Set<LocationUpdate> locationListeners = new HashSet<>();

    public void addListener(LocationUpdate listener) {
        locationListeners.add(listener);
    }

    public void removeListener(LocationUpdate listener) {
        locationListeners.remove(listener);
    }

    // Currently just mocks location, change it to actual mock data received from service etc
    public void mock() {
        final Location location = new Location(LocationManager.GPS_PROVIDER);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                location.setLatitude(Math.random() * 100);
                location.setLongitude(Math.random());
                location.setTime(System.currentTimeMillis());
                location.setAccuracy(10);
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                for (LocationUpdate listener : locationListeners) {
                    listener.onLocationUpdated(location);
                }
            }
        }, 0, 5000);
    }

    public void stopMock() {
        if (timer == null) {
            return;
        }
        timer.cancel();
    }

    public interface LocationUpdate {
        void onLocationUpdated(Location location);
    }
}