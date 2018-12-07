/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.nocknock.utilities.providers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE

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
class RealNotificationProvider(
  private val context: Context
) : NotificationProvider {

  override fun create(
    channelId: String,
    title: String,
    content: String,
    intent: PendingIntent,
    smallIcon: Int,
    largeIcon: Bitmap
  ): Notification {
    return NotificationCompat.Builder(context, channelId)
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
