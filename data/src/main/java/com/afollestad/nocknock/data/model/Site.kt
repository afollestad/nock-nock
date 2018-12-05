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
package com.afollestad.nocknock.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.utilities.ext.timeString
import com.afollestad.nocknock.utilities.providers.CanNotifyModel
import java.lang.System.currentTimeMillis
import kotlin.math.max

/** @author Aidan Follestad (@afollestad) */
@Entity(tableName = "sites")
data class Site(
  /** The site's unique ID. */
  @PrimaryKey(autoGenerate = true) var id: Long = 0,
  /** The site's user-given name. */
  var name: String,
  /** The URl at which validation attempts are made to. */
  var url: String,
  /** Settings for the site. */
  @Ignore var settings: SiteSettings?,
  /** The last validation attempt result for the site, if any. */
  @Ignore var lastResult: ValidationResult?
) : CanNotifyModel {

  constructor() : this(0, "", "", null, null)

  override fun notiId(): Int = id.toInt()

  override fun notiName(): String = name

  override fun notiTag(): String = url

  fun intervalText(): String {
    requireNotNull(settings) { "Settings not queried." }
    val lastCheck = lastResult?.timestampMs ?: -1
    val checkInterval = settings!!.validationIntervalMs
    val now = System.currentTimeMillis()
    val nextCheck = max(lastCheck, 0) + checkInterval
    return (nextCheck - now).timeString()
  }

  fun withStatus(
    status: Status? = null,
    reason: String? = null,
    timestamp: Long? = null
  ): Site {
    val newLastResult = lastResult?.copy(
        status = status ?: lastResult!!.status,
        reason = reason,
        timestampMs = timestamp ?: lastResult!!.timestampMs
    ) ?: ValidationResult(
        siteId = this.id,
        timestampMs = timestamp ?: currentTimeMillis(),
        status = status ?: WAITING,
        reason = reason
    )
    return this.copy(lastResult = newLastResult)
  }
}
