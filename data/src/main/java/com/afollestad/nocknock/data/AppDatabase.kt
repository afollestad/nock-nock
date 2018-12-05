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
package com.afollestad.nocknock.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.afollestad.nocknock.data.model.Converters
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.ValidationResult

/** @author Aidan Follestad (@afollestad) */
@Database(
    entities = [
      ValidationResult::class,
      SiteSettings::class,
      Site::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun siteDao(): SiteDao

  abstract fun siteSettingsDao(): SiteSettingsDao

  abstract fun validationResultsDao(): ValidationResultsDao
}

/**
 * Gets all sites and maps their settings and last validation results.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.allSites(): List<Site> {
  return siteDao().all()
      .map {
        val settings = siteSettingsDao().forSite(it.id)
            .single()
        val lastResult = validationResultsDao().forSite(it.id)
            .singleOrNull()
        return@map it.copy(
            settings = settings,
            lastResult = lastResult
        )
      }
}

/**
 * Gets a single site and maps its settings and last validation result.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.getSite(id: Long): Site? {
  val result = siteDao().one(id)
      .singleOrNull() ?: return null
  val settings = siteSettingsDao().forSite(id)
      .single()
  val lastResult = validationResultsDao().forSite(id)
      .singleOrNull()
  return result.copy(
      settings = settings,
      lastResult = lastResult
  )
}

/**
 * Inserts a site along with its settings and last result into the database.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.putSite(site: Site): Site {
  requireNotNull(site.settings) { "Settings must be populated." }
  val newId = siteDao().insert(site)
  val settingsWithSiteId =
    site.settings!!.copy(
        siteId = newId
    )
  siteSettingsDao().insert(settingsWithSiteId)
  site.lastResult?.let { validationResultsDao().insert(it) }
  return site.copy(
      id = newId,
      settings = settingsWithSiteId
  )
}

/**
 * Updates a site, along with its settings and last result, in the database.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.updateSite(site: Site) {
  siteDao().update(site)
  if (site.settings != null) {
    val existing = siteSettingsDao().forSite(site.id)
        .singleOrNull()
    if (existing != null) {
      siteSettingsDao().update(site.settings!!)
    } else {
      siteSettingsDao().insert(
          site.settings!!.copy(
              siteId = site.id
          )
      )
    }
  }
  if (site.lastResult != null) {
    val existing = validationResultsDao().forSite(site.id)
        .singleOrNull()
    if (existing != null) {
      validationResultsDao().update(site.lastResult!!)
    } else {
      validationResultsDao().insert(site.lastResult!!)
    }
  }
}

/**
 * Deletes a site along with its settings and last result from the database.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.deleteSite(site: Site) {
  if (site.settings != null) {
    siteSettingsDao().delete(site.settings!!)
  }
  if (site.lastResult != null) {
    validationResultsDao().delete(site.lastResult!!)
  }
  siteDao().delete(site)
}
