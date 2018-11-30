/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeResource
import androidx.annotation.DrawableRes
import javax.inject.Inject

/** @author Aidan Follestad (afollestad) */
interface BitmapProvider {

  fun get(@DrawableRes res: Int): Bitmap
}

/** @author Aidan Follestad (afollestad) */
class RealBitmapProvider @Inject constructor(
  private val app: Application
) : BitmapProvider {

  override fun get(res: Int): Bitmap {
    return decodeResource(app.resources, res)
  }
}
