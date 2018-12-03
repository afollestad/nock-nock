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
package com.afollestad.nocknock.viewcomponents

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.annotation.CheckResult
import com.afollestad.nocknock.utilities.ext.DAY
import com.afollestad.nocknock.utilities.ext.HOUR
import com.afollestad.nocknock.utilities.ext.MINUTE
import com.afollestad.nocknock.utilities.ext.WEEK
import com.afollestad.nocknock.viewcomponents.R.array
import com.afollestad.nocknock.viewcomponents.ext.textAsLong
import kotlinx.android.synthetic.main.check_interval_layout.view.input
import kotlinx.android.synthetic.main.check_interval_layout.view.spinner
import kotlin.math.ceil

/** @author Aidan Follestad (@afollestad) */
class CheckIntervalLayout(
  context: Context,
  attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

  companion object {
    private const val INDEX_MINUTE = 0
    private const val INDEX_HOUR = 1
    private const val INDEX_DAY = 2
    private const val INDEX_WEEK = 3
  }

  init {
    orientation = VERTICAL
    inflate(context, R.layout.check_interval_layout, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    val spinnerAdapter = ArrayAdapter(
        context,
        R.layout.list_item_spinner,
        resources.getStringArray(array.interval_options)
    )
    spinnerAdapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown)
    spinner.adapter = spinnerAdapter
  }

  fun setError(error: String?) {
    input.error = error
  }

  fun set(interval: Long) {
    when {
      interval >= WEEK -> {
        input.setText(calculateDisplayValue(interval, WEEK))
        spinner.setSelection(3)
      }
      interval >= DAY -> {
        input.setText(calculateDisplayValue(interval, DAY))
        spinner.setSelection(2)
      }
      interval >= HOUR -> {
        input.setText(calculateDisplayValue(interval, HOUR))
        spinner.setSelection(1)
      }
      interval >= MINUTE -> {
        input.setText(calculateDisplayValue(interval, MINUTE))
        spinner.setSelection(0)
      }
      else -> {
        input.setText("0")
        spinner.setSelection(0)
      }
    }
  }

  @CheckResult fun getSelectedCheckInterval(): Long {
    val intervalInput = input.textAsLong()
    val spinnerPos = spinner.selectedItemPosition
    return when (spinnerPos) {
      INDEX_MINUTE -> intervalInput * MINUTE
      INDEX_HOUR -> intervalInput * HOUR
      INDEX_DAY -> intervalInput * DAY
      INDEX_WEEK -> intervalInput * WEEK
      else -> throw IllegalStateException("Unexpected index: $spinnerPos")
    }
  }

  private fun calculateDisplayValue(
    interval: Long,
    by: Long
  ): String {
    val intervalFloat = interval.toFloat()
    val byFloat = by.toFloat()
    return ceil(intervalFloat / byFloat).toInt()
        .toString()
  }
}
