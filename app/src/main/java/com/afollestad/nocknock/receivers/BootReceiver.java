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
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            final Inquiry inq = Inquiry.newInstance(context, MainActivity.DB_NAME).build(false);
            ServerModel[] models = inq
                    .selectFrom(MainActivity.SITES_TABLE_NAME, ServerModel.class)
                    .all();
            AlarmUtil.setSiteChecks(context, models);
            inq.destroyInstance();
        }
    }
}
