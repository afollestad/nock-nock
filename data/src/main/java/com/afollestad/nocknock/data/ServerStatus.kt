/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.data

import com.afollestad.nocknock.data.ServerStatus.CHECKING
import com.afollestad.nocknock.data.ServerStatus.OK
import com.afollestad.nocknock.data.ServerStatus.WAITING

/** @author Aidan Follestad (afollestad) */
enum class ServerStatus(val value: Int) {
  OK(1),
  WAITING(2),
  CHECKING(3),
  ERROR(4);

  companion object {

    fun fromValue(value: Int) = when (value) {
      OK.value -> OK
      WAITING.value -> WAITING
      CHECKING.value -> CHECKING
      ERROR.value -> ERROR
      else -> throw IllegalArgumentException("Unknown validationMode: $value")
    }
  }
}

fun ServerStatus.textRes() = when (this) {
  OK -> R.string.everything_checks_out
  WAITING -> R.string.waiting
  CHECKING -> R.string.checking_status
  else -> 0
}

fun Int.toServerStatus() = ServerStatus.fromValue(this)

fun ServerStatus.isPending() = this == WAITING || this == CHECKING
