/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.nocknock.data.ServerStatus
import com.afollestad.nocknock.data.ServerStatus.CHECKING
import com.afollestad.nocknock.data.ServerStatus.ERROR
import com.afollestad.nocknock.data.ServerStatus.OK
import com.afollestad.nocknock.data.ServerStatus.WAITING

/** @author Aidan Follestad (afollestad) */
class StatusImageView(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

  init {
    setStatus(OK)
  }

  fun setStatus(status: ServerStatus) = when (status) {
    CHECKING, WAITING -> {
      setImageResource(R.drawable.status_progress)
      setBackgroundResource(R.drawable.yellow_circle)
    }
    ERROR -> {
      setImageResource(R.drawable.status_error)
      setBackgroundResource(R.drawable.red_circle)
    }
    OK -> {
      setImageResource(R.drawable.status_ok)
      setBackgroundResource(R.drawable.green_circle)
    }
  }
}
