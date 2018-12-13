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

fun View.enable() {
  isEnabled = true
}

fun View.disable() {
  isEnabled = false
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
