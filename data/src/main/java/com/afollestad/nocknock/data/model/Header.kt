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
import java.io.Serializable

/**
 * Represents an HTTP header that is sent with a site's validation attempts.
 *
 * @author Aidan Follestad (@afollestad)
 */
@Entity(tableName = "headers")
data class Header(
  /** The header's unique datrabase ID. */
  @PrimaryKey(autoGenerate = true) var id: Long = 0,
  /** The [Site] this header belong to. */
  var siteId: Long = 0,
  /** The header key/name. */
  var key: String = "",
  /** The header value. */
  var value: String = ""
) : Serializable {

  constructor() : this(0, 0, "", "")
}
