/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents.ext

import android.widget.ScrollView

fun ScrollView.onScroll(cb: (y: Int) -> Unit) =
  viewTreeObserver.addOnScrollChangedListener { cb(scrollY) }
