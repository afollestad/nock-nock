package com.afollestad.nocknock.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.services.CheckService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AlarmUtil {

    private final static int BASE_RQC = 69;

    public static PendingIntent getSiteIntent(Context context, ServerModel site) {
        return PendingIntent.getService(context,
                BASE_RQC + (int) site.id,
                new Intent(context, CheckService.class)
                        .putExtra(CheckService.MODEL_ID, site.id),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static AlarmManager am(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static void cancelSiteChecks(Context context, ServerModel site) {
        PendingIntent pi = getSiteIntent(context, site);
        am(context).cancel(pi);
    }

    public static void setSiteChecks(Context context, ServerModel site) {
        cancelSiteChecks(context, site);
        if (site.lastCheck <= 0)
            site.lastCheck = System.currentTimeMillis();
        final long nextCheck = site.lastCheck + site.checkInterval;
        final AlarmManager aMgr = am(context);
        final PendingIntent serviceIntent = getSiteIntent(context, site);
        aMgr.setRepeating(AlarmManager.RTC_WAKEUP, nextCheck, site.checkInterval, serviceIntent);
        final SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm:ssa z yyyy", Locale.getDefault());
        Log.d("AlarmUtil", String.format(Locale.getDefault(), "Set site check alarm for %s (%s), next check: %s", site.name, site.url, df.format(new Date(nextCheck))));
    }

    public static void setSiteChecks(Context context, ServerModel[] sites) {
        for (ServerModel site : sites)
            setSiteChecks(context, site);
    }
}