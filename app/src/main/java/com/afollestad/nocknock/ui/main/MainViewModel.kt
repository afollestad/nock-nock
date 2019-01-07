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

import androidx.annotation.CheckResult
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.allSites
import com.afollestad.nocknock.data.deleteSite
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.engine.validation.ValidationManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.ui.ScopedViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** @author Aidan Follestad (@afollestad) */
class MainViewModel(
  private val database: AppDatabase,
  private val notificationManager: NockNotificationManager,
  private val validationManager: ValidationManager,
  mainDispatcher: CoroutineDispatcher,
  private val ioDispatcher: CoroutineDispatcher
) : ScopedViewModel(mainDispatcher), LifecycleObserver {

  private val sites = MutableLiveData<List<Site>>()
  private val isLoading = MutableLiveData<Boolean>()
  private val emptyTextVisibility = MutableLiveData<Boolean>()

  @CheckResult fun onSites(): LiveData<List<Site>> = sites

  @CheckResult fun onIsLoading(): LiveData<Boolean> = isLoading

  @CheckResult fun onEmptyTextVisibility(): LiveData<Boolean> = emptyTextVisibility

  @OnLifecycleEvent(ON_RESUME)
  fun onResume() = loadSites()

  fun postSiteUpdate(model: Site) {
    val currentSites = sites.value ?: return
    val index = currentSites.indexOfFirst { it.id == model.id }
    if (index == -1) return
    sites.value = currentSites.toMutableList()
        .apply {
          this[index] = model
        }
  }

  fun refreshSite(model: Site) {
    validationManager.scheduleCheck(
        site = model,
        rightNow = true,
        cancelPrevious = true
    )
  }

  fun removeSite(model: Site) {
    validationManager.cancelCheck(model)
    notificationManager.cancelStatusNotification(model)

    scope.launch {
      isLoading.value = true
      withContext(ioDispatcher) { database.deleteSite(model) }

      val currentSites = sites.value ?: return@launch
      val index = currentSites.indexOfFirst { it.id == model.id }
      isLoading.value = false
      if (index == -1) return@launch

      val newSitesList = currentSites.toMutableList()
          .apply {
            removeAt(index)
          }
      sites.value = newSitesList
      emptyTextVisibility.value = newSitesList.isEmpty()
    }
  }

  private fun loadSites() {
    scope.launch {
      notificationManager.cancelStatusNotifications()
      sites.value = listOf()
      emptyTextVisibility.value = false
      isLoading.value = true

      val result = withContext(ioDispatcher) {
        database.allSites()
      }

      sites.value = result
      ensureCheckJobs()
      isLoading.value = false
      emptyTextVisibility.value = result.isEmpty()
    }
  }

  private suspend fun ensureCheckJobs() {
    withContext(ioDispatcher) {
      validationManager.ensureScheduledChecks()
    }
  }
}
