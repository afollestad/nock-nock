/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
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
interface IdProvider : Serializable {

  fun id(): Int
}

/** @author Aidan Follestad (@afollestad) */
interface IntentProvider {

  fun getPendingIntentForViewSite(
    model: IdProvider
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

  override fun getPendingIntentForViewSite(model: IdProvider): PendingIntent {
    val openIntent = getIntentForViewSite(model)
    return PendingIntent.getActivity(
        app,
        BASE_NOTIFICATION_REQUEST_CODE + model.id(),
        openIntent,
        FLAG_CANCEL_CURRENT
    )
  }

  private fun getIntentForViewSite(model: IdProvider) =
    Intent(app, mainActivity).apply {
      putExtra(KEY_VIEW_NOTIFICATION_MODEL, model)
    }
}
