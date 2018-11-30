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
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
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

fun TextView.trimmedText() = text.toString().trim()

fun TextView.textAsLong(): Long {
  val text = trimmedText()
  return if (text.isEmpty()) 0L else text.toLong()
}

fun Spinner.onItemSelected(cb: (Int) -> Unit) {
  onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(
      parent: AdapterView<*>?,
      view: View?,
      position: Int,
      id: Long
    ) = cb(position)
  }
}

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
