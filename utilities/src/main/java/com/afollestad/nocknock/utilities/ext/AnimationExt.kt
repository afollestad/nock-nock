/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

fun Animator.onEnd(cb: () -> Unit) {
  addListener(object : AnimatorListenerAdapter() {
    override fun onAnimationEnd(animation: Animator) {
      super.onAnimationEnd(animation)
      cb()
    }
  })
}
