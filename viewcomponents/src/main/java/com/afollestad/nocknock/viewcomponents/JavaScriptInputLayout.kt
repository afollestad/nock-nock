/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.viewcomponents

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import androidx.annotation.CheckResult
import com.afollestad.nocknock.viewcomponents.ext.dimenFloat
import com.afollestad.nocknock.viewcomponents.ext.dimenInt
import com.afollestad.nocknock.viewcomponents.ext.showOrHide
import com.afollestad.nocknock.viewcomponents.ext.trimmedText
import kotlinx.android.synthetic.main.javascript_input_layout.view.error_text
import kotlinx.android.synthetic.main.javascript_input_layout.view.userInput

/** @author Aidan Follestad (afollestad) */
class JavaScriptInputLayout(
  context: Context,
  attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

  init {
    val contentInset = dimenInt(R.dimen.content_inset)
    val contentInsetHalf = dimenInt(R.dimen.content_inset_half)
    setPadding(
        contentInsetHalf, // left
        contentInset, // top
        contentInsetHalf, // right
        contentInset // bottom
    )
    elevation = dimenFloat(R.dimen.default_elevation)
    inflate(context, R.layout.javascript_input_layout, this)
  }

  fun setError(error: String?) {
    error_text.showOrHide(error != null)
    error_text.text = error
  }

  fun setCode(code: String?) {
    if (code.isNullOrEmpty()) {
      setDefaultCode()
      return
    }
    userInput.setText(code.trim())
  }

  @CheckResult fun getCode() = userInput.trimmedText()

  fun clear() = userInput.setText("")

  private fun setDefaultCode() = userInput.setText(R.string.default_js)
}
