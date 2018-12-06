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
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.afollestad.nocknock.utilities.ext.DAY
import com.afollestad.nocknock.utilities.ext.HOUR
import com.afollestad.nocknock.utilities.ext.MINUTE
import com.afollestad.nocknock.utilities.ext.WEEK
import com.afollestad.nocknock.utilities.ext.onTextChanged
import com.afollestad.nocknock.viewcomponents.R.array
import com.afollestad.nocknock.viewcomponents.R.layout
import com.afollestad.nocknock.viewcomponents.ext.onItemSelected
import kotlinx.android.synthetic.main.check_interval_layout.view.input
import kotlinx.android.synthetic.main.check_interval_layout.view.spinner

/** @author Aidan Follestad (@afollestad) */
class CheckIntervalLayout(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs), LifecycleOwner {

  companion object {
    const val INDEX_MINUTE = 0
    const val INDEX_HOUR = 1
    const val INDEX_DAY = 2
    const val INDEX_WEEK = 3
  }

  init {
    orientation = VERTICAL
    inflate(context, layout.check_interval_layout, this)
  }

  private lateinit var valueData: MutableLiveData<Int>
  private lateinit var multiplierData: MutableLiveData<Long>

  private val lifecycle = LifecycleRegistry(this)

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
    lifecycle.markState(STARTED)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    lifecycle.markState(RESUMED)
  }

  override fun onDetachedFromWindow() {
    lifecycle.markState(DESTROYED)
    super.onDetachedFromWindow()
  }

  fun setError(error: String?) {
    input.error = error
  }

  fun attach(
    valueData: MutableLiveData<Int>,
    multiplierData: MutableLiveData<Long>,
    errorData: LiveData<Int?>
  ) {
    this.valueData = valueData
    this.multiplierData = multiplierData

    this.valueData.observe(this, Observer {
      input.setText("$it")
    })
    this.multiplierData.observe(this, Observer { multiplier ->
      val targetPos = when (multiplier) {
        MINUTE -> 0
        HOUR -> 1
        DAY -> 2
        WEEK -> 3
        else -> throw IllegalStateException("Unknown multiplier: $multiplier")
      }
      if (spinner.selectedItemPosition != targetPos) {
        spinner.setSelection(targetPos)
      }
    })

    errorData.observe(this, Observer {
      setError(if (it != null) resources.getString(it) else null)
    })

    input.onTextChanged { this.valueData.value = it.toInt() }
    spinner.onItemSelected {
      this.multiplierData.value = when (it) {
        INDEX_MINUTE -> MINUTE
        INDEX_HOUR -> HOUR
        INDEX_DAY -> DAY
        INDEX_WEEK -> WEEK
        else -> throw IllegalStateException("Unknown multiplier index: $it")
      }
    }
  }

  override fun getLifecycle() = lifecycle
}
