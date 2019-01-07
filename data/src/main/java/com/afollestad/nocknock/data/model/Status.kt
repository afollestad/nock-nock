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

import com.afollestad.nocknock.data.R.string
import com.afollestad.nocknock.data.model.Status.CHECKING
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.data.model.Status.WAITING

/**
 * Represents the current status of a [Site] - or whether or not the
 * site passed its most recent check.
 *
 * @author Aidan Follestad (@afollestad)
 */
enum class Status(val value: Int) {
  /** The site has not been validated yet, pending the background job. */
  WAITING(1),
  /** The site is currently being validated. */
  CHECKING(2),
  /** The most recent validation attempt passed. */
  OK(3),
  /** The site did not pass a recent validation attempt. */
  ERROR(4);

  companion object {
    fun fromValue(value: Int) = when (value) {
      OK.value -> OK
      WAITING.value -> WAITING
      CHECKING.value -> CHECKING
      ERROR.value -> ERROR
      else -> throw IllegalArgumentException("Unknown status: $value")
    }
  }
}

fun Status.textRes() = when (this) {
  OK -> string.everything_checks_out
  WAITING -> string.waiting
  CHECKING -> string.checking_status
  else -> 0
}

fun Status?.isPending() = this == WAITING || this == CHECKING
