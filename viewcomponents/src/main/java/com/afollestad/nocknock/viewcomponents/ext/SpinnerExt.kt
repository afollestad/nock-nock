/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents.ext

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

fun Spinner.onItemSelected(cb: (Int) -> Unit) {
  onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(
      parent: AdapterView<*>?,
      view: View?,
      position: Int,
      id: Long
    ) = cb(position)
  }
}
