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
    private const val SHOW_DELAY_MS = 100L
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
