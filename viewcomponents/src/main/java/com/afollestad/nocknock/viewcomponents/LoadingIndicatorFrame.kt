/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.afollestad.nocknock.viewcomponents.ext.hide
import com.afollestad.nocknock.viewcomponents.ext.show

/** @author Aidan Follestad (@afollestad) */
class LoadingIndicatorFrame(
  context: Context,
  attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
  companion object {
    private const val SHOW_DELAY_MS = 200L
  }

  private val showRunnable = Runnable { show() }

  init {
    setBackgroundColor(ContextCompat.getColor(context, R.color.loading_indicator_frame_background))
    hide() // hide self by default
    inflate(context, R.layout.loading_indicator_frame, this)
    isClickable = true
    isFocusable = true
  }

  fun setLoading() {
    handler.postDelayed(showRunnable, SHOW_DELAY_MS)
  }

  fun setDone() {
    handler.removeCallbacks(showRunnable)
    hide()
  }
}
