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
package com.afollestad.nocknock.ui.main

import com.afollestad.nocknock.ALL_MOCK_MODELS
import com.afollestad.nocknock.MOCK_MODEL_1
import com.afollestad.nocknock.MOCK_MODEL_2
import com.afollestad.nocknock.MOCK_MODEL_3
import com.afollestad.nocknock.engine.validation.ValidationManager
import com.afollestad.nocknock.mockDatabase
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

class MainViewModelTest {

  private val database = mockDatabase()
  private val notificationManager = mock<NockNotificationManager>()
  private val validationManager = mock<ValidationManager>()

  private val viewModel = MainViewModel(
      database,
      notificationManager,
      validationManager,
      Dispatchers.Default
  ).apply {
    this.mainDispatcher = Dispatchers.Default
  }

  @After fun tearDown() = viewModel.destroy()

  @Test fun onResume() = runBlocking {
    val isLoading = viewModel.onIsLoading()
        .test()
    val sites = viewModel.onSites()
        .test()

    viewModel.onResume()

    verify(notificationManager).cancelStatusNotifications()
    verify(validationManager).ensureScheduledChecks()

    sites.assertValues(
        listOf(),
        ALL_MOCK_MODELS
    )
    isLoading.assertValues(true, false)
  }

  @Test fun postSiteUpdate_notFound() {
    val sites = viewModel.onSites()
        .test()
    viewModel.postSiteUpdate(MOCK_MODEL_1)
    sites.assertNoValues()
  }

  @Test fun postSiteUpdate() {
    val sites = viewModel.onSites()
        .test()

    viewModel.onResume()
    sites.assertValues(
        listOf(),
        ALL_MOCK_MODELS
    )

    val updatedModel2 = MOCK_MODEL_2.copy(
        name = "Wakanda Forever!!!"
    )
    val updatedSites = ALL_MOCK_MODELS.toMutableList()
        .apply {
          this[1] = updatedModel2
        }
    viewModel.postSiteUpdate(updatedModel2)

    sites.assertValues(updatedSites)

  }

  @Test fun refreshSite() {
    viewModel.refreshSite(MOCK_MODEL_3)

    verify(validationManager).scheduleCheck(
        site = MOCK_MODEL_3,
        rightNow = true,
        cancelPrevious = true
    )
  }

  @Test fun removeSite_notFound() {
    val sites = viewModel.onSites()
        .test()
    val isLoading = viewModel.onIsLoading()
        .test()

    viewModel.onResume()
    sites.assertValues(
        listOf(),
        ALL_MOCK_MODELS
    )

    val modifiedModel = MOCK_MODEL_1.copy(id = 11111)
    viewModel.removeSite(modifiedModel)

    sites.assertNoValues()
    isLoading.assertValues(true, false)

    verify(validationManager).cancelCheck(modifiedModel)
    verify(notificationManager).cancelStatusNotification(modifiedModel)
    verify(database.siteDao()).delete(modifiedModel)
    verify(database.siteSettingsDao()).delete(modifiedModel.settings!!)
  }

  @Test fun removeSite() {
    val sites = viewModel.onSites()
        .test()
    val isLoading = viewModel.onIsLoading()
        .test()

    viewModel.onResume()
    sites.assertValues(
        listOf(),
        ALL_MOCK_MODELS
    )

    val modelsWithout1 = ALL_MOCK_MODELS.toMutableList()
        .apply {
          removeAt(0)
        }
    viewModel.removeSite(MOCK_MODEL_1)

    sites.assertValues(modelsWithout1)
    isLoading.assertValues(true, false)

    verify(validationManager).cancelCheck(MOCK_MODEL_1)
    verify(notificationManager).cancelStatusNotification(MOCK_MODEL_1)
    verify(database.siteDao()).delete(MOCK_MODEL_1)
    verify(database.siteSettingsDao()).delete(MOCK_MODEL_1.settings!!)
  }
}
