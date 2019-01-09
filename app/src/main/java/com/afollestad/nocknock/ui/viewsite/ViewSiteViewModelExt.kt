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
package com.afollestad.nocknock.ui.viewsite

import com.afollestad.nocknock.data.model.RetryPolicy
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.utilities.ext.DAY
import com.afollestad.nocknock.utilities.ext.HOUR
import com.afollestad.nocknock.utilities.ext.MINUTE
import com.afollestad.nocknock.utilities.ext.WEEK
import kotlin.math.ceil

fun ViewSiteViewModel.setModel(site: Site) {
  val settings = site.settings ?: throw IllegalArgumentException("Settings must be populated!")
  this.site = site

  status.value = site.lastResult?.status ?: WAITING
  name.value = site.name
  tags.value = site.tags
  url.value = site.url
  timeout.value = settings.networkTimeout

  validationMode.value = settings.validationMode
  when (settings.validationMode) {
    TERM_SEARCH -> {
      validationSearchTerm.value = settings.validationArgs
      validationScript.value = null
    }
    JAVASCRIPT -> {
      validationSearchTerm.value = null
      validationScript.value = settings.validationArgs
    }
    else -> {
      validationSearchTerm.value = null
      validationScript.value = null
    }
  }

  setCheckInterval(settings.validationIntervalMs)
  setRetryPolicy(site.retryPolicy)
  headers.value = site.headers

  this.disabled.value = settings.disabled
  this.lastResult.value = site.lastResult
}

private fun ViewSiteViewModel.setCheckInterval(interval: Long) {
  when {
    interval >= WEEK -> {
      checkIntervalValue.value =
          getIntervalFromUnit(interval, WEEK)
      checkIntervalUnit.value = WEEK
    }
    interval >= DAY -> {
      checkIntervalValue.value =
          getIntervalFromUnit(interval, DAY)
      checkIntervalUnit.value = DAY
    }
    interval >= HOUR -> {
      checkIntervalValue.value =
          getIntervalFromUnit(interval, HOUR)
      checkIntervalUnit.value = HOUR
    }
    interval >= MINUTE -> {
      checkIntervalValue.value =
          getIntervalFromUnit(interval, MINUTE)
      checkIntervalUnit.value = MINUTE
    }
    else -> {
      checkIntervalValue.value = 0
      checkIntervalUnit.value = MINUTE
    }
  }
}

private fun ViewSiteViewModel.setRetryPolicy(policy: RetryPolicy?) {
  if (policy == null) return
  retryPolicyTimes.value = policy.count
  retryPolicyMinutes.value = policy.minutes
}

private fun getIntervalFromUnit(
  millis: Long,
  unit: Long
): Int {
  val intervalFloat = millis.toFloat()
  val byFloat = unit.toFloat()
  return ceil(intervalFloat / byFloat).toInt()
}
