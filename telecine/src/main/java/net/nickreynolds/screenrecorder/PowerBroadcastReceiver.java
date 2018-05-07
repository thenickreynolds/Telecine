package com.jakewharton.telecine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class PowerBroadcastReceiver extends BroadcastReceiver {

    public interface Listener {
        void onScreenOff();
        void onScreenOn();
    }

    private List<Listener> listeners = new ArrayList<>();
    private boolean wasScreenOn;

    public PowerBroadcastReceiver(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        setState(pm.isScreenOn());
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                setState(false);
                break;
            case Intent.ACTION_SCREEN_ON:
                setState(true);
                break;
        }
    }

    private void setState(boolean isScreenOn) {
        Timber.i("Screen turned %s", isScreenOn ? "on" : "off");

        if (isScreenOn == wasScreenOn) {
            return;
        }

        wasScreenOn = isScreenOn;

        for (Listener listener : listeners) {
            if (isScreenOn) {
                listener.onScreenOn();
            } else {
                listener.onScreenOff();
            }
        }
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        return filter;
    }
}
