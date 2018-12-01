/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import kotlin.math.ceil

const val SECOND: Long = 1000
const val MINUTE = SECOND * 60
const val HOUR = MINUTE * 60
const val DAY = HOUR * 24
const val WEEK = DAY * 7
const val MONTH = WEEK * 4

fun Long.timeString() = when {
  this <= 0 -> "??"
  this >= MONTH ->
    "${ceil((this.toFloat() / MONTH.toFloat()).toDouble()).toInt()}mo"
  this >= WEEK ->
    "${ceil((this.toFloat() / WEEK.toFloat()).toDouble()).toInt()}w"
  this >= DAY ->
    "${ceil((this.toFloat() / DAY.toFloat()).toDouble()).toInt()}d"
  this >= HOUR ->
    "${ceil((this.toFloat() / HOUR.toFloat()).toDouble()).toInt()}h"
  this >= MINUTE ->
    "${ceil((this.toFloat() / MINUTE.toFloat()).toDouble()).toInt()}m"
  else -> "<1m"
}
