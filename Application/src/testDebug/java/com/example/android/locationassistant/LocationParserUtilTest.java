package com.example.android.locationassistant;

import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import com.example.android.common.protocol.MessageType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class LocationParserUtilTest {
    @Test
    public void getLocationFromString() {
        String message = MessageType.LOCATION.toString();
        message += "," + 123.33;
        message += "," + 456.78;
        message += "," + 910.1;
        message += "," + 20;
        message += "," + 1234567890;
        message += "," + 1011121314;

        assertNotNull(LocationParserUtil.getLocationFromString(message));
    }

    @Test
    public void getStringFromLocation() {
        final Location location = getLocation();
        assertNotNull(LocationParserUtil.getStringFromLocation(location));
    }

    @NonNull
    private Location getLocation() {
        final Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(123.33);
        location.setLongitude(456.78);
        location.setAltitude(910.1);
        location.setAccuracy(20);
        location.setTime(1234567890);
        location.setElapsedRealtimeNanos(1011121314);
        return location;
    }
}