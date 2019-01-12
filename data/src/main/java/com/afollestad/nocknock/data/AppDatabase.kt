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
import com.afollestad.nocknock.data.model.Header
import com.afollestad.nocknock.data.model.RetryPolicy
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.ValidationResult

/** @author Aidan Follestad (@afollestad) */
@Database(
    entities = [
      Header::class,
      RetryPolicy::class,
      ValidationResult::class,
      SiteSettings::class,
      Site::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun siteDao(): SiteDao

  abstract fun siteSettingsDao(): SiteSettingsDao

  abstract fun validationResultsDao(): ValidationResultsDao

  abstract fun retryPolicyDao(): RetryPolicyDao

  abstract fun headerDao(): HeaderDao
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
        val retryPolicy = retryPolicyDao().forSite(it.id)
            .singleOrNull()
        val headers = headerDao().forSite(it.id)
        return@map it.copy(
            settings = settings,
            lastResult = lastResult,
            retryPolicy = retryPolicy,
            headers = headers
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
  val retryPolicy = retryPolicyDao().forSite(id)
      .singleOrNull()
  val headers = headerDao().forSite(id)
  return result.copy(
      settings = settings,
      lastResult = lastResult,
      retryPolicy = retryPolicy,
      headers = headers
  )
}

/**
 * Inserts a site along with its settings and last result into the database.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.putSite(site: Site): Site {
  val settings = site.settings ?: throw IllegalArgumentException("Settings cannot be null.")
  val newId = siteDao().insert(site)
  val settingsWithSiteId = settings.copy(siteId = newId)
  val lastResultWithSiteId = site.lastResult?.copy(siteId = newId)
  val retryPolicyWithSiteId = site.retryPolicy?.copy(siteId = newId)
  val headersWithSiteId = site.headers.map { it.copy(siteId = newId) }

  siteSettingsDao().insert(settingsWithSiteId)
  lastResultWithSiteId?.let { validationResultsDao().insert(it) }
  retryPolicyWithSiteId?.let { retryPolicyDao().insert(it) }
  headerDao().insert(headersWithSiteId)

  return site.copy(
      id = newId,
      settings = settingsWithSiteId,
      lastResult = lastResultWithSiteId,
      retryPolicy = retryPolicyWithSiteId,
      headers = headersWithSiteId
  )
}

/**
 * Updates a site, along with its settings and last result, in the database.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.updateSite(site: Site) {
  siteDao().update(site)

  val settings = site.settings?.copy(siteId = site.id)
  if (settings != null) {
    val existing = siteSettingsDao().forSite(site.id)
        .singleOrNull()
    if (existing != null) {
      siteSettingsDao().update(settings)
    } else {
      siteSettingsDao().insert(settings)
    }
  }

  val lastResult = site.lastResult?.copy(siteId = site.id)
  if (lastResult != null) {
    val existing = validationResultsDao().forSite(site.id)
        .singleOrNull()
    if (existing != null) {
      validationResultsDao().update(lastResult)
    } else {
      validationResultsDao().insert(lastResult)
    }
  }

  val retryPolicy = site.retryPolicy?.copy(siteId = site.id)
  if (retryPolicy != null) {
    val existing = retryPolicyDao().forSite(site.id)
        .singleOrNull()
    if (existing != null) {
      retryPolicyDao().update(retryPolicy)
    } else {
      retryPolicyDao().insert(retryPolicy)
    }
  }

  // Wipe existing headers
  headerDao().delete(headerDao().forSite(site.id))
  // Then add ones that still exist
  site.headers.forEach { header ->
    headerDao().insert(header.copy(id = 0, siteId = site.id))
  }
}

/**
 * Deletes a site along with its settings and last result from the database.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun AppDatabase.deleteSite(site: Site) {
  site.settings?.let { siteSettingsDao().delete(it) }
  site.lastResult?.let { validationResultsDao().delete(it) }
  site.retryPolicy?.let { retryPolicyDao().delete(it) }
  if (site.headers.any { it.id == 0L }) {
    throw IllegalStateException("Cannot delete header with ID = 0.")
  }
  headerDao().delete(site.headers)
  siteDao().delete(site)
}
