/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock

import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
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

  private val serverModelStore = mock<ServerModelStore> {
    on { runBlocking { put(any()) } } doAnswer { inv ->
      inv.getArgument<ServerModel>(0)
    }
  }
  private val checkStatusManager = mock<CheckStatusManager>()
  private val view = mock<AddSiteView>()

  private val presenter = RealAddSitePresenter(
      serverModelStore,
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

    val modelCaptor = argumentCaptor<ServerModel>()
    verify(view).setLoading()
    verify(serverModelStore).put(modelCaptor.capture())
    val model = modelCaptor.firstValue
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
