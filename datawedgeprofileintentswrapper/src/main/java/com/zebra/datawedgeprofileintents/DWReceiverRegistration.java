package com.zebra.datawedgeprofileintents;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

/**
 * Registers receivers for broadcasts sent by the DataWedge application.
 * DataWedge is a separate app, so its broadcasts are not system broadcasts and the receiver must be
 * exported (mandatory flag when targeting API 34+, available since API 33).
 */
public final class DWReceiverRegistration {
    private DWReceiverRegistration() {
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag") // flags do not exist before API 33
    public static void registerExported(Context context, BroadcastReceiver receiver, IntentFilter filter, Handler handler)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            context.registerReceiver(receiver, filter, null, handler, Context.RECEIVER_EXPORTED);
        }
        else
        {
            context.registerReceiver(receiver, filter, null, handler);
        }
    }
}
