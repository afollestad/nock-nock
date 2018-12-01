/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

fun Animator.onEnd(cb: () -> Unit) {
  addListener(object : AnimatorListenerAdapter() {
    override fun onAnimationEnd(animation: Animator) {
      super.onAnimationEnd(animation)
      cb()
    }
  })
}

fun View.animateRotation(
  loop: Boolean = true,
  firstPass: Boolean = true,
  durationPerRotation: Long = 1000,
  degreesPerRotation: Float = 360f
) {
  if (firstPass) {
    animate().cancel()
  }
  animate()
      .rotationBy(degreesPerRotation)
      .setDuration(durationPerRotation)
      .setListener(object : AnimatorListenerAdapter() {

        var isCancelled = false

        override fun onAnimationCancel(animation: Animator?) {
          super.onAnimationCancel(animation)
          isCancelled = true
        }

        override fun onAnimationEnd(animation: Animator) {
          super.onAnimationEnd(animation)
          if (loop && !isCancelled) {
            animateRotation(loop = true, firstPass = false)
          }
        }
      })
      .start()
}
