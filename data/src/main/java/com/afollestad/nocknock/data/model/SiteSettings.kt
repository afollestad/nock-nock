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
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import java.io.Serializable

/**
 * Represents the current user configuration for a [Site].
 *
 * @author Aidan Follestad (@afollestad)
 */
@Entity(tableName = "site_settings")
data class SiteSettings(
  /** The [Site] these settings belong to. */
  @PrimaryKey(autoGenerate = false) var siteId: Long = 0,
  /** How often a validation attempt is made, in milliseconds. */
  var validationIntervalMs: Long,
  /** The method of which is used to validate the [Site]. */
  var validationMode: ValidationMode,
  /** Args that are used for the [ValidationMode], e.g. a search term. */
  var validationArgs: String?,
  /** Whether or not the [Site] is enabled for automatic periodic checks. */
  var disabled: Boolean,
  /** The network response timeout for validation attempts. */
  var networkTimeout: Int
) : Serializable {

  constructor() : this(0, 0, STATUS_CODE, null, false, 0)
}
