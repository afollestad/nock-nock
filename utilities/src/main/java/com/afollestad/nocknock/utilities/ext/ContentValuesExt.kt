/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
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
