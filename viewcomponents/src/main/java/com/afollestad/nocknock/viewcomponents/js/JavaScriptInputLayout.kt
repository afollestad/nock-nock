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
package com.afollestad.nocknock.viewcomponents.js

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afollestad.nocknock.viewcomponents.R.dimen
import com.afollestad.nocknock.viewcomponents.R.layout
import com.afollestad.nocknock.viewcomponents.ext.dimenFloat
import com.afollestad.nocknock.viewcomponents.ext.dimenInt
import com.afollestad.nocknock.viewcomponents.ext.showOrHide
import com.afollestad.nocknock.viewcomponents.livedata.attachLiveData
import com.afollestad.nocknock.viewcomponents.livedata.lifecycleOwner
import com.afollestad.nocknock.viewcomponents.livedata.toViewError
import com.afollestad.nocknock.viewcomponents.livedata.toViewVisibility
import kotlinx.android.synthetic.main.javascript_input_layout.view.error_text
import kotlinx.android.synthetic.main.javascript_input_layout.view.userInput

/** @author Aidan Follestad (@afollestad) */
class JavaScriptInputLayout(
  context: Context,
  attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

  init {
    val contentInset = dimenInt(dimen.content_inset)
    val contentInsetHalf = dimenInt(
        dimen.content_inset_half
    )
    setPadding(
        contentInsetHalf, // left
        contentInset, // top
        contentInsetHalf, // right
        contentInset // bottom
    )
    elevation = dimenFloat(dimen.default_elevation)
    inflate(context, layout.javascript_input_layout, this)
  }

  fun attach(
    codeData: MutableLiveData<String>,
    errorData: LiveData<Int?>,
    visibility: LiveData<Boolean>
  ) {
    userInput.attachLiveData(lifecycleOwner(), codeData)
    errorData.toViewError(lifecycleOwner(), this, ::setError)
    visibility.toViewVisibility(lifecycleOwner(), this)
  }

  private fun setError(error: String?) {
    error_text.showOrHide(error != null)
    error_text.text = error
  }
}
