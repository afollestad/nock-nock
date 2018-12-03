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
package com.afollestad.nocknock.ui.addsite

import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.afollestad.nocknock.utilities.ext.onEnd
import com.afollestad.nocknock.viewcomponents.ext.conceal
import com.afollestad.nocknock.viewcomponents.ext.show
import kotlinx.android.synthetic.main.activity_addsite.rootView

const val REVEAL_DURATION = 300L

internal fun AddSiteActivity.circularRevealActivity() {
  val circularReveal =
    ViewAnimationUtils.createCircularReveal(rootView, revealCx, revealCy, 0f, revealRadius)
        .apply {
          duration = REVEAL_DURATION
          interpolator = DecelerateInterpolator()
        }
  rootView.show()
  circularReveal.start()
}

internal fun AddSiteActivity.closeActivityWithReveal() {
  if (isClosing) return
  isClosing = true
  ViewAnimationUtils.createCircularReveal(rootView, revealCx, revealCy, revealRadius, 0f)
      .apply {
        duration = REVEAL_DURATION
        interpolator = AccelerateInterpolator()
        onEnd {
          rootView.conceal()
          finish()
          overridePendingTransition(0, 0)
        }
        start()
      }
}
