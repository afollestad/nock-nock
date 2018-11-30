/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.util

import android.graphics.Path
import android.view.View

/** @author Aidan Follestad (afollestad) */
object MathUtil {

  fun bezierCurve(
    targetView: View,
    rootView: View
  ): Path {
    val fabCenterX = (targetView.x + targetView.measuredWidth / 2).toInt()
    val fabCenterY = (targetView.y + targetView.measuredHeight / 2).toInt()

    val endCenterX = rootView.measuredWidth / 2 - targetView.measuredWidth / 2
    val endCenterY = rootView.measuredHeight / 2 - targetView.measuredHeight / 2

    val halfX = (fabCenterX - endCenterX) / 2
    val halfY = (fabCenterY - endCenterY) / 2

    var controlX = endCenterX + halfX
    var controlY = endCenterY + halfY

    controlY -= halfY
    controlX += halfX

    val path = Path()
    path.moveTo(targetView.x, targetView.y)
    path.quadTo(
        controlX.toFloat(),
        controlY.toFloat(),
        endCenterX.toFloat(),
        endCenterY.toFloat()
    )

    return path
  }
}
