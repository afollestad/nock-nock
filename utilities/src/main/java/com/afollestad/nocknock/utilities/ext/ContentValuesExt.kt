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
package com.afollestad.nocknock.utilities.ext

import android.content.ContentValues

/**
 * Returns a [ContentValues] instance which contains only values that have changed between
 * the receiver (original) and parameter (new) instances.
 */
fun ContentValues.diffFrom(contentValues: ContentValues): ContentValues {
  val diff = ContentValues()
  for ((name, oldValue) in this.valueSet()) {
    val newValue = contentValues.get(name)
    if (newValue != oldValue) {
      diff.putAny(name, newValue)
    }
  }
  return diff
}

/**
 * Auto casts an [Any] value and uses the appropriate `put` method to store it
 * in the [ContentValues] instance.
 */
fun ContentValues.putAny(
  name: String,
  value: Any?
) {
  if (value == null) {
    putNull(name)
    return
  }
  when (value) {
    is String -> put(name, value)
    is Byte -> put(name, value)
    is Short -> put(name, value)
    is Int -> put(name, value)
    is Long -> put(name, value)
    is Float -> put(name, value)
    is Double -> put(name, value)
    is Boolean -> put(name, value)
    is ByteArray -> put(name, value)
    else -> throw IllegalArgumentException("ContentValues can't hold $value")
  }
}
