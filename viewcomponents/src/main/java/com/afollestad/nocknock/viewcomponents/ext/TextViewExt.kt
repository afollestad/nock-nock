/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents.ext

import android.widget.TextView

fun TextView.trimmedText() = text.toString().trim()

fun TextView.textAsLong(): Long {
  val text = trimmedText()
  return if (text.isEmpty()) 0L else text.toLong()
}

///** @author https://stackoverflow.com/a/53296137/309644 */
//fun EditText.addFilter(filter: InputFilter) {
//  filters =
//      if (filters.isNullOrEmpty()) {
//        arrayOf(filter)
//      } else {
//        filters
//            .toMutableList()
//            .apply {
//              removeAll { it.javaClass == filter.javaClass }
//              add(filter)
//            }
//            .toTypedArray()
//      }
//}
//
//fun <T : InputFilter> EditText.removeFilters(type: Class<in T>) {
//  filters =
//      if (filters.isNullOrEmpty()) {
//        filters
//      } else {
//        filters
//            .toMutableList()
//            .apply {
//              removeAll { it.javaClass == type }
//            }
//            .toTypedArray()
//      }
//}
//
//fun EditText.setMaxLength(maxLength: Int) {
//  if (maxLength <= 0) {
//    removeFilters(LengthFilter::class.java)
//  } else {
//    if (text.length > maxLength) {
//      setText(text.subSequence(0, maxLength))
//      setSelection(text.length)
//    }
//    addFilter(LengthFilter(maxLength))
//  }
//}
