/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.notifications

import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT

/** @author Aidan Follestad (@afollestad) */
enum class Channel(
  val id: String,
  val title: Int,
  val description: Int,
  val importance: Int
) {
  CheckFailures(
      id = "check_failures",
      title = R.string.channel_server_check_failures_title,
      description = R.string.channel_server_check_failures_description,
      importance = IMPORTANCE_DEFAULT
  )
}
