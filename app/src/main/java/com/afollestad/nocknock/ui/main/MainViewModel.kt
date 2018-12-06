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
import com.afollestad.nocknock.di.viewmodels.ScopedViewModel
import com.afollestad.nocknock.engine.validation.ValidationManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.di.qualifiers.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
class MainViewModel @Inject constructor(
  private val database: AppDatabase,
  private val notificationManager: NockNotificationManager,
  private val validationManager: ValidationManager,
  @field:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ScopedViewModel(), LifecycleObserver {

  private val sites = MutableLiveData<List<Site>>()
  private val isLoading = MutableLiveData<Boolean>()

  @CheckResult fun onSites(): LiveData<List<Site>> = sites

  @CheckResult fun onIsLoading(): LiveData<Boolean> = isLoading

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

      sites.value = currentSites.toMutableList()
          .apply {
            removeAt(index)
          }
    }
  }

  private fun loadSites() {
    scope.launch {
      notificationManager.cancelStatusNotifications()
      sites.value = listOf()
      isLoading.value = true

      val result = withContext(ioDispatcher) {
        database.allSites()
      }

      sites.value = result
      ensureCheckJobs()
      isLoading.value = false
    }
  }

  private suspend fun ensureCheckJobs() {
    withContext(ioDispatcher) {
      validationManager.ensureScheduledChecks()
    }
  }
}
