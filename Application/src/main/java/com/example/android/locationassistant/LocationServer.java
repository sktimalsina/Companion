package com.example.android.locationassistant;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.companion.CompanionManager;
import com.example.android.companion.MessageState;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by linuS on 4/2/2018.
 */

public class LocationServer {

    private static final String TAG = LocationServer.class.getSimpleName();

    private static final long LOCATION_REFRESH_TIME_MS = 500;
    private static final float LOCATION_REFRESH_DISTANCE_METERS = 0;
    private final CompanionManager companionManager;
    private final Timer timer;
    private final LocationManager locationManager;
    private final FusedLocationProviderClient fusedLocationProviderClient;

    private volatile Location currentLocation;
    private LocationListener locationListener = getLocationListener();

    @SuppressLint("MissingPermission")
    public LocationServer(final CompanionManager companionManager,
                          final LocationManager locationManager,
                          final FusedLocationProviderClient fusedLocationProviderClient) {
        this.companionManager = companionManager;
        this.locationManager = locationManager;
        this.fusedLocationProviderClient = fusedLocationProviderClient;
        setLastLocationListener();
//        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        timer = new Timer();
    }

    @SuppressLint("MissingPermission")
    private void setLastLocationListener() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            currentLocation = location;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        android.util.Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    // Sends current location to companions in frequent interval.
    @SuppressLint("MissingPermission")
    public void startService(final int frequency) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_REFRESH_TIME_MS,
                LOCATION_REFRESH_DISTANCE_METERS,
                locationListener);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendLocationToCompanion(currentLocation);
            }
        }, 0, frequency);
    }

    public void stopService() {
        locationManager.removeUpdates(locationListener);
        timer.cancel();
    }

    private void sendLocationToCompanion(Location location) {
        if (location == null) {
            Log.e(TAG, "Location is null, not sending to companion");
            return;
        }
        companionManager.sendMessage(LocationParserUtil.getLocationBytes(location), new MessageState() {
            @Override
            public void onSuccess() {
                // Report to interested
            }

            @Override
            public void onError(String errorMessage) {
                // Report to interested
            }
        });
    }

    private LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }
}
