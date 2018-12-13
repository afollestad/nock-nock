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
