package com.example.android.companion;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.example.android.bluetoothchat.BluetoothChatService;
import com.example.android.bluetoothchat.Constants;
import com.example.android.bluetoothchat.DeviceListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linuS on 3/25/2018.
 */

public class CompanionManager {

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;
    private static CompanionManager instance;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    /**
     * Listeners for talk states etc
     */
    private List<CompanionTalk> talkers = new ArrayList<>();
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            for (CompanionTalk talk : talkers) {
                                talk.onCompanionConnected();
                            }
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            for (CompanionTalk talk : talkers) {
                                talk.onCompanionConnecting();
                            }
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            for (CompanionTalk talk : talkers) {
                                talk.onCompanionNotConnected();
                            }
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    for (CompanionTalk talk : talkers) {
                        talk.onMessageWrite(writeMessage);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    for (CompanionTalk talk : talkers) {
                        talk.onIncomingMessage(readMessage);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    for (CompanionTalk talk : talkers) {
                        talk.onCompanionNameReceived(msg.getData().getString(Constants.DEVICE_NAME));
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    for (CompanionTalk talk : talkers) {
                        talk.onNewNotification(msg.getData().getString(Constants.TOAST));
                    }
                    break;
            }
        }
    };

    public CompanionManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Notify bluetooth not supported
            for (CompanionTalk talk : talkers) {
                talk.onBluetoothNotSupported();
            }
        }
    }

    // Singleton
    public static CompanionManager getInstance() {
        if (instance == null) {
            instance = new CompanionManager();
        }
        return instance;
    }

    public void start(Activity context) {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableIntent, CompanionManager.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothChatService(context, mHandler);
        }
    }

    public void stop() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    public void restartIfNecessary() {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    public void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

            for (CompanionTalk talk : talkers) {
                talk.startActivity(discoverableIntent);
            }
        }
    }

    public void addTalker(CompanionTalk companionTalk) {
        if (talkers.contains(companionTalk)) {
            return;
        }
        talkers.add(companionTalk);
    }

    public void removeTalker(CompanionTalk talk) {
        talkers.remove(talk);
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(String message, MessageState state) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            state.onError("Companion not connected!");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            state.onSuccess();
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(byte[] message, MessageState state) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            state.onError("Companion not connected!");
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            mChatService.write(message);

            state.onSuccess();
        }
    }
}
