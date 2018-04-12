package com.example.android.locationassistant;

import android.location.Location;
import android.location.LocationManager;

import com.example.android.common.protocol.MessageType;

/**
 * Does parsing to/from Location to String
 * Todo, change into parcel
 */
class LocationParserUtil {

    public static Location getLocationFromString(String incomingMessage) {
        String[] messageArray = incomingMessage.split(",");
        if (messageArray[0] == null
                || !messageArray[0].equals(MessageType.LOCATION.toString())) {
            return null;
        }

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(Double.valueOf(messageArray[1]));
        location.setLongitude(Double.valueOf(messageArray[2]));
        location.setAltitude(Double.valueOf(messageArray[3]));
        location.setAccuracy(Float.valueOf(messageArray[4]));
        location.setTime(Long.valueOf(messageArray[5]));
        location.setElapsedRealtimeNanos(Long.valueOf(messageArray[6]));
        return location;
    }

    public static String getStringFromLocation(Location location) {
        String message = MessageType.LOCATION.toString();
        message += "," + location.getLatitude();
        message += "," + location.getLongitude();
        message += "," + location.getAltitude();
        message += "," + location.getAccuracy();
        message += "," + location.getTime();
        message += "," + location.getElapsedRealtimeNanos();

        return message;
    }
}
