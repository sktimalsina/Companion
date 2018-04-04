package com.example.android.companion;

/**
 * Created by linuS on 3/25/2018.
 */

public interface MessageState {
    void onSuccess();

    void onError(String errorMessage);
}
