package com.afollestad.nocknock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.afollestad.inquiry.Inquiry;
import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.ui.MainActivity;
import com.afollestad.nocknock.util.AlarmUtil;

/**
 * @author Aidan Follestad (afollestad)
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Inquiry.init(context, MainActivity.DB_NAME, 1);
            ServerModel[] models = Inquiry.get()
                    .selectFrom(MainActivity.SITES_TABLE_NAME, ServerModel.class)
                    .all();
            AlarmUtil.setSiteChecks(context, models);
        }
    }
}
