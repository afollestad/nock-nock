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

import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.deleteSite
import com.afollestad.nocknock.data.model.Header
import com.afollestad.nocknock.data.model.RetryPolicy
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.Status
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.ValidationMode
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.model.ValidationResult
import com.afollestad.nocknock.data.model.textRes
import com.afollestad.nocknock.data.updateSite
import com.afollestad.nocknock.engine.validation.ValidationExecutor
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.ui.ScopedViewModel
import com.afollestad.nocknock.utilities.ext.formatDate
import com.afollestad.nocknock.utilities.livedata.map
import com.afollestad.nocknock.utilities.livedata.zip
import com.afollestad.nocknock.utilities.providers.StringProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.lang.System.currentTimeMillis

/** @author Aidan Follestad (@afollestad) */
class ViewSiteViewModel(
  private val stringProvider: StringProvider,
  private val database: AppDatabase,
  private val notificationManager: NockNotificationManager,
  private val validationManager: ValidationExecutor,
  mainDispatcher: CoroutineDispatcher,
  private val ioDispatcher: CoroutineDispatcher
) : ScopedViewModel(mainDispatcher), LifecycleObserver {

  lateinit var site: Site

  // Public properties
  val status = MutableLiveData<Status>()
  val name = MutableLiveData<String>()
  val tags = MutableLiveData<String>()
  val url = MutableLiveData<String>()
  val timeout = MutableLiveData<Int>()
  val validationMode = MutableLiveData<ValidationMode>()
  val validationSearchTerm = MutableLiveData<String>()
  val validationScript = MutableLiveData<String>()
  val checkIntervalValue = MutableLiveData<Int>()
  val checkIntervalUnit = MutableLiveData<Long>()
  val retryPolicyTimes = MutableLiveData<Int>()
  val retryPolicyMinutes = MutableLiveData<Int>()
  val headers = MutableLiveData<List<Header>>()
  val certificateUri = MutableLiveData<String>()
  internal val disabled = MutableLiveData<Boolean>()
  internal val lastResult = MutableLiveData<ValidationResult?>()

  private val isLoading = MutableLiveData<Boolean>()

  @CheckResult fun onIsLoading(): LiveData<Boolean> = isLoading

  @CheckResult fun onUrlWarningVisibility(): LiveData<Boolean> {
    return url.map {
      val parsed = HttpUrl.parse(it)
      return@map it.isNotEmpty() && parsed == null
    }
  }

  @CheckResult fun onValidationModeDescription(): LiveData<Int> {
    return validationMode.map {
      when (it!!) {
        STATUS_CODE -> R.string.validation_mode_status_desc
        TERM_SEARCH -> R.string.validation_mode_term_desc
        JAVASCRIPT -> R.string.validation_mode_javascript_desc
      }
    }
  }

  @CheckResult fun onValidationSearchTermVisibility() = validationMode.map { it == TERM_SEARCH }

  @CheckResult fun onValidationScriptVisibility() = validationMode.map { it == JAVASCRIPT }

  @CheckResult fun onDisableChecksVisibility(): LiveData<Boolean> = disabled.map { !it }

  @CheckResult fun onDoneButtonText(): LiveData<Int> =
    disabled.map {
      if (it) R.string.renable_and_save_changes
      else R.string.save_changes
    }

  @CheckResult fun onLastCheckResultText(): LiveData<String> = lastResult.map {
    if (it == null) {
      stringProvider.get(R.string.none)
    } else {
      val statusText = it.status.textRes()
      if (statusText == 0) {
        it.reason
      } else {
        stringProvider.get(statusText)
      }
    }
  }

  @CheckResult fun onNextCheckText(): LiveData<String> {
    return zip(disabled, lastResult)
        .map {
          val disabled = it.first
          val lastResult = it.second
          if (disabled) {
            stringProvider.get(R.string.auto_checks_disabled)
          } else {
            val lastCheck = lastResult?.timestampMs ?: currentTimeMillis()
            (lastCheck + getCheckIntervalMs()).formatDate()
          }
        }
  }

  // Actions
  fun commit(done: () -> Unit) {
    scope.launch {
      val updatedModel = getUpdatedDbModel() ?: return@launch
      isLoading.value = true

      withContext(ioDispatcher) {
        database.updateSite(updatedModel)
      }
      validationManager.scheduleValidation(
          site = updatedModel,
          rightNow = true,
          cancelPrevious = true
      )

      isLoading.value = false
      done()
    }
  }

  fun checkNow() {
    val checkModel = site.withStatus(
        status = WAITING
    )
    setModel(checkModel)
    validationManager.scheduleValidation(
        site = checkModel,
        rightNow = true,
        cancelPrevious = true
    )
  }

  fun removeSite(done: () -> Unit) {
    validationManager.cancelScheduledValidation(site)
    notificationManager.cancelStatusNotification(site)

    scope.launch {
      isLoading.value = true
      withContext(ioDispatcher) {
        database.deleteSite(site)
      }
      isLoading.value = false
      done()
    }
  }

  fun disableSite() {
    validationManager.cancelScheduledValidation(site)
    notificationManager.cancelStatusNotification(site)

    scope.launch {
      isLoading.value = true
      val newModel = site.copy(
          settings = site.settings!!.copy(
              disabled = true
          )
      )
      withContext(ioDispatcher) {
        database.updateSite(newModel)
      }
      isLoading.value = false
      setModel(newModel)
    }
  }

  // Utilities
  @VisibleForTesting(otherwise = PRIVATE)
  fun getCheckIntervalMs(): Long {
    val value = checkIntervalValue.value ?: return 0
    val unit = checkIntervalUnit.value ?: return 0
    return value * unit
  }

  @VisibleForTesting(otherwise = PRIVATE)
  fun getValidationArgs(): String? {
    return when (validationMode.value) {
      TERM_SEARCH -> validationSearchTerm.value?.trim()
      JAVASCRIPT -> validationScript.value?.trim()
      else -> null
    }
  }

  private fun getUpdatedDbModel(): Site? {
    val timeout = timeout.value ?: 10_000
    val cleanedTags = tags.value?.split(',')?.joinToString(separator = ",") ?: ""

    val newSettings = site.settings!!.copy(
        validationIntervalMs = getCheckIntervalMs(),
        validationMode = validationMode.value!!,
        validationArgs = getValidationArgs(),
        networkTimeout = timeout,
        disabled = false,
        certificate = certificateUri.value?.toString()
    )

    val retryPolicyTimes = retryPolicyTimes.value ?: 0
    val retryPolicyMinutes = retryPolicyMinutes.value ?: 0
    val retryPolicy: RetryPolicy? = if (retryPolicyTimes > 0 && retryPolicyMinutes > 0) {
      if (site.retryPolicy != null) {
        // Have existing policy, update it
        site.retryPolicy!!.copy(
            count = retryPolicyTimes,
            minutes = retryPolicyMinutes
        )
      } else {
        // Create new policy
        RetryPolicy(
            count = retryPolicyTimes,
            minutes = retryPolicyMinutes
        )
      }
    } else {
      // No policy
      null
    }

    return site.copy(
        name = name.value!!.trim(),
        tags = cleanedTags,
        url = url.value!!.trim(),
        settings = newSettings,
        retryPolicy = retryPolicy,
        headers = headers.value ?: emptyList()
    )
        .withStatus(status = WAITING)
  }
}
