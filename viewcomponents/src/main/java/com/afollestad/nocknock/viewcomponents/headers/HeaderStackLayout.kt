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
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import com.afollestad.nocknock.data.model.Header
import com.afollestad.nocknock.viewcomponents.R
import kotlinx.android.synthetic.main.header_stack_item_content.view.btnRemove
import kotlinx.android.synthetic.main.header_stack_item_content.view.inputKey
import kotlinx.android.synthetic.main.header_stack_item_content.view.inputValue
import kotlinx.android.synthetic.main.header_stack_layout.view.addHeader
import kotlinx.android.synthetic.main.header_stack_layout.view.header_list as list

/** @author Aidan Follestad (@afollestad) */
class HeaderStackLayout(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs), OnClickListener {

  private var data: MutableLiveData<List<Header>>? = null
  private var headers = mutableListOf<Header>()

  init {
    orientation = VERTICAL
    inflate(context, R.layout.header_stack_layout, this)
    addHeader.setOnClickListener { addEntry(Header()) }
  }

  fun attach(data: MutableLiveData<List<Header>>) {
    list.removeAllViews()
    headers.clear()
    data.value?.forEach(::addEntry)
    this.data = data
  }

  fun postLiveData() = this.data?.postValue(headers)

  override fun onClick(v: View) {
    val index = v.tag as Int
    list.removeViewAt(index)
    headers.removeAt(index)
    postLiveData()
  }

  private fun addEntry(forHeader: Header) {
    // Keep track of reference for posting future changes.
    headers.add(forHeader)

    val li = LayoutInflater.from(context)
    val entry = li.inflate(R.layout.header_stack_item, list, false) as HeaderItemLayout
    list.addView(entry)

    entry.run {
      inputKey.setText(forHeader.key)
      inputKey.post { entry.inputKey.requestFocus() }
      attachHeader(forHeader, this@HeaderStackLayout)
      inputValue.setText(forHeader.value)

      btnRemove.tag = headers.size - 1
      btnRemove.setOnClickListener(this@HeaderStackLayout)
    }
  }
}
