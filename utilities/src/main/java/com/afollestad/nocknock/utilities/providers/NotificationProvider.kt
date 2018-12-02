/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
interface NotificationProvider {

  fun create(
    channelId: String,
    title: String,
    content: String,
    intent: PendingIntent,
    smallIcon: Int,
    largeIcon: Bitmap
  ): Notification
}

/** @author Aidan Follestad (@afollestad) */
class RealNotificationProvider @Inject constructor(
  private val app: Application
) : NotificationProvider {

  override fun create(
    channelId: String,
    title: String,
    content: String,
    intent: PendingIntent,
    smallIcon: Int,
    largeIcon: Bitmap
  ): Notification {
    return NotificationCompat.Builder(app, channelId)
        .setContentTitle(title)
        .setContentText(content)
        .setContentIntent(intent)
        .setSmallIcon(smallIcon)
        .setLargeIcon(largeIcon)
        .setAutoCancel(true)
        .setDefaults(DEFAULT_VIBRATE)
        .build()
  }
}
