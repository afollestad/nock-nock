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
@file:Suppress("DEPRECATION")

package com.afollestad.nocknock.data.legacy

import android.app.Application
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.Status.CHECKING
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.ValidationResult
import javax.inject.Inject

/**
 * Migrates from manual SQLite management to Room.
 *
 * @author Aidan Follestad (@afollestad)
 */
class DbMigrator @Inject constructor(
  app: Application,
  private val appDb: AppDatabase
) {
  private val legacyStore = ServerModelStore(app)

  fun migrateAll(): Int {
    val legacyModels = legacyStore.get()
    var count = 0

    for (oldModel in legacyModels) {
      // Insert site
      val site = oldModel.toNewModel()
      val siteId = appDb.siteDao()
          .insert(site)

      // Insert site settings
      val settingsWithId = site.settings!!.copy(
          siteId = siteId
      )
      appDb.siteSettingsDao()
          .insert(settingsWithId)

      // Insert validation result
      site.lastResult?.let {
        val resultWithId = it.copy(
            siteId = siteId
        )
        appDb.validationResultsDao()
            .insert(resultWithId)
      }

      count++
    }

    legacyStore.wipe()
    return count
  }

  private fun ServerModel.toNewModel(): Site {
    return Site(
        id = 0,
        name = this.name,
        url = this.url,
        settings = this.toSettingsModel(),
        lastResult = this.toValidationModel()
    )
  }

  private fun ServerModel.toSettingsModel(): SiteSettings {
    return SiteSettings(
        siteId = 0,
        validationIntervalMs = this.checkInterval,
        validationMode = this.validationMode,
        validationArgs = this.validationContent,
        disabled = this.disabled,
        networkTimeout = this.networkTimeout
    )
  }

  private fun ServerModel.toValidationModel(): ValidationResult? {
    if (this.lastCheck == LAST_CHECK_NONE) {
      return null
    }
    return ValidationResult(
        siteId = 0,
        timestampMs = this.lastCheck,
        status = if (this.status == CHECKING) WAITING else this.status,
        reason = this.reason
    )
  }
}
