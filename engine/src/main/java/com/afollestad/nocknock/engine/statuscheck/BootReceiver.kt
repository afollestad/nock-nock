/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine.statuscheck

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import com.afollestad.nocknock.utilities.ext.injector
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
class BootReceiver : BroadcastReceiver() {

  @Inject lateinit var checkStatusManager: CheckStatusManager

  override fun onReceive(
    context: Context,
    intent: Intent
  ) {
    require(ACTION_BOOT_COMPLETED == intent.action) {
      "BootReceiver should only receive ACTION_BOOT_COMPLETED intents."
    }

    log("Received boot event! Let's go.")
    context.injector()
        .injectInto(this)

    val pendingResult = goAsync()
    GlobalScope.launch(Main) {
      async(IO) { checkStatusManager.ensureScheduledChecks() }.await()
      pendingResult.resultCode = 0
      pendingResult.finish()
    }
  }
}
