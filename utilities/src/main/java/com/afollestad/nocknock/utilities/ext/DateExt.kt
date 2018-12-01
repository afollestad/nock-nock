/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatDate(): String {
  if (this <= 0) {
    return "(None)"
  }
  val df = SimpleDateFormat("MMMM dd, hh:mm:ss a", Locale.getDefault())
  return df.format(Date(this))
}
