/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ui

import android.view.View
import java.lang.System.currentTimeMillis

private const val DEFAULT_DEBOUNCE_INTERVAL = 750L

/** @author Aidan Follestad (@afollestad) */
abstract class DebouncedOnClickListener(
  private val delayBetweenClicks: Long = DEFAULT_DEBOUNCE_INTERVAL
) : View.OnClickListener {

  private var lastClickTimestamp = -1L

  @Deprecated(
      message = "onDebouncedClick should be overridden instead.",
      replaceWith = ReplaceWith("onDebouncedClick(v)")
  )
  override fun onClick(v: View) {
    val now = currentTimeMillis()
    if (lastClickTimestamp == -1L || now >= (lastClickTimestamp + delayBetweenClicks)) {
      onDebouncedClick(v)
    }
    lastClickTimestamp = now
  }

  abstract fun onDebouncedClick(v: View)
}

/** @author Aidan Follestad (@afollestad) */
fun View.onDebouncedClick(
  delayBetweenClicks: Long = DEFAULT_DEBOUNCE_INTERVAL,
  click: (view: View) -> Unit
) {
  setOnClickListener(object : DebouncedOnClickListener(delayBetweenClicks) {
    override fun onDebouncedClick(v: View) {
      click(v)
    }
  })
}
