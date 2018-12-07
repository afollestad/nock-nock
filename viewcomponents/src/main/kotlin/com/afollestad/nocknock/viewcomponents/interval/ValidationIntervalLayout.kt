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
package com.afollestad.nocknock.viewcomponents.interval

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afollestad.nocknock.utilities.ext.DAY
import com.afollestad.nocknock.utilities.ext.HOUR
import com.afollestad.nocknock.utilities.ext.MINUTE
import com.afollestad.nocknock.utilities.ext.WEEK
import com.afollestad.nocknock.viewcomponents.R.array
import com.afollestad.nocknock.viewcomponents.R.layout
import com.afollestad.nocknock.viewcomponents.livedata.attachLiveData
import com.afollestad.nocknock.viewcomponents.livedata.lifecycleOwner
import com.afollestad.nocknock.viewcomponents.livedata.toViewError
import kotlinx.android.synthetic.main.validation_interval_layout.view.input
import kotlinx.android.synthetic.main.validation_interval_layout.view.spinner

/** @author Aidan Follestad (@afollestad) */
class ValidationIntervalLayout(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

  companion object {
    const val INDEX_MINUTE = 0
    const val INDEX_HOUR = 1
    const val INDEX_DAY = 2
    const val INDEX_WEEK = 3
  }

  init {
    orientation = VERTICAL
    inflate(context, layout.validation_interval_layout, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    val spinnerAdapter = ArrayAdapter(
        context,
        layout.list_item_spinner,
        resources.getStringArray(array.interval_options)
    )
    spinnerAdapter.setDropDownViewResource(
        layout.list_item_spinner_dropdown
    )
    spinner.adapter = spinnerAdapter
  }

  fun attach(
    valueData: MutableLiveData<Int>,
    multiplierData: MutableLiveData<Long>,
    errorData: LiveData<Int?>
  ) {
    input.attachLiveData(lifecycleOwner(), valueData)
    spinner.attachLiveData(
        lifecycleOwner = lifecycleOwner(),
        data = multiplierData,
        inTransformer = {
          when (it) {
            MINUTE -> 0
            HOUR -> 1
            DAY -> 2
            WEEK -> 3
            else -> throw IllegalStateException("Unknown multiplier: $it")
          }
        },
        outTransformer = {
          when (it) {
            INDEX_MINUTE -> MINUTE
            INDEX_HOUR -> HOUR
            INDEX_DAY -> DAY
            INDEX_WEEK -> WEEK
            else -> throw IllegalStateException("Unknown multiplier index: $it")
          }
        }
    )
    errorData.toViewError(lifecycleOwner(), this, ::setError)
  }

  private fun setError(error: String?) {
    input.error = error
  }
}
