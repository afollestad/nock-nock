/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.notifications

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

typealias NotificationBuilder = NotificationCompat.Builder
typealias NotificationConstructor = NotificationBuilder.() -> Unit

/** @author Aidan Follestad (afollestad) */
fun notification(
  context: Context,
  channel: Channel,
  builder: NotificationConstructor
): Notification {
  val newNotification = NotificationCompat.Builder(context, channel.id)
  builder(newNotification)
  return newNotification.build()
}
