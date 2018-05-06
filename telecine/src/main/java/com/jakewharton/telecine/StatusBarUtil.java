package com.jakewharton.telecine;

import android.os.Build;

public class StatusBarUtil {

    public static boolean canReceiveTouchEventsUnderStatusBar() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }
}
