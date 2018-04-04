package com.example.android.locationassistant;


import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.util.Log;

import com.example.android.companion.CompanionManager;
import com.example.android.companion.CompanionTalk;


/**
 * Sets the location to provided value.
 */
public class LocationMocker {

    private static final String TAG = LocationMocker.class.getSimpleName();
    private final LocationManager locationManager;

    private CompanionTalk companionTalk = new CompanionTalkImpl();
    private CompanionManager companionManager = CompanionManager.getInstance();

    public LocationMocker(final LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void stopMocking() {
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
        locationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        companionManager.removeTalker(companionTalk);
    }

    public void startMocking() {
        companionManager.addTalker(companionTalk);
        try {
            locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE);

        } catch (SecurityException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private class CompanionTalkImpl implements CompanionTalk {
        @Override
        public void onCompanionConnected() {

        }

        @Override
        public void onIncomingMessage(String incomingMessage) {
            if (locationManager == null) {
                return;
            }
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER,
                    LocationParserUtil.getLocationFromString(incomingMessage));
        }

        @Override
        public void onBluetoothNotSupported() {

        }

        @Override
        public void onCompanionConnecting() {

        }

        @Override
        public void onCompanionNotConnected() {

        }

        @Override
        public void onMessageWrite(String writeMessage) {

        }

        @Override
        public void onCompanionNameReceived(String companionName) {

        }

        @Override
        public void onNewNotification(String string) {

        }

        @Override
        public void startActivity(Intent discoverableIntent) {

        }
    }
}