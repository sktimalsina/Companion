/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;
import com.example.android.companion.CompanionManager;
import com.example.android.companion.CompanionTalk;
import com.example.android.companion.MessageState;
import com.example.android.locationassistant.LocationMocker;
import com.example.android.locationassistant.LocationServer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";
    public static final int MESSAGE_FREQUENCY = 2000;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    private Button mClientButton;
    private Button mServerButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;
    private CompanionTalk companionTalk = new CompanionTalkImpl();
    private CompanionManager companionManager = CompanionManager.getInstance();
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        companionManager.addTalker(companionTalk);
        if (getActivity() != null) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        companionManager.start(getActivity());
        setupChat();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        companionManager.restartIfNecessary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mClientButton = view.findViewById(R.id.button_client);
        mServerButton = view.findViewById(R.id.button_server);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        mClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationMocker mocker = new LocationMocker(locationManager);
                mocker.startMocking();
            }
        });

        mServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationServer server = new LocationServer(companionManager, locationManager, getFusedLocationProviderClient());
                server.startService(MESSAGE_FREQUENCY);
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    // Just for test

    private FusedLocationProviderClient getFusedLocationProviderClient() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return mFusedLocationClient;
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        companionManager.sendMessage(message, new MessageState() {
            @Override
            public void onSuccess() {
                // Reset out string buffer to zero and clear the edit text field
                mOutStringBuffer.setLength(0);
                mOutEditText.setText(mOutStringBuffer);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CompanionManager.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    companionManager.connectDevice(data, true);
                }
                break;
            case CompanionManager.REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    companionManager.connectDevice(data, false);
                }
                break;
            case CompanionManager.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, CompanionManager.REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, CompanionManager.REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                companionManager.ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    private class CompanionTalkImpl implements CompanionTalk {
        @Override
        public void onCompanionConnected() {
            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
            mConversationArrayAdapter.clear();
        }

        @Override
        public void onIncomingMessage(String incomingMessage) {
            mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + incomingMessage);
        }

        @Override
        public void onBluetoothNotSupported() {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        @Override
        public void onCompanionConnecting() {
            setStatus(R.string.title_connecting);
        }

        @Override
        public void onCompanionNotConnected() {
            setStatus(R.string.title_not_connected);
        }

        @Override
        public void onMessageWrite(String writeMessage) {
            mConversationArrayAdapter.add("Me:  " + writeMessage);
        }

        @Override
        public void onCompanionNameReceived(String companionName) {
            mConnectedDeviceName = companionName;
            if (null != getActivity()) {
                Toast.makeText(getActivity(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNewNotification(String notification) {
            if (null != getActivity()) {
                Toast.makeText(getActivity(), notification, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void startActivity(Intent discoverableIntent) {
            FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }

            activity.startActivity(discoverableIntent);
        }
    }
}
