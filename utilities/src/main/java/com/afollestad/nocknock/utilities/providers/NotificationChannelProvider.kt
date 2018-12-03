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
