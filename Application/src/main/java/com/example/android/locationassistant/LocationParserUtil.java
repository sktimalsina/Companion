package com.example.android.locationassistant;

import android.location.Location;
import android.os.Parcel;

/**
 * Does parsing to/from Location to String
 */
class LocationParserUtil {

    public static Location getLocationFromString(String incomingMessage) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(incomingMessage.getBytes(), 0, incomingMessage.getBytes().length);
        parcel.setDataPosition(0);
        return Location.CREATOR.createFromParcel(parcel);
    }

    public static byte[] getLocationBytes(Location location) {
        Parcel parcel = Parcel.obtain();
        location.writeToParcel(parcel, 0);
        return parcel.marshall();
    }
}
