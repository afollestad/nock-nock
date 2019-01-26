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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.afollestad.nocknock.MOCK_MODEL_1
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.Status.CHECKING
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.model.ValidationResult
import com.afollestad.nocknock.engine.validation.ValidationExecutor
import com.afollestad.nocknock.mockDatabase
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.utilities.livedata.test
import com.afollestad.nocknock.utilities.providers.StringProvider
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
@ExperimentalCoroutinesApi
class ViewSiteViewModelTest {

  companion object {
    private const val TEXT_NONE = "None"
    private const val TEXT_EVERYTHING_CHECKS_OUT = "Everything checks out!"
    private const val TEXT_WAITING = "Waiting..."
    private const val TEXT_CHECKING = "Checking..."
    private const val TEXT_CHECKS_DISABLED = "Automatic Checks Disabled"
  }

  private val stringProvider = mock<StringProvider> {
    on { get(any()) } doAnswer { inv ->
      val id = inv.getArgument<Int>(0)
      when (id) {
        R.string.none -> TEXT_NONE
        R.string.everything_checks_out -> TEXT_EVERYTHING_CHECKS_OUT
        R.string.waiting -> TEXT_WAITING
        R.string.checking_status -> TEXT_CHECKING
        R.string.auto_checks_disabled -> TEXT_CHECKS_DISABLED
        else -> ""
      }
    }
  }
  private val database = mockDatabase()
  private val validationManager = mock<ValidationExecutor>()
  private val notificationManager = mock<NockNotificationManager>()

  @Rule @JvmField val rule = InstantTaskExecutorRule()

  private val viewModel = ViewSiteViewModel(
      stringProvider,
      database,
      notificationManager,
      validationManager,
      Dispatchers.Unconfined,
      Dispatchers.Unconfined
  )

  @After fun tearDown() = viewModel.destroy()

  @Test fun onUrlWarningVisibility() {
    val urlWarningVisibility = viewModel.onUrlWarningVisibility()
        .test()

    viewModel.url.value = ""
    urlWarningVisibility.assertValues(false)

    viewModel.url.value = "helloworld"
    urlWarningVisibility.assertValues(true)

    viewModel.url.value = "http://helloworld.com"
    urlWarningVisibility.assertValues(false)

    viewModel.url.value = "ftp://helloworld.com"
    urlWarningVisibility.assertValues(true)
  }

  @Test fun onValidationModeDescription() {
    val description = viewModel.onValidationModeDescription()
        .test()

    viewModel.validationMode.value = STATUS_CODE
    description.assertValues(R.string.validation_mode_status_desc)

    viewModel.validationMode.value = TERM_SEARCH
    description.assertValues(R.string.validation_mode_term_desc)

    viewModel.validationMode.value = JAVASCRIPT
    description.assertValues(R.string.validation_mode_javascript_desc)
  }

  @Test fun onValidationSearchTermVisibility() {
    val visibility = viewModel.onValidationSearchTermVisibility()
        .test()

    viewModel.validationMode.value = STATUS_CODE
    visibility.assertValues(false)

    viewModel.validationMode.value = TERM_SEARCH
    visibility.assertValues(true)

    viewModel.validationMode.value = JAVASCRIPT
    visibility.assertValues(false)
  }

  @Test fun onValidationScriptVisibility() {
    val visibility = viewModel.onValidationScriptVisibility()
        .test()

    viewModel.validationMode.value = STATUS_CODE
    visibility.assertValues(false)

    viewModel.validationMode.value = TERM_SEARCH
    visibility.assertValues(false)

    viewModel.validationMode.value = JAVASCRIPT
    visibility.assertValues(true)
  }

  @Test fun onDisableChecksVisibility() {
    val visibility = viewModel.onDisableChecksVisibility()
        .test()

    viewModel.disabled.value = false
    visibility.assertValues(true)

    viewModel.disabled.value = true
    visibility.assertValues(false)
  }

  @Test fun onDoneButtonText() {
    val text = viewModel.onDoneButtonText()
        .test()

    viewModel.disabled.value = false
    text.assertValues(R.string.save_changes)

    viewModel.disabled.value = true
    text.assertValues(R.string.renable_and_save_changes)
  }

  @Test fun onLastCheckResultText() {
    val text = viewModel.onLastCheckResultText()
        .test()
    val lastResult = ValidationResult(
        siteId = 1,
        timestampMs = 10,
        status = OK,
        reason = "Hello, world!"
    )

    viewModel.lastResult.value = null
    text.assertValues(TEXT_NONE)

    viewModel.lastResult.value = lastResult
    text.assertValues(TEXT_EVERYTHING_CHECKS_OUT)

    viewModel.lastResult.value = lastResult.copy(status = WAITING)
    text.assertValues(TEXT_WAITING)

    viewModel.lastResult.value = lastResult.copy(status = CHECKING)
    text.assertValues(TEXT_CHECKING)

    viewModel.lastResult.value = lastResult.copy(
        status = ERROR,
        reason = "Uh oh!"
    )
    text.assertValues("Uh oh!")
  }

  @Test fun onNextCheckText() {
    viewModel.checkIntervalValue.value = 60
    viewModel.checkIntervalUnit.value = 5000

    val text = viewModel.onNextCheckText()
        .test()
    val calendar = Calendar.getInstance()
        .apply {
          set(Calendar.YEAR, 2018)
          set(Calendar.MONTH, Calendar.DECEMBER)
          set(Calendar.DAY_OF_MONTH, 6)
          set(Calendar.HOUR_OF_DAY, 8)
          set(Calendar.MINUTE, 30)
          set(Calendar.SECOND, 0)
        }
    val lastResult = ValidationResult(
        siteId = 1,
        timestampMs = calendar.timeInMillis,
        status = OK,
        reason = null
    )

    viewModel.disabled.value = true
    viewModel.lastResult.value = lastResult
    text.assertValues(TEXT_CHECKS_DISABLED)

    viewModel.disabled.value = false
    text.assertValues("December 6, 8:35 AM")
  }

  @Test fun getCheckIntervalMs() {
    viewModel.checkIntervalValue.value = 3
    viewModel.checkIntervalUnit.value = 200
    Truth.assertThat(viewModel.getCheckIntervalMs())
        .isEqualTo(600L)
  }

  @Test fun getValidationArgs() {
    viewModel.validationSearchTerm.value = "One"
    viewModel.validationScript.value = "Two"

    viewModel.validationMode.value = STATUS_CODE
    Truth.assertThat(viewModel.getValidationArgs())
        .isNull()

    viewModel.validationMode.value = TERM_SEARCH
    Truth.assertThat(viewModel.getValidationArgs())
        .isEqualTo("One")

    viewModel.validationMode.value = JAVASCRIPT
    Truth.assertThat(viewModel.getValidationArgs())
        .isEqualTo("Two")
  }

  @Test fun commit_success() = runBlocking {
    val isLoading = viewModel.onIsLoading()
        .test()

    fillInModel()
    val onDone = mock<() -> Unit>()

    viewModel.site = MOCK_MODEL_1
    viewModel.commit(onDone)

    val siteCaptor = argumentCaptor<Site>()
    val settingsCaptor = argumentCaptor<SiteSettings>()
    val resultCaptor = argumentCaptor<ValidationResult>()

    isLoading.assertValues(true, false)
    verify(database.siteDao()).update(siteCaptor.capture())
    verify(database.siteSettingsDao()).update(settingsCaptor.capture())
    verify(database.validationResultsDao()).update(resultCaptor.capture())

    // From fillInModel() below
    val updatedSettings = MOCK_MODEL_1.settings!!.copy(
        networkTimeout = 30000,
        validationMode = JAVASCRIPT,
        validationArgs = "throw 'Oh no!'",
        disabled = false,
        validationIntervalMs = 24 * 60000
    )
    val updatedResult = MOCK_MODEL_1.lastResult!!.copy(
        status = WAITING
    )
    val updatedModel = MOCK_MODEL_1.copy(
        name = "Hello There",
        url = "https://www.hellothere.com",
        settings = updatedSettings,
        lastResult = updatedResult
    )

    assertThat(siteCaptor.firstValue).isEqualTo(updatedModel)
    assertThat(settingsCaptor.firstValue).isEqualTo(updatedSettings)
    assertThat(resultCaptor.firstValue).isEqualTo(updatedResult)

    verify(validationManager).scheduleValidation(
        site = updatedModel,
        rightNow = true,
        cancelPrevious = true,
        fromFinishingJob = false
    )

    verify(onDone).invoke()
  }

  @Test fun checkNow() {
    val status = viewModel.status.test()

    viewModel.site = MOCK_MODEL_1
    val expectedModel = MOCK_MODEL_1.copy(
        lastResult = MOCK_MODEL_1.lastResult!!.copy(
            status = WAITING
        )
    )

    viewModel.checkNow()
    verify(validationManager).scheduleValidation(
        site = expectedModel,
        rightNow = true,
        cancelPrevious = true
    )
    status.assertValues(WAITING)
  }

  @Test fun removeSite() {
    val isLoading = viewModel.onIsLoading()
        .test()
    val onDone = mock<() -> Unit>()

    viewModel.site = MOCK_MODEL_1
    viewModel.removeSite(onDone)
    isLoading.assertValues(true, false)

    verify(validationManager).cancelScheduledValidation(MOCK_MODEL_1)
    verify(notificationManager).cancelStatusNotification(MOCK_MODEL_1)
    verify(database.siteDao()).delete(MOCK_MODEL_1)
    verify(database.siteSettingsDao()).delete(MOCK_MODEL_1.settings!!)
    verify(database.validationResultsDao()).delete(MOCK_MODEL_1.lastResult!!)
    verify(onDone).invoke()
  }

  @Test fun disableSite() {
    val isLoading = viewModel.onIsLoading()
        .test()
    val disabled = viewModel.disabled.test()

    viewModel.site = MOCK_MODEL_1
    viewModel.disableSite()
    isLoading.assertValues(true, false)
    disabled.assertValues(true)

    val expectedSite = MOCK_MODEL_1.copy(
        settings = MOCK_MODEL_1.settings!!.copy(
            disabled = true
        )
    )

    verify(validationManager).cancelScheduledValidation(MOCK_MODEL_1)
    verify(notificationManager).cancelStatusNotification(MOCK_MODEL_1)
    verify(database.siteDao()).update(expectedSite)
    verify(database.siteSettingsDao()).update(expectedSite.settings!!)
    verify(database.validationResultsDao()).update(expectedSite.lastResult!!)
  }

  private fun fillInModel() = viewModel.apply {
    name.value = "Hello There"
    url.value = "https://www.hellothere.com"
    timeout.value = 30000
    validationMode.value = JAVASCRIPT
    validationSearchTerm.value = null
    validationScript.value = "throw 'Oh no!'"
    checkIntervalValue.value = 24
    checkIntervalUnit.value = 60000
  }
}
