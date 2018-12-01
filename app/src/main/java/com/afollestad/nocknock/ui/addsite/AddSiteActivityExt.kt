/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
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
