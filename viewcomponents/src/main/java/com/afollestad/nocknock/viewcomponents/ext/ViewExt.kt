/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents.ext

import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import androidx.annotation.DimenRes

fun View.show() {
  visibility = VISIBLE
}

fun View.conceal() {
  visibility = INVISIBLE
}

fun View.hide() {
  visibility = GONE
}

fun View.showOrHide(show: Boolean) = if (show) show() else hide()

fun View.onLayout(cb: () -> Unit) {
  if (this.viewTreeObserver.isAlive) {
    this.viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
          override fun onGlobalLayout() {
            cb()
            this@onLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
          }
        })
  }
}

fun View.dimenFloat(@DimenRes res: Int) = resources.getDimension(res)

fun View.dimenInt(@DimenRes res: Int) = resources.getDimensionPixelSize(res)
