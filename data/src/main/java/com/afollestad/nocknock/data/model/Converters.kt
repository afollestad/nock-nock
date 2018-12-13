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

import androidx.room.TypeConverter

/** @author Aidan Follestad (@afollestad) */
class Converters {

  @TypeConverter
  fun fromStatus(status: Status): Int {
    return status.value
  }

  @TypeConverter
  fun toStatus(raw: Int): Status {
    return Status.fromValue(raw)
  }

  @TypeConverter
  fun fromValidationMode(mode: ValidationMode): Int {
    return mode.value
  }

  @TypeConverter
  fun toValidationMode(raw: Int): ValidationMode {
    return ValidationMode.fromValue(raw)
  }
}
