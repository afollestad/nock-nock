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
package com.afollestad.nocknock.viewcomponents.retrypolicy

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import com.afollestad.nocknock.utilities.ext.onTextChanged
import com.afollestad.nocknock.viewcomponents.R
import com.afollestad.nocknock.viewcomponents.ext.asSafeInt
import com.afollestad.nocknock.viewcomponents.livedata.attachLiveData
import com.afollestad.nocknock.viewcomponents.livedata.lifecycleOwner
import com.afollestad.vvalidator.form.Form
import kotlinx.android.synthetic.main.retry_policy_layout.view.minutes
import kotlinx.android.synthetic.main.retry_policy_layout.view.times
import kotlinx.android.synthetic.main.retry_policy_layout.view.retry_policy_desc as description

/** @author Aidan Follestad (@afollestad) */
class RetryPolicyLayout(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

  init {
    orientation = VERTICAL
    inflate(context, R.layout.retry_policy_layout, this)
  }

  fun attach(
    timesData: MutableLiveData<Int>,
    minutesData: MutableLiveData<Int>,
    form: Form
  ) {
    times.attachLiveData(lifecycleOwner(), timesData)
    minutes.attachLiveData(lifecycleOwner(), minutesData)

    times.onTextChanged { invalidateDescriptionText() }
    minutes.onTextChanged { invalidateDescriptionText() }

    invalidateDescriptionText()

    form.input(times, optional = true) {
      isNumber().greaterThan(0)
    }
    form.input(minutes, optional = true) {
      isNumber().greaterThan(0)
    }
  }

  private fun invalidateDescriptionText() {
    val timesInt = times.text.toString()
        .asSafeInt()
    val minutesInt = minutes.text.toString()
        .asSafeInt()
    description.text = resources.getString(
        R.string.retry_policy_description,
        timesInt,
        minutesInt
    )
  }
}
