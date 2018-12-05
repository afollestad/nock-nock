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

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.engine.statuscheck.ValidationJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.ValidationJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.engine.statuscheck.ValidationManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.ui.main.MainView
import com.afollestad.nocknock.ui.main.RealMainPresenter
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class MainPresenterTest {

  private val prefs = mock<SharedPreferences> {
    on { getBoolean("did_db_migration", false) } doReturn true
  }
  private val app = mock<Application> {
    on { getSharedPreferences("settings", MODE_PRIVATE) } doReturn prefs
  }

  private val database = mockDatabase()

  private val notificationManager = mock<NockNotificationManager>()
  private val checkStatusManager = mock<ValidationManager>()
  private val view = mock<MainView>()

  private val presenter = RealMainPresenter(
      app,
      database,
      notificationManager,
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

  @Test fun onBroadcast() {
    val badIntent = fakeIntent("Hello World")
    presenter.onBroadcast(badIntent)

    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_UPDATE_MODEL))
        .doReturn(MOCK_MODEL_2)

    presenter.onBroadcast(goodIntent)
    verify(view, times(1)).updateModel(MOCK_MODEL_2)
  }

  @Test fun resume() = runBlocking {
    presenter.resume()

    verify(notificationManager).cancelStatusNotifications()

    val modelsCaptor = argumentCaptor<List<Site>>()
    verify(view, times(2)).setModels(modelsCaptor.capture())
    assertThat(modelsCaptor.firstValue).isEmpty()
    assertThat(modelsCaptor.lastValue).isEqualTo(ALL_MOCK_MODELS)
  }

  @Test fun refreshSite() {
    presenter.refreshSite(MOCK_MODEL_3)

    verify(checkStatusManager).scheduleCheck(
        site = MOCK_MODEL_3,
        rightNow = true,
        cancelPrevious = true
    )
  }

  @Test fun removeSite() = runBlocking {
    presenter.removeSite(MOCK_MODEL_1)

    verify(checkStatusManager).cancelCheck(MOCK_MODEL_1)
    verify(notificationManager).cancelStatusNotification(MOCK_MODEL_1)
    verify(database.siteDao()).delete(MOCK_MODEL_1)
    verify(database.siteSettingsDao()).delete(MOCK_MODEL_1.settings!!)
    verify(view).onSiteDeleted(MOCK_MODEL_1)
  }

  private fun fakeIntent(action: String): Intent {
    return mock {
      on { getAction() } doReturn action
    }
  }
}
