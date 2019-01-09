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
package com.afollestad.nocknock.viewcomponents.headers

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.afollestad.nocknock.data.model.Header
import com.afollestad.nocknock.utilities.ext.onTextChanged
import com.afollestad.nocknock.viewcomponents.R
import kotlinx.android.synthetic.main.header_stack_item_content.view.inputKey
import kotlinx.android.synthetic.main.header_stack_item_content.view.inputValue

/** @author Aidan Follestad (@afollestad) */
class HeaderItemLayout(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

  private var header: Header? = null
  private var stack: HeaderStackLayout? = null

  init {
    z
    orientation = HORIZONTAL
    inflate(context, R.layout.header_stack_item_content, this)
  }

  fun attachHeader(
    newHeader: Header,
    parentStack: HeaderStackLayout
  ) {
    this.header = newHeader
    this.stack = parentStack

    inputKey.run {
      setText(newHeader.key)
      onTextChanged {
        header?.key = it.trim()
        stack?.postLiveData()
      }
    }

    inputValue.run {
      setText(newHeader.value)
      onTextChanged {
        header?.value = it.trim()
        stack?.postLiveData()
      }
    }
  }
}
