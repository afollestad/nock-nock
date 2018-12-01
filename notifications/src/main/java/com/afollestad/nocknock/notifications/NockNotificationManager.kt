/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.notifications

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.notifications.Channel.Statuses
import com.afollestad.nocknock.utilities.providers.BitmapProvider
import com.afollestad.nocknock.utilities.providers.StringProvider
import com.afollestad.nocknock.utilities.qualifiers.AppIconRes
import com.afollestad.nocknock.utilities.util.hasOreo
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
interface NockNotificationManager {

  fun setIsAppOpen(open: Boolean)

  fun createChannels()

  fun postStatusNotification(model: ServerModel)

  fun cancelStatusNotification(model: ServerModel)

  fun cancelStatusNotifications()
}

/** @author Aidan Follestad (afollestad) */
class RealNockNotificationManager @Inject constructor(
  private val app: Application,
  @AppIconRes private val appIconRes: Int,
  private val stockManager: NotificationManager,
  private val bitmapProvider: BitmapProvider,
  private val stringProvider: StringProvider
) : NockNotificationManager {
  companion object {
    private const val BASE_REQUEST_CODE = 44

    const val KEY_MODEL = "model"

    private fun log(message: String) {
      if (BuildConfig.DEBUG) {
        Log.d("NockNotificationManager", message)
      }
    }
  }

  private var isAppOpen = false

  override fun setIsAppOpen(open: Boolean) {
    this.isAppOpen = open
    log("Is app open? $open")
  }

  override fun createChannels() {
    Channel.values()
        .forEach(this::createChannel)
  }

  override fun postStatusNotification(model: ServerModel) {
    if (isAppOpen) {
      // Don't show notifications while the app is open
      log("App is open, status notification for site ${model.id} won't be posted.")
      return
    }

    log("Posting status notification for site ${model.id}...")
    val viewSiteActivityCls =
      Class.forName("com.afollestad.nocknock.ui.viewsite.ViewSiteActivity")
    val openIntent = Intent(app, viewSiteActivityCls).apply {
      putExtra(KEY_MODEL, model)
      addFlags(FLAG_ACTIVITY_NEW_TASK)
    }
    val openPendingIntent = PendingIntent.getBroadcast(
        app,
        BASE_REQUEST_CODE + model.id,
        openIntent,
        FLAG_CANCEL_CURRENT
    )

    val newNotification = notification(app, Statuses) {
      setContentTitle(model.name)
      setContentText(stringProvider.get(R.string.something_wrong))
      setContentIntent(openPendingIntent)
      setSmallIcon(R.drawable.ic_notification)
      setLargeIcon(bitmapProvider.get(appIconRes))
      setAutoCancel(true)
      setDefaults(DEFAULT_VIBRATE)
    }

    stockManager.notify(model.url, model.notificationId(), newNotification)
    log("Posted status notification for site ${model.notificationId()}.")
  }

  override fun cancelStatusNotification(model: ServerModel) {
    stockManager.cancel(model.notificationId())
    log("Cancelled status notification for site ${model.id}.")
  }

  override fun cancelStatusNotifications() {
    stockManager.cancelAll()
  }

  @TargetApi(VERSION_CODES.O)
  private fun createChannel(channel: Channel) {
    if (!hasOreo()) {
      log("Not running Android O, channels won't be created.")
      return
    }
    val notificationChannel = channel.toNotificationChannel(app)
    stockManager.createNotificationChannel(notificationChannel)
    log("Created notification channel ${channel.id}")
  }

  private fun ServerModel.notificationId() = BASE_REQUEST_CODE + this.id
}
