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

  @Inject lateinit var checkStatusManager: ValidationManager

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
