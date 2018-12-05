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

import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Intent
import com.afollestad.nocknock.utilities.qualifiers.MainActivityClass
import java.io.Serializable
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
interface CanNotifyModel : Serializable {

  fun notiId(): Int

  fun notiName(): String

  fun notiTag(): String
}

/** @author Aidan Follestad (@afollestad) */
interface IntentProvider {

  fun getPendingIntentForViewSite(
    model: CanNotifyModel
  ): PendingIntent
}

/** @author Aidan Follestad (@afollestad) */
class RealIntentProvider @Inject constructor(
  private val app: Application,
  @MainActivityClass private val mainActivity: Class<*>
) : IntentProvider {

  companion object {
    const val BASE_NOTIFICATION_REQUEST_CODE = 40
    const val KEY_VIEW_NOTIFICATION_MODEL = "model"
  }

  override fun getPendingIntentForViewSite(model: CanNotifyModel): PendingIntent {
    val openIntent = getIntentForViewSite(model)
    return PendingIntent.getActivity(
        app,
        BASE_NOTIFICATION_REQUEST_CODE + model.notiId(),
        openIntent,
        FLAG_CANCEL_CURRENT
    )
  }

  private fun getIntentForViewSite(model: CanNotifyModel) =
    Intent(app, mainActivity).apply {
      putExtra(KEY_VIEW_NOTIFICATION_MODEL, model)
    }
}
