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
