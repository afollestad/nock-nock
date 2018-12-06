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

import android.app.Application
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.deleteSite
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
import com.afollestad.nocknock.di.viewmodels.ScopedViewModel
import com.afollestad.nocknock.engine.validation.ValidationManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.utilities.ext.formatDate
import com.afollestad.nocknock.di.qualifiers.IoDispatcher
import com.afollestad.nocknock.viewcomponents.ext.isNullOrLessThan
import com.afollestad.nocknock.viewcomponents.ext.map
import com.afollestad.nocknock.viewcomponents.ext.zip
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
class ViewSiteViewModel @Inject constructor(
  private val app: Application,
  private val database: AppDatabase,
  private val notificationManager: NockNotificationManager,
  private val validationManager: ValidationManager,
  @field:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ScopedViewModel(), LifecycleObserver {

  lateinit var site: Site

  // Public properties
  val status = MutableLiveData<Status>()
  val name = MutableLiveData<String>()
  val url = MutableLiveData<String>()
  val timeout = MutableLiveData<Int>()
  val validationMode = MutableLiveData<ValidationMode>()
  val validationSearchTerm = MutableLiveData<String>()
  val validationScript = MutableLiveData<String>()
  val checkIntervalValue = MutableLiveData<Int>()
  val checkIntervalUnit = MutableLiveData<Long>()
  internal val disabled = MutableLiveData<Boolean>()
  internal val lastResult = MutableLiveData<ValidationResult?>()

  // Private properties
  private val isLoading = MutableLiveData<Boolean>()
  private val nameError = MutableLiveData<Int?>()
  private val urlError = MutableLiveData<Int?>()
  private val timeoutError = MutableLiveData<Int?>()
  private val validationSearchTermError = MutableLiveData<Int?>()
  private val validationScriptError = MutableLiveData<Int?>()
  private val checkIntervalValueError = MutableLiveData<Int?>()

  // Expose private properties or calculated properties
  @CheckResult fun onIsLoading(): LiveData<Boolean> = isLoading

  @CheckResult fun onNameError(): LiveData<Int?> = nameError

  @CheckResult fun onUrlError(): LiveData<Int?> = urlError

  @CheckResult fun onUrlWarningVisibility(): LiveData<Boolean> {
    return url.map {
      val parsed = HttpUrl.parse(it)
      return@map it.isNotEmpty() &&
          parsed != null &&
          parsed.scheme() != "http" &&
          parsed.scheme() != "https"
    }
  }

  @CheckResult fun onTimeoutError(): LiveData<Int?> = timeoutError

  @CheckResult fun onValidationModeDescription(): LiveData<Int> {
    return validationMode.map {
      when (it) {
        STATUS_CODE -> R.string.validation_mode_status_desc
        TERM_SEARCH -> R.string.validation_mode_term_desc
        JAVASCRIPT -> R.string.validation_mode_javascript_desc
        else -> throw IllegalStateException("Unknown validation mode: $it")
      }
    }
  }

  @CheckResult fun onValidationSearchTermError(): LiveData<Int?> = validationSearchTermError

  @CheckResult fun onValidationSearchTermVisibility() =
    validationMode.map { it == TERM_SEARCH }

  @CheckResult fun onValidationScriptError(): LiveData<Int?> = validationScriptError

  @CheckResult fun onValidationScriptVisibility() =
    validationMode.map { it == JAVASCRIPT }

  @CheckResult fun onCheckIntervalError(): LiveData<Int?> = checkIntervalValueError

  @CheckResult fun onDisableChecksVisibility(): LiveData<Boolean> =
    disabled.map { !it }

  @CheckResult fun onDoneButtonText(): LiveData<Int> =
    disabled.map {
      if (it) R.string.renable_and_save_changes
      else R.string.save_changes
    }

  @CheckResult fun onLastCheckResultText(): LiveData<String> = lastResult.map {
    if (it == null) {
      app.getString(R.string.none)
    } else {
      val statusText = it.status.textRes()
      if (statusText == 0) {
        it.reason
      } else {
        app.getString(statusText)
      }
    }
  }

  @CheckResult fun onNextCheckText(): LiveData<String> {
    return zip(disabled, lastResult)
        .map {
          val disabled = it.first
          val lastResult = it.second
          if (disabled) {
            app.getString(R.string.auto_checks_disabled)
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
      validationManager.scheduleCheck(
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
    validationManager.scheduleCheck(
        site = checkModel,
        rightNow = true,
        cancelPrevious = true
    )
  }

  fun removeSite(done: () -> Unit) {
    validationManager.cancelCheck(site)
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

  fun disable() {
    validationManager.cancelCheck(site)
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
  private fun getCheckIntervalMs(): Long {
    val value = checkIntervalValue.value ?: return 0
    val unit = checkIntervalUnit.value ?: return 0
    return value * unit
  }

  private fun getValidationArgs(): String? {
    return when (validationMode.value) {
      TERM_SEARCH -> validationSearchTerm.value
      JAVASCRIPT -> validationScript.value
      else -> null
    }
  }

  private fun getUpdatedDbModel(): Site? {
    var errorCount = 0

    // Validation name
    if (name.value.isNullOrEmpty()) {
      nameError.value = R.string.please_enter_name
      errorCount++
    } else {
      nameError.value = null
    }

    // Validate URL
    when {
      url.value.isNullOrEmpty() -> {
        urlError.value = R.string.please_enter_url
        errorCount++
      }
      HttpUrl.parse(url.value!!) == null -> {
        urlError.value = R.string.please_enter_valid_url
        errorCount++
      }
      else -> {
        urlError.value = null
      }
    }

    // Validate timeout
    if (timeout.value.isNullOrLessThan(1)) {
      timeoutError.value = R.string.please_enter_networkTimeout
      errorCount++
    } else {
      timeoutError.value = null
    }

    // Validate check interval
    if (checkIntervalValue.value.isNullOrLessThan(1)) {
      checkIntervalValueError.value = R.string.please_enter_check_interval
      errorCount++
    } else {
      checkIntervalValueError.value = null
    }

    // Validate arguments
    if (validationMode == TERM_SEARCH &&
        validationSearchTerm.value.isNullOrEmpty()
    ) {
      errorCount++
      validationSearchTermError.value = R.string.please_enter_search_term
      validationScriptError.value = null
    } else if (validationMode == JAVASCRIPT &&
        validationScript.value.isNullOrEmpty()
    ) {
      errorCount++
      validationSearchTermError.value = null
      validationScriptError.value = R.string.please_enter_javaScript
    } else {
      validationSearchTermError.value = null
      validationScriptError.value = null
    }

    if (errorCount > 0) {
      return null
    }

    val newSettings = site.settings!!.copy(
        validationIntervalMs = getCheckIntervalMs(),
        validationMode = validationMode.value!!,
        validationArgs = getValidationArgs(),
        networkTimeout = timeout.value!!,
        disabled = false
    )
    return site.copy(
        name = name.value!!,
        url = url.value!!,
        settings = newSettings
    )
        .withStatus(status = WAITING)
  }
}
