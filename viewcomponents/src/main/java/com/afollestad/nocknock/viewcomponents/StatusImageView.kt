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
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.nocknock.data.model.Status
import com.afollestad.nocknock.data.model.Status.CHECKING
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.data.model.Status.WAITING

/** @author Aidan Follestad (@afollestad) */
class StatusImageView(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

  init {
    setStatus(OK)
  }

  fun setStatus(status: Status) = when (status) {
    CHECKING, WAITING -> {
      setImageResource(R.drawable.status_progress)
      setBackgroundResource(R.drawable.yellow_circle)
    }
    ERROR -> {
      setImageResource(R.drawable.status_error)
      setBackgroundResource(R.drawable.red_circle)
    }
    OK -> {
      setImageResource(R.drawable.status_ok)
      setBackgroundResource(R.drawable.green_circle)
    }
  }
}
