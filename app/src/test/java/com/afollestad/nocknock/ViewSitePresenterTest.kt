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

import android.content.Intent
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.model.ValidationResult
import com.afollestad.nocknock.engine.statuscheck.ValidationJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.ValidationJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.engine.statuscheck.ValidationManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.ui.viewsite.InputErrors
import com.afollestad.nocknock.ui.viewsite.KEY_VIEW_MODEL
import com.afollestad.nocknock.ui.viewsite.RealViewSitePresenter
import com.afollestad.nocknock.ui.viewsite.ViewSiteView
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
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
import java.lang.System.currentTimeMillis

class ViewSitePresenterTest {

  private val database = mockDatabase()
  private val checkStatusManager = mock<ValidationManager>()
  private val notificationManager = mock<NockNotificationManager>()
  private val view = mock<ViewSiteView>()

  private val presenter = RealViewSitePresenter(
      database,
      checkStatusManager,
      notificationManager
  )

  @Before fun setup() {
    doAnswer {
      val exec = it.getArgument<ScopeReceiver>(1)
      runBlocking { exec() }
      Unit
    }.whenever(view)
        .scopeWhileAttached(any(), any())

    val intent = fakeIntent("")
    whenever(intent.getSerializableExtra(KEY_VIEW_MODEL))
        .doReturn(MOCK_MODEL_1)
    presenter.takeView(view, intent)
    assertThat(presenter.currentModel()).isEqualTo(MOCK_MODEL_1)
    verify(view, times(1)).displayModel(MOCK_MODEL_1)
  }

  @After fun destroy() {
    presenter.dropView()
  }

  @Test fun onBroadcast() {
    val badIntent = fakeIntent("Hello World")
    presenter.onBroadcast(badIntent)

    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_UPDATE_MODEL))
        .doReturn(MOCK_MODEL_2)

    presenter.onBroadcast(goodIntent)
    assertThat(presenter.currentModel()).isEqualTo(MOCK_MODEL_2)
    verify(view, times(1)).displayModel(MOCK_MODEL_2)
  }

  @Test fun onNewIntent() {
    val badIntent = fakeIntent(ACTION_STATUS_UPDATE)
    presenter.onBroadcast(badIntent)

    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_VIEW_MODEL))
        .doReturn(MOCK_MODEL_3)
    presenter.onBroadcast(goodIntent)

    verify(view, times(1)).displayModel(MOCK_MODEL_3)
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

  @Test fun commit_success() = runBlocking {
    val name = "Testing"
    val url = "https://hello.com"
    val checkInterval = 60000L
    val validationMode = TERM_SEARCH
    val validationArgs = "Hello World"

    val currentModel = presenter.currentModel()
    val initialLastResult = ValidationResult(
        siteId = currentModel.id,
        timestampMs = currentTimeMillis() - 60000,
        status = ERROR,
        reason = "Oh no!"
    )
    val disabledModel = currentModel.copy(
        settings = currentModel.settings!!.copy(disabled = true),
        lastResult = initialLastResult
    )
    presenter.setModel(disabledModel)

    presenter.commit(
        name,
        url,
        checkInterval,
        validationMode,
        validationArgs,
        60000
    )

    val siteCaptor = argumentCaptor<Site>()
    val settingsCaptor = argumentCaptor<SiteSettings>()
    val resultCaptor = argumentCaptor<ValidationResult>()

    verify(view).setLoading()
    verify(database.siteDao()).update(siteCaptor.capture())
    verify(database.siteSettingsDao()).update(settingsCaptor.capture())
    verify(database.validationResultsDao()).update(resultCaptor.capture())

    val model = siteCaptor.firstValue
    model.apply {
      assertThat(this.name).isEqualTo(name)
      assertThat(this.url).isEqualTo(url)
    }

    val settings = settingsCaptor.firstValue
    settings.apply {
      assertThat(this.validationIntervalMs).isEqualTo(checkInterval)
      assertThat(this.validationArgs).isEqualTo(validationArgs)
      assertThat(this.disabled).isFalse()
    }

    val result = resultCaptor.firstValue
    result.apply {
      assertThat(this.status).isEqualTo(WAITING)
      assertThat(this.reason).isNull()
      assertThat(this.timestampMs).isGreaterThan(0)
    }

    verify(view, never()).setInputErrors(any())
    verify(checkStatusManager).scheduleCheck(
        site = model,
        rightNow = true,
        cancelPrevious = true,
        fromFinishingJob = false
    )
    verify(view).setDoneLoading()
    verify(view).finish()
  }

  @Test fun checkNow() {
    val newModel = presenter.currentModel()
        .withStatus(status = WAITING)
    presenter.checkNow()

    verify(view, never()).setLoading()
    verify(view).displayModel(newModel)
    verify(checkStatusManager).scheduleCheck(
        site = newModel,
        rightNow = true,
        cancelPrevious = true
    )
  }

  @Test fun disableChecks() = runBlocking {
    val model = presenter.currentModel()
    presenter.disableChecks()

    verify(checkStatusManager).cancelCheck(model)
    verify(notificationManager).cancelStatusNotification(model)
    verify(view).setLoading()

    val modelCaptor = argumentCaptor<Site>()
    val settingsCaptor = argumentCaptor<SiteSettings>()
    val resultCaptor = argumentCaptor<ValidationResult>()

    verify(database.siteDao()).update(modelCaptor.capture())
    verify(database.siteSettingsDao()).update(settingsCaptor.capture())
    verify(database.validationResultsDao()).update(resultCaptor.capture())

    val newModel = modelCaptor.firstValue
    val newSettings = settingsCaptor.firstValue
    val result = resultCaptor.firstValue
    assertThat(newSettings.disabled).isTrue()

    verify(view).setDoneLoading()
    verify(view, times(1)).displayModel(newModel)
  }

  @Test fun removeSite() = runBlocking {
    val model = presenter.currentModel()
    presenter.removeSite()

    verify(checkStatusManager).cancelCheck(model)
    verify(notificationManager).cancelStatusNotification(model)
    verify(view).setLoading()

    verify(database.siteSettingsDao()).delete(model.settings!!)
    verify(database.validationResultsDao()).delete(model.lastResult!!)
    verify(database.siteDao()).delete(model)

    verify(view).setDoneLoading()
    verify(view).finish()
  }

  private fun fakeIntent(action: String): Intent {
    return mock {
      on { getAction() } doReturn action
    }
  }
}
