/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.data

/** @author Aidan Follestad (afollestad) */
enum class ValidationMode(val value: Int) {
  STATUS_CODE(1),
  TERM_SEARCH(2),
  JAVASCRIPT(3);

  companion object {

    fun fromValue(value: Int) = when (value) {
      STATUS_CODE.value -> STATUS_CODE
      TERM_SEARCH.value -> TERM_SEARCH
      JAVASCRIPT.value -> JAVASCRIPT
      else -> throw IllegalArgumentException("Unknown validationMode: $value")
    }

    fun fromIndex(index: Int) = when (index) {
      0 -> STATUS_CODE
      1 -> TERM_SEARCH
      2 -> JAVASCRIPT
      else -> throw IllegalArgumentException("Index out of range: $index")
    }
  }
}

fun Int.toValidationMode() = ValidationMode.fromValue(this)

fun Int.indexToValidationMode() = ValidationMode.fromIndex(this)
