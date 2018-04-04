package com.example.android.companion;

import android.content.Intent;

/**
 * Created by linuS on 3/24/2018.
 */

public interface CompanionTalk {
    void onCompanionConnected();

    void onIncomingMessage(String incomingMessage);

    void onBluetoothNotSupported();

    void onCompanionConnecting();

    void onCompanionNotConnected();

    void onMessageWrite(String writeMessage);

    void onCompanionNameReceived(String companionName);

    void onNewNotification(String string);

    void startActivity(Intent discoverableIntent);
}
