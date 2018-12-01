/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.notifications

import android.app.NotificationChannel
import android.content.Context
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT

/** @author Aidan Follestad (@afollestad) */
enum class Channel(
  val id: String,
  val title: Int,
  val description: Int,
  val importance: Int
) {
  Statuses(
      id = "statuses",
      title = R.string.channel_server_status_title,
      description = R.string.channel_server_status_description,
      importance = IMPORTANCE_DEFAULT
  )
}

/** @author Aidan Follestad (@afollestad) */
@RequiresApi(VERSION_CODES.O)
fun Channel.toNotificationChannel(context: Context): NotificationChannel {
  val titleText = context.getString(this.title)
  val descriptionText = context.getString(this.description)
  return NotificationChannel(this.id, titleText, this.importance)
      .apply {
        description = descriptionText
      }
}
