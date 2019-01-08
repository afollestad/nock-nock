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
@file:Suppress("unused")

package com.afollestad.nocknock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afollestad.nocknock.utilities.ext.MINUTE
import java.io.Serializable

/**
 * Represents a site's retry policy, or how many times we
 * retry in a certain timespan before considering a site to
 * have a problem.
 *
 * @author Aidan Follestad (@afollestad)
 */
@Entity(tableName = "retry_policies")
data class RetryPolicy(
  /** The [Site] these settings belong to. */
  @PrimaryKey(autoGenerate = false) var siteId: Long = 0,
  /** How many times we want to retry. */
  var count: Int = 0,
  /**
   * In what amount of time (in minutes) we want
   * to perform those retries.
   */
  var minutes: Int = 0,
  /** The timestamp in milliseconds of the last attempt. */
  var lastTryTimestamp: Long = 0,
  /** How many retries we have left before considering the site to have problem. */
  var triesLeft: Int = -1
) : Serializable {

  constructor() : this(0, 0, 0)

  // Say we are trying 6 times in 3 minutes, that means times per minute = 2.
  // Twice per minute means every 30 seconds.
  // 30 seconds = 30 * 1000 or 30,000 milliseconds.
  // 60,000 / 2 = 30,000.
  fun interval(): Long {
    if (count == 0 || minutes == 0) {
      return -1
    }
    val timesPerMinute = count.toFloat() / minutes.toFloat()
    return MINUTE / timesPerMinute.toSafeInt()
  }

  private fun Float.toSafeInt(): Int {
    val intValue = toInt()
    if (intValue == 0) {
      return 1
    }
    return intValue
  }
}
