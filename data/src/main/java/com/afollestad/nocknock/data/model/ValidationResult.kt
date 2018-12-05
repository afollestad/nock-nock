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
import com.afollestad.nocknock.data.model.Status.OK
import java.io.Serializable

/**
 * Represents the most recent validation result for a [Site].
 *
 * @author Aidan Follestad (@afollestad)
 */
@Entity(tableName = "validation_results")
data class ValidationResult(
  /** The [Site] that this result belongs to. */
  @PrimaryKey(autoGenerate = false) var siteId: Long = 0,
  /** The timestamp in milliseconds at which this attempt was made. */
  var timestampMs: Long,
  /** The result of this validation attempt. */
  var status: Status,
  /** If the attempt was not successful, why it was not successful. */
  var reason: String?
) : Serializable {

  constructor(): this(0, 0, OK, null)
}
