/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.notifications

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationManager
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.notifications.Channel.CheckFailures
import com.afollestad.nocknock.utilities.providers.BitmapProvider
import com.afollestad.nocknock.utilities.providers.IntentProvider
import com.afollestad.nocknock.utilities.providers.RealIntentProvider.Companion.BASE_NOTIFICATION_REQUEST_CODE
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

/** @author Aidan Follestad (@afollestad) */
class RealNockNotificationManager @Inject constructor(
  private val app: Application,
  @AppIconRes private val appIconRes: Int,
  private val stockManager: NotificationManager,
  private val bitmapProvider: BitmapProvider,
  private val stringProvider: StringProvider,
  private val intentProvider: IntentProvider
) : NockNotificationManager {

  companion object {
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

  override fun createChannels() =
    Channel.values().forEach(this::createChannel)

  override fun postStatusNotification(model: ServerModel) {
    if (isAppOpen) {
      // Don't show notifications while the app is open
      log("App is open, status notification for site ${model.id} won't be posted.")
      return
    }

    log("Posting status notification for site ${model.id}...")
    val intent = intentProvider.getPendingIntentForViewSite(model)

    val newNotification = notification(app, CheckFailures) {
      setContentTitle(model.name)
      setContentText(stringProvider.get(R.string.something_wrong))
      setContentIntent(intent)
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

  override fun cancelStatusNotifications() = stockManager.cancelAll()

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

  private fun ServerModel.notificationId() = BASE_NOTIFICATION_REQUEST_CODE + this.id
}
