/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock

import android.content.Intent
import com.afollestad.nocknock.data.LAST_CHECK_NONE
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.WAITING
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
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

class ViewSitePresenterTest {

  private val serverModelStore = mock<ServerModelStore> {
    on { runBlocking { put(any()) } } doAnswer { inv ->
      inv.getArgument<ServerModel>(0)
    }
  }
  private val checkStatusManager = mock<CheckStatusManager>()
  private val notificationManager = mock<NockNotificationManager>()
  private val view = mock<ViewSiteView>()

  private val presenter = RealViewSitePresenter(
      serverModelStore,
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

    val model = fakeModel()
    val intent = fakeIntent("")
    whenever(intent.getSerializableExtra(KEY_VIEW_MODEL))
        .doReturn(model)
    presenter.takeView(view, intent)
    assertThat(presenter.currentModel()).isEqualTo(model)
    verify(view, times(1)).displayModel(model)
  }

  @After fun destroy() {
    presenter.dropView()
  }

  @Test fun onBroadcast() {
    val badIntent = fakeIntent("Hello World")
    presenter.onBroadcast(badIntent)

    val model = fakeModel().copy(lastCheck = 0)
    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_UPDATE_MODEL))
        .doReturn(model)

    presenter.onBroadcast(goodIntent)
    assertThat(presenter.currentModel()).isEqualTo(model)
    verify(view, times(1)).displayModel(model)
  }

  @Test fun onNewIntent() {
    val badIntent = fakeIntent(ACTION_STATUS_UPDATE)
    presenter.onBroadcast(badIntent)

    val model = fakeModel().copy(lastCheck = 0)
    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_VIEW_MODEL))
        .doReturn(model)
    presenter.onBroadcast(goodIntent)

    verify(view, times(1)).displayModel(model)
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
        null
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
        null
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
        null
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
        null
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
        null
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
        null
    )

    val inputErrorsCaptor = argumentCaptor<InputErrors>()
    verify(view).setInputErrors(inputErrorsCaptor.capture())
    verify(checkStatusManager, never())
        .scheduleCheck(any(), any(), any(), any())

    val errors = inputErrorsCaptor.firstValue
    assertThat(errors.javaScript).isEqualTo(R.string.please_enter_javaScript)
  }

  @Test fun commit_success() = runBlocking {
    val name = "Testing"
    val url = "https://hello.com"
    val checkInterval = 60000L
    val validationMode = TERM_SEARCH
    val validationContent = "Hello World"

    val disabledModel = presenter.currentModel()
        .copy(disabled = true)
    presenter.setModel(disabledModel)

    presenter.commit(
        name,
        url,
        checkInterval,
        validationMode,
        validationContent
    )

    val modelCaptor = argumentCaptor<ServerModel>()
    verify(view).setLoading()
    verify(serverModelStore).update(modelCaptor.capture())

    val model = modelCaptor.firstValue
    assertThat(model.name).isEqualTo(name)
    assertThat(model.url).isEqualTo(url)
    assertThat(model.checkInterval).isEqualTo(checkInterval)
    assertThat(model.validationMode).isEqualTo(validationMode)
    assertThat(model.validationContent).isEqualTo(validationContent)
    assertThat(model.disabled).isFalse()

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
        .copy(
            status = WAITING
        )
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

    val modelCaptor = argumentCaptor<ServerModel>()
    verify(serverModelStore).update(modelCaptor.capture())
    val newModel = modelCaptor.firstValue
    assertThat(newModel.disabled).isTrue()
    assertThat(newModel.lastCheck).isEqualTo(LAST_CHECK_NONE)

    verify(view).setDoneLoading()
    verify(view, times(1)).displayModel(newModel)
  }

  @Test fun removeSite() = runBlocking {
    val model = presenter.currentModel()
    presenter.removeSite()

    verify(checkStatusManager).cancelCheck(model)
    verify(notificationManager).cancelStatusNotification(model)
    verify(view).setLoading()
    verify(serverModelStore).delete(model)
    verify(view).setDoneLoading()
    verify(view).finish()
  }

  private fun fakeModel() = ServerModel(
      id = 1,
      name = "Test",
      url = "https://test.com",
      validationMode = STATUS_CODE
  )

  private fun fakeIntent(action: String): Intent {
    return mock {
      on { getAction() } doReturn action
    }
  }
}