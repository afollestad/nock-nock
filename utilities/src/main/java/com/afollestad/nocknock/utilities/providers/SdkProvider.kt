/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
interface SdkProvider {

  fun hasOreo(): Boolean
}

/** @author Aidan Follestad (@afollestad) */
class RealSdkProvider @Inject constructor() : SdkProvider {

  override fun hasOreo() = SDK_INT >= O
}
