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
package com.afollestad.nocknock.viewcomponents.ext

import android.widget.TextView

fun TextView.trimmedText() = text.toString().trim()

fun TextView.textAsInt(defaultValue: Int = 0): Int {
  val text = trimmedText()
  return if (text.isEmpty()) defaultValue else text.toInt()
}

fun TextView.textAsLong(defaultValue: Long = 0L): Long {
  val text = trimmedText()
  return if (text.isEmpty()) defaultValue else text.toLong()
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
