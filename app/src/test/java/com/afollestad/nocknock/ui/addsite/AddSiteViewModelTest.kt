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
package com.afollestad.nocknock.ui.addsite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.validation.ValidationManager
import com.afollestad.nocknock.mockDatabase
import com.afollestad.nocknock.test
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test

/** @author Aidan Follestad (@afollestad) */
@ExperimentalCoroutinesApi
class AddSiteViewModelTest {

  private val database = mockDatabase()
  private val validationManager = mock<ValidationManager>()

  @Rule @JvmField val rule = InstantTaskExecutorRule()

  private val viewModel = AddSiteViewModel(
      database,
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

  @Test fun getCheckIntervalMs() {
    viewModel.checkIntervalValue.value = 3
    viewModel.checkIntervalUnit.value = 200
    assertThat(viewModel.getCheckIntervalMs()).isEqualTo(600L)
  }

  @Test fun getValidationArgs() {
    viewModel.validationSearchTerm.value = "One"
    viewModel.validationScript.value = "Two"

    viewModel.validationMode.value = STATUS_CODE
    assertThat(viewModel.getValidationArgs()).isNull()

    viewModel.validationMode.value = TERM_SEARCH
    assertThat(viewModel.getValidationArgs()).isEqualTo("One")

    viewModel.validationMode.value = JAVASCRIPT
    assertThat(viewModel.getValidationArgs()).isEqualTo("Two")
  }

  @Test fun commit_nameError() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      name.value = ""
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertValues(R.string.please_enter_name)
    onUrlError.assertNoValues()
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertNoValues()
    onScriptError.assertNoValues()

    verify(onDone, never()).invoke()
  }

  @Test fun commit_urlEmptyError() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      url.value = ""
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertNoValues()
    onUrlError.assertValues(R.string.please_enter_url)
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertNoValues()
    onScriptError.assertNoValues()

    verify(onDone, never()).invoke()
  }

  @Test fun commit_urlFormatError() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      url.value = "ftp://www.idk.com"
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertNoValues()
    onUrlError.assertValues(R.string.please_enter_valid_url)
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertNoValues()
    onScriptError.assertNoValues()

    verify(onDone, never()).invoke()
  }

  @Test fun commit_networkTimeout_error() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      timeout.value = 0
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertNoValues()
    onUrlError.assertNoValues()
    onTimeoutError.assertValues(R.string.please_enter_networkTimeout)
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertNoValues()
    onScriptError.assertNoValues()

    verify(onDone, never()).invoke()
  }

  @Test fun commit_checkIntervalError() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      checkIntervalValue.value = 0
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertNoValues()
    onUrlError.assertNoValues()
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertValues(R.string.please_enter_check_interval)
    onSearchTermError.assertNoValues()
    onScriptError.assertNoValues()

    verify(onDone, never()).invoke()
  }

  @Test fun commit_termSearchError() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      validationMode.value = TERM_SEARCH
      validationSearchTerm.value = ""
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertNoValues()
    onUrlError.assertNoValues()
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertValues(R.string.please_enter_search_term)
    onScriptError.assertNoValues()

    verify(onDone, never()).invoke()
  }

  @Test fun commit_javaScript_error() {
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()

    fillInModel().apply {
      validationMode.value = JAVASCRIPT
      validationScript.value = ""
    }
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    verify(validationManager, never())
        .scheduleCheck(any(), any(), any(), any())
    onNameError.assertNoValues()
    onUrlError.assertNoValues()
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertNoValues()
    onScriptError.assertValues(R.string.please_enter_javaScript)

    verify(onDone, never()).invoke()
  }

  @Test fun commit_success() = runBlocking {
    val isLoading = viewModel.onIsLoading()
        .test()
    val onNameError = viewModel.onNameError()
        .test()
    val onUrlError = viewModel.onUrlError()
        .test()
    val onTimeoutError = viewModel.onTimeoutError()
        .test()
    val onSearchTermError = viewModel.onValidationSearchTermError()
        .test()
    val onScriptError = viewModel.onValidationScriptError()
        .test()
    val onCheckIntervalError = viewModel.onCheckIntervalError()
        .test()

    fillInModel()
    val onDone = mock<() -> Unit>()
    viewModel.commit(onDone)

    val siteCaptor = argumentCaptor<Site>()
    val settingsCaptor = argumentCaptor<SiteSettings>()

    isLoading.assertValues(true, false)
    verify(database.siteDao()).insert(siteCaptor.capture())
    verify(database.siteSettingsDao()).insert(settingsCaptor.capture())
    verify(database.validationResultsDao(), never()).insert(any())

    val settings = settingsCaptor.firstValue
    val model = siteCaptor.firstValue.copy(
        id = 1, // fill it in because our insert captor doesn't catch this
        settings = settings,
        lastResult = null
    )

    verify(validationManager).scheduleCheck(
        site = model,
        rightNow = true,
        cancelPrevious = true,
        fromFinishingJob = false
    )
    onNameError.assertNoValues()
    onUrlError.assertNoValues()
    onTimeoutError.assertNoValues()
    onCheckIntervalError.assertNoValues()
    onSearchTermError.assertNoValues()
    onScriptError.assertNoValues()

    verify(onDone).invoke()
  }

  private fun fillInModel() = viewModel.apply {
    name.value = "Welcome to Wakanda"
    url.value = "https://www.wakanda.gov"
    timeout.value = 10000
    validationMode.value = TERM_SEARCH
    validationSearchTerm.value = "T'Challa"
    validationScript.value = null
    checkIntervalValue.value = 60
    checkIntervalUnit.value = 1000
  }
}
