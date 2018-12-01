/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.app.Application
import androidx.annotation.StringRes
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
interface StringProvider {

  fun get(@StringRes res: Int): String
}

/** @author Aidan Follestad (@afollestad) */
class RealStringProvider @Inject constructor(
  private val app: Application
) : StringProvider {

  override fun get(res: Int): String {
    return app.resources.getString(res)
  }
}
