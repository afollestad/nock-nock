/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import android.content.Context

/** @author Aidan Follestad (afollestad) */
inline fun <reified T> Context.systemService(name: String): T {
  return getSystemService(name) as T
}
