/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.IntentFilter

fun Activity.safeRegisterReceiver(
  broadcastReceiver: BroadcastReceiver,
  filter: IntentFilter
) {
  try {
    registerReceiver(broadcastReceiver, filter)
  } catch (_: Exception) {
  }
}

fun Activity.safeUnregisterReceiver(broadcastReceiver: BroadcastReceiver) {
  try {
    unregisterReceiver(broadcastReceiver)
  } catch (_: Exception) {
  }
}
