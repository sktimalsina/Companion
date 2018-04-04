package com.example.android.locationassistant;

import android.location.Location;
import android.location.LocationManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class LocationParserUtilTest {
    @Test
    public void getLocationFromString() {

    }

    @Test
    public void getLocationBytes() {
        byte[] sut = getBytes();
        assertNotNull(sut);
    }

    private byte[] getBytes() {
        final Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(100);
        location.setLongitude(50);
        location.setTime(1212);
        location.setAccuracy(10);
        location.setElapsedRealtimeNanos(12345);
        location.setTime(6789);

        return LocationParserUtil.getLocationBytes(location);
    }
}