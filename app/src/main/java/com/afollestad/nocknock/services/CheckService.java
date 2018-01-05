package com.afollestad.nocknock.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.inquiry.Query;
import com.afollestad.nocknock.BuildConfig;
import com.afollestad.nocknock.R;
import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.api.ServerStatus;
import com.afollestad.nocknock.api.ValidationMode;
import com.afollestad.nocknock.ui.MainActivity;
import com.afollestad.nocknock.ui.ViewSiteActivity;
import com.afollestad.nocknock.util.JsUtil;
import com.afollestad.nocknock.util.NetworkUtil;
import java.util.Locale;

/** @author Aidan Follestad (afollestad) */
@SuppressWarnings("CheckResult")
@SuppressLint("VisibleForTests")
public class CheckService extends IntentService {

  public static String ACTION_CHECK_UPDATE = BuildConfig.APPLICATION_ID + ".CHECK_UPDATE";
  public static String ACTION_RUNNING = BuildConfig.APPLICATION_ID + ".CHECK_RUNNING";
  public static String MODEL_ID = "model_id";
  public static String ONLY_WAITING = "only_waiting";
  public static int NOTI_ID = 3456;

  public CheckService() {
    super("NockNockCheckService");
  }

  private static void LOG(String msg, Object... format) {
    if (format != null) {
      msg = String.format(Locale.getDefault(), msg, format);
    }
    Log.v("NockNockService", msg);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Inquiry.newInstance(this, MainActivity.DB_NAME).build();
    isRunning(true);
    Bridge.config().defaultHeader("User-Agent", getString(R.string.app_name) + " (Android)");

    final Query<ServerModel, Integer> query =
        Inquiry.get(this).selectFrom(MainActivity.SITES_TABLE_NAME, ServerModel.class);
    if (intent != null && intent.hasExtra(MODEL_ID)) {
      query.where("_id = ?", intent.getLongExtra(MODEL_ID, -1));
    } else if (intent != null && intent.getBooleanExtra(ONLY_WAITING, false)) {
      query.where("status = ?", ServerStatus.WAITING);
    }
    final ServerModel[] sites = query.all();

    if (sites == null || sites.length == 0) {
      LOG("No sites added to check, service will terminate.");
      isRunning(false);
      stopSelf();
      return;
    }

    LOG("Checking %d sites...", sites.length);
    sendBroadcast(new Intent(ACTION_RUNNING));

    for (ServerModel site : sites) {
      LOG("Updating %s (%s) status to WAITING...", site.name, site.url);
      site.status = ServerStatus.WAITING;
      updateStatus(site);
    }

    if (NetworkUtil.hasInternet(this)) {
      for (ServerModel site : sites) {
        LOG("Checking %s (%s)...", site.name, site.url);
        site.status = ServerStatus.CHECKING;
        site.lastCheck = System.currentTimeMillis();
        updateStatus(site);

        try {
          final Response response =
              Bridge.get(site.url).throwIfNotSuccess().cancellable(false).request().response();

          site.reason = null;
          site.status = ServerStatus.OK;

          if (site.validationMode == ValidationMode.TERM_SEARCH) {
            final String body = response.asString();
            if (body == null || !body.contains(site.validationContent)) {
              site.status = ServerStatus.ERROR;
              site.reason = "Term \"" + site.validationContent + "\" not found in response body.";
            }
          } else if (site.validationMode == ValidationMode.JAVASCRIPT) {
            final String body = response.asString();
            site.reason = JsUtil.exec(site.validationContent, body);
            if (site.reason != null && !site.toString().isEmpty()) site.status = ServerStatus.ERROR;
          }

          if (site.status == ServerStatus.ERROR) showNotification(this, site);
        } catch (BridgeException e) {
          processError(e, site);
        }
        updateStatus(site);
      }
    } else {
      LOG("No internet connection, waiting.");
    }

    isRunning(false);
    LOG("Service is finished!");
  }

  private void processError(BridgeException e, ServerModel site) {
    site.status = ServerStatus.OK;
    site.reason = null;

    switch (e.reason()) {
      case BridgeException.REASON_REQUEST_CANCELLED:
        // Shouldn't happen
        break;
      case BridgeException.REASON_REQUEST_FAILED:
      case BridgeException.REASON_RESPONSE_UNPARSEABLE:
      case BridgeException.REASON_RESPONSE_UNSUCCESSFUL:
      case BridgeException.REASON_RESPONSE_IOERROR:
        //noinspection ConstantConditions
        if (e.response() != null && e.response().code() == 401) {
          // Don't consider 401 unsuccessful here
          site.reason = null;
        } else {
          site.status = ServerStatus.ERROR;
          site.reason = e.getMessage();
        }
        break;
      case BridgeException.REASON_REQUEST_TIMEOUT:
        site.status = ServerStatus.ERROR;
        site.reason = getString(R.string.timeout);
        break;
      case BridgeException.REASON_RESPONSE_VALIDATOR_ERROR:
      case BridgeException.REASON_RESPONSE_VALIDATOR_FALSE:
        // Not used
        break;
    }

    if (site.status != ServerStatus.OK) {
      LOG("%s error: %s", site.name, site.reason);
      showNotification(this, site);
    }
  }

  private void updateStatus(ServerModel site) {
    Inquiry.get(this).update(ServerModel.class).values(new ServerModel[] {site}).run();
    sendBroadcast(new Intent(ACTION_CHECK_UPDATE).putExtra("model", site));
  }

  private void isRunning(boolean running) {
    PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putBoolean("check_service_running", running)
        .apply();
  }

  public static boolean isRunning(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean("check_service_running", false);
  }

  public static void isAppOpen(Context context, boolean open) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putBoolean("is_app_open", open)
        .apply();
  }

  public static boolean isAppOpen(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_app_open", false);
  }

  private static void showNotification(Context context, ServerModel site) {
    if (isAppOpen(context)) {
      // Don't show notifications while the app is open
      return;
    }

    final NotificationManagerCompat nm = NotificationManagerCompat.from(context);
    final PendingIntent openIntent =
        PendingIntent.getActivity(
            context,
            9669,
            new Intent(context, ViewSiteActivity.class)
                .putExtra("model", site)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_CANCEL_CURRENT);
    final Notification noti =
        new NotificationCompat.Builder(context)
            .setContentTitle(site.name)
            .setContentText(context.getString(R.string.something_wrong))
            .setContentIntent(openIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .build();
    nm.notify(site.url, NOTI_ID, noti);
  }

  @Override
  public void onDestroy() {
    try {
      Inquiry.destroy(this);
    } catch (Throwable t2) {
      t2.printStackTrace();
    }
    super.onDestroy();
  }
}
