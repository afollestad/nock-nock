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
package com.afollestad.nocknock.utilities.providers

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.io.Serializable

/** @author Aidan Follestad (@afollestad) */
interface CanNotifyModel : Serializable {

  fun notifyId(): Int

  fun notifyName(): String

  fun notifyTag(): String

  fun notifyDescription(): String?
}

/** @author Aidan Follestad (@afollestad) */
interface IntentProvider {

  fun createFilter(vararg actions: String): IntentFilter

  fun getPendingIntentForViewSite(
    model: CanNotifyModel
  ): PendingIntent
}

/** @author Aidan Follestad (@afollestad) */
class RealIntentProvider(
  private val context: Context,
  private val mainActivityClass: Class<*>
) : IntentProvider {

  companion object {
    const val BASE_NOTIFICATION_REQUEST_CODE = 40
    const val KEY_VIEW_NOTIFICATION_MODEL = "model"
  }

  override fun createFilter(vararg actions: String) = IntentFilter().apply {
    actions.forEach { addAction(it) }
  }

  override fun getPendingIntentForViewSite(model: CanNotifyModel): PendingIntent {
    val openIntent = getIntentForViewSite(model)
    return PendingIntent.getActivity(
        context,
        BASE_NOTIFICATION_REQUEST_CODE + model.notifyId(),
        openIntent,
        FLAG_CANCEL_CURRENT
    )
  }

  private fun getIntentForViewSite(model: CanNotifyModel) =
    Intent(context, mainActivityClass).apply {
      putExtra(KEY_VIEW_NOTIFICATION_MODEL, model)
    }
}
