/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.os.Build.VERSION_CODES
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
interface NotificationChannelProvider {

  /** @return null if the device doesn't have Android O. */
  fun create(
    id: String,
    title: String,
    description: String,
    importance: Int
  ): NotificationChannel?
}

/** @author Aidan Follestad (@afollestad) */
class RealNotificationChannelProvider @Inject constructor(
  private val sdkProvider: SdkProvider
) : NotificationChannelProvider {

  @TargetApi(VERSION_CODES.O)
  override fun create(
    id: String,
    title: String,
    description: String,
    importance: Int
  ): NotificationChannel? {
    if (!sdkProvider.hasOreo()) {
      return null
    }
    return NotificationChannel(id, title, importance)
        .apply {
          this.description = description
        }
  }
}
