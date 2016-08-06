package com.afollestad.nocknock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.afollestad.nocknock.services.CheckService;
import com.afollestad.nocknock.util.NetworkUtil;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final boolean hasInternet = NetworkUtil.hasInternet(context);
        Log.v("ConnectivityReceiver", "Connectivity state changed... has internet? " + hasInternet);
        if (hasInternet) {
            context.startService(new Intent(context, CheckService.class)
                    .putExtra(CheckService.ONLY_WAITING, true));
        }
    }
}
