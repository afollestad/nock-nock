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
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
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

  private val serverModelStore = mock<ServerModelStore>()
  private val notificationManager = mock<NockNotificationManager>()
  private val checkStatusManager = mock<CheckStatusManager>()
  private val view = mock<MainView>()

  private val presenter = RealMainPresenter(
      serverModelStore,
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

    val model = fakeModel()
    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_UPDATE_MODEL))
        .doReturn(model)

    presenter.onBroadcast(goodIntent)
    verify(view, times(1)).updateModel(model)
  }

  @Test fun resume() = runBlocking {
    val model = fakeModel()
    whenever(serverModelStore.get()).doReturn(listOf(model))
    presenter.resume()

    verify(notificationManager).cancelStatusNotifications()

    val modelsCaptor = argumentCaptor<List<ServerModel>>()
    verify(view, times(2)).setModels(modelsCaptor.capture())
    assertThat(modelsCaptor.firstValue).isEmpty()
    assertThat(modelsCaptor.lastValue.single()).isEqualTo(model)
  }

  @Test fun refreshSite() {
    val model = fakeModel()
    presenter.refreshSite(model)

    verify(checkStatusManager).scheduleCheck(
        site = model,
        rightNow = true,
        cancelPrevious = true
    )
  }

  @Test fun removeSite() = runBlocking {
    val model = fakeModel()
    presenter.removeSite(model)

    verify(checkStatusManager).cancelCheck(model)
    verify(notificationManager).cancelStatusNotification(model)
    verify(serverModelStore).delete(model)
    verify(view).onSiteDeleted(model)
  }

  private fun fakeModel() = ServerModel(
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
