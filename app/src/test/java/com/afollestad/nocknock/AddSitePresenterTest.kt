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
package com.afollestad.nocknock

import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.statuscheck.ValidationManager
import com.afollestad.nocknock.ui.addsite.AddSiteView
import com.afollestad.nocknock.ui.addsite.InputErrors
import com.afollestad.nocknock.ui.addsite.RealAddSitePresenter
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class AddSitePresenterTest {

  private val database = mockDatabase()
  private val checkStatusManager = mock<ValidationManager>()
  private val view = mock<AddSiteView>()

  private val presenter = RealAddSitePresenter(
      database,
      checkStatusManager
  )

  @Before fun setup() {
    doAnswer {
      val exec = it.getArgument<ScopeReceiver>(1)
      runBlocking { exec() }
      Unit
    }.whenever(view)
        .scopeWhileAttached(any(), any())

    presenter.takeView(view)
  }

  @After fun destroy() {
    presenter.dropView()
  }

  @Test fun onUrlInputFocusChange_focused() {
    presenter.onUrlInputFocusChange(true, "hello")
    verifyNoMoreInteractions(view)
  }

  @Test fun onUrlInputFocusChange_empty() {
    presenter.onUrlInputFocusChange(false, "")
    verifyNoMoreInteractions(view)
  }

  @Test fun onUrlInputFocusChange_notHttpHttps() {
    presenter.onUrlInputFocusChange(false, "ftp://hello.com")
    verify(view).showOrHideUrlSchemeWarning(true)
  }

  @Test fun onUrlInputFocusChange_isHttpOrHttps() {
    presenter.onUrlInputFocusChange(false, "http://hello.com")
    presenter.onUrlInputFocusChange(false, "https://hello.com")
    verify(view, times(2)).showOrHideUrlSchemeWarning(false)
  }

  @Test fun onValidationModeSelected_statusCode() {
    presenter.onValidationModeSelected(0)
    verify(view).showOrHideValidationSearchTerm(false)
    verify(view).showOrHideScriptInput(false)
    verify(view).setValidationModeDescription(R.string.validation_mode_status_desc)
  }

  @Test fun onValidationModeSelected_termSearch() {
    presenter.onValidationModeSelected(1)
    verify(view).showOrHideValidationSearchTerm(true)
    verify(view).showOrHideScriptInput(false)
    verify(view).setValidationModeDescription(R.string.validation_mode_term_desc)
  }

  @Test fun onValidationModeSelected_javaScript() {
    presenter.onValidationModeSelected(2)
    verify(view).showOrHideValidationSearchTerm(false)
    verify(view).showOrHideScriptInput(true)
    verify(view).setValidationModeDescription(R.string.validation_mode_javascript_desc)
  }

  @Test(expected = IllegalStateException::class)
  fun onValidationModeSelected_other() {
    presenter.onValidationModeSelected(3)
  }

  @Test fun commit_nameError() {
    presenter.commit(
        "",
        "https://test.com",
        1,
        STATUS_CODE,
        null,
        60000
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.name).isEqualTo(R.string.please_enter_name)
  }

  @Test fun commit_urlEmptyError() {
    presenter.commit(
        "Testing",
        "",
        1,
        STATUS_CODE,
        null,
        60000
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.url).isEqualTo(R.string.please_enter_url)
  }

  @Test fun commit_urlFormatError() {
    presenter.commit(
        "Testing",
        "ftp://hello.com",
        1,
        STATUS_CODE,
        null,
        60000
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.url).isEqualTo(R.string.please_enter_valid_url)
  }

  @Test fun commit_checkIntervalError() {
    presenter.commit(
        "Testing",
        "https://hello.com",
        -1,
        STATUS_CODE,
        null,
        60000
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.checkInterval).isEqualTo(R.string.please_enter_check_interval)
  }

  @Test fun commit_termSearchError() {
    presenter.commit(
        "Testing",
        "https://hello.com",
        1,
        TERM_SEARCH,
        null,
        60000
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.termSearch).isEqualTo(R.string.please_enter_search_term)
  }

  @Test fun commit_networkTimeout_error() {
    presenter.commit(
        "Testing",
        "https://hello.com",
        1,
        STATUS_CODE,
        null,
        0
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.networkTimeout).isEqualTo(R.string.please_enter_networkTimeout)
  }

  @Test fun commit_javaScript_error() {
    presenter.commit(
        "Testing",
        "https://hello.com",
        1,
        JAVASCRIPT,
        null,
        60000
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.javaScript).isEqualTo(R.string.please_enter_javaScript)
  }

  @Test fun commit_success() = runBlocking {
    presenter.commit(
        "Testing",
        "https://hello.com",
        1,
        STATUS_CODE,
        null,
        60000
    )

    val siteCaptor = argumentCaptor<Site>()
    val settingsCaptor = argumentCaptor<SiteSettings>()

    verify(view).setLoading()
    verify(database.siteDao()).insert(siteCaptor.capture())
    verify(database.siteSettingsDao()).insert(settingsCaptor.capture())
    verify(database.validationResultsDao(), never()).insert(any())

    val settings = settingsCaptor.firstValue
    val model = siteCaptor.firstValue.copy(
        id = 1, // fill it in because our insert captor doesn't catch this
        settings = settings,
        lastResult = null
    )

    verify(view, never()).setInputErrors(any())
    verify(checkStatusManager).scheduleCheck(
        site = model,
        rightNow = true,
        cancelPrevious = true,
        fromFinishingJob = false
    )

    verify(view).setDoneLoading()
    verify(view).onSiteAdded()
  }
}
