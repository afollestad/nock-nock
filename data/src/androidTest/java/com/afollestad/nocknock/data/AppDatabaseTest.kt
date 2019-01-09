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
@file:Suppress("RemoveEmptyPrimaryConstructor")

package com.afollestad.nocknock.data

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.runner.AndroidJUnit4
import com.afollestad.nocknock.data.model.Header
import com.afollestad.nocknock.data.model.RetryPolicy
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.model.ValidationResult
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.System.currentTimeMillis

/** @author Aidan Follestad (@afollestad) */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest() {

  private lateinit var db: AppDatabase
  private lateinit var sitesDao: SiteDao
  private lateinit var settingsDao: SiteSettingsDao
  private lateinit var resultsDao: ValidationResultsDao
  private lateinit var retryDao: RetryPolicyDao
  private lateinit var headerDao: HeaderDao

  @Before fun setup() {
    val context = getApplicationContext<Context>()
    db = inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    sitesDao = db.siteDao()
    settingsDao = db.siteSettingsDao()
    resultsDao = db.validationResultsDao()
    retryDao = db.retryPolicyDao()
    headerDao = db.headerDao()
  }

  @After
  @Throws(IOException::class)
  fun destroy() = db.close()

  // SiteDao

  @Test fun site_insert_and_get_all() {
    val model1 = Site(
        name = "Test 1",
        url = "https://test1.com",
        tags = "",
        settings = null,
        lastResult = null,
        retryPolicy = null,
        headers = emptyList()
    )
    val newId1 = sitesDao.insert(model1)
    assertThat(newId1).isGreaterThan(0)

    val model2 = Site(
        name = "Test 2",
        url = "https://test2.com",
        tags = "",
        settings = null,
        lastResult = null,
        retryPolicy = null,
        headers = emptyList()
    )
    val newId2 = sitesDao.insert(model2)
    assertThat(newId2).isGreaterThan(newId1)

    val models = sitesDao.all()
    assertThat(models.size).isEqualTo(2)
    assertThat(models[0]).isEqualTo(model1.copy(id = newId1))
    assertThat(models[1]).isEqualTo(model2.copy(id = newId2))
  }

  @Test fun site_insert_and_get_one() {
    val model = Site(
        name = "Test",
        url = "https://test.com",
        tags = "",
        settings = null,
        lastResult = null,
        retryPolicy = null,
        headers = emptyList()
    )
    val newId = sitesDao.insert(model)
    assertThat(newId).isGreaterThan(0)

    val models = sitesDao.all()
    assertThat(models.single()).isEqualTo(model.copy(id = newId))
  }

  @Test fun site_insert_and_update() {
    val initialModel = Site(
        name = "Test 1",
        url = "https://test1.com",
        tags = "",
        settings = null,
        lastResult = null,
        retryPolicy = null,
        headers = emptyList()
    )
    val newId = sitesDao.insert(initialModel)
    assertThat(newId).isGreaterThan(0)

    val insertedModel = sitesDao.all()
        .single()
    val updatedModel = insertedModel.copy(
        name = "Test 2",
        url = "https://hi.com"
    )
    assertThat(sitesDao.update(updatedModel)).isEqualTo(1)

    val finalModel = sitesDao.all()
        .single()
    assertThat(finalModel).isNotEqualTo(initialModel.copy(id = newId))
  }

  @Test fun site_insert_and_delete() {
    val model1 = Site(
        name = "Test 1",
        url = "https://test1.com",
        tags = "",
        settings = null,
        lastResult = null,
        retryPolicy = null,
        headers = emptyList()
    )
    val newId1 = sitesDao.insert(model1)
    assertThat(newId1).isGreaterThan(0)

    val model2 = Site(
        name = "Test 2",
        url = "https://test2.com",
        tags = "",
        settings = null,
        lastResult = null,
        retryPolicy = null,
        headers = emptyList()
    )
    val newId2 = sitesDao.insert(model2)
    assertThat(newId2).isGreaterThan(newId1)

    val models1 = sitesDao.all()
    sitesDao.delete(models1[0])

    val models2 = sitesDao.all()
    assertThat(models2.single()).isEqualTo(models1[1])
  }

  // SiteSettingsDao

  @Test fun settings_insert_and_forSite() {
    val model = SiteSettings(
        siteId = 1,
        validationIntervalMs = 60000,
        validationMode = STATUS_CODE,
        validationArgs = null,
        disabled = false,
        networkTimeout = 10000
    )
    val newId = settingsDao.insert(model)
    assertThat(newId).isEqualTo(1)

    val finalModel = settingsDao.forSite(newId)
        .single()
    assertThat(finalModel).isEqualTo(model.copy(siteId = newId))
  }

  @Test fun settings_update() {
    settingsDao.insert(
        SiteSettings(
            siteId = 1,
            validationIntervalMs = 60000,
            validationMode = STATUS_CODE,
            validationArgs = null,
            disabled = false,
            networkTimeout = 10000
        )
    )

    val insertedModel = settingsDao.forSite(1)
        .single()
    val updatedModel = insertedModel.copy(
        validationIntervalMs = 10000,
        validationMode = TERM_SEARCH,
        validationArgs = "test",
        disabled = false,
        networkTimeout = 1000
    )
    assertThat(settingsDao.update(updatedModel)).isEqualTo(1)

    val finalModel = settingsDao.forSite(1)
        .single()
    assertThat(finalModel).isEqualTo(updatedModel)
  }

  @Test fun settings_delete() {
    settingsDao.insert(
        SiteSettings(
            siteId = 1,
            validationIntervalMs = 60000,
            validationMode = STATUS_CODE,
            validationArgs = null,
            disabled = false,
            networkTimeout = 10000
        )
    )

    val insertedModel = settingsDao.forSite(1)
        .single()
    settingsDao.delete(insertedModel)
    assertThat(settingsDao.forSite(1)).isEmpty()
  }

  // ValidationResultsDao

  @Test fun validation_insert_and_forSite() {
    val model = ValidationResult(
        siteId = 1,
        timestampMs = currentTimeMillis(),
        status = ERROR,
        reason = "Oh no"
    )
    val newId = resultsDao.insert(model)
    assertThat(newId).isEqualTo(1)

    val finalModel = resultsDao.forSite(newId)
        .single()
    assertThat(finalModel).isEqualTo(model.copy(siteId = newId))
  }

  @Test fun validation_update() {
    resultsDao.insert(
        ValidationResult(
            siteId = 1,
            timestampMs = currentTimeMillis(),
            status = ERROR,
            reason = "Oh no"
        )
    )

    val insertedModel = resultsDao.forSite(1)
        .single()
    val updatedModel = insertedModel.copy(
        timestampMs = currentTimeMillis() + 1000,
        status = OK,
        reason = null
    )
    assertThat(resultsDao.update(updatedModel)).isEqualTo(1)

    val finalModel = resultsDao.forSite(1)
        .single()
    assertThat(finalModel).isEqualTo(updatedModel)
  }

  @Test fun validation_delete() {
    resultsDao.insert(
        ValidationResult(
            siteId = 1,
            timestampMs = currentTimeMillis(),
            status = ERROR,
            reason = "Oh no"
        )
    )

    val insertedModel = resultsDao.forSite(1)
        .single()
    resultsDao.delete(insertedModel)
    assertThat(resultsDao.forSite(1)).isEmpty()
  }

  // RetryPolicyDao

  @Test fun retryPolicy_insert_and_forSite() {
    val model = RetryPolicy(
        siteId = 1,
        count = 3,
        minutes = 6
    )
    val newId = retryDao.insert(model)
    assertThat(newId).isEqualTo(1)

    val finalModel = retryDao.forSite(newId)
        .single()
    assertThat(finalModel).isEqualTo(model.copy(siteId = newId))
  }

  @Test fun retryPolicy_update() {
    retryDao.insert(
        RetryPolicy(
            siteId = 1,
            count = 3,
            minutes = 6
        )
    )

    val insertedModel = retryDao.forSite(1)
        .single()
    val updatedModel = insertedModel.copy(
        count = 4,
        minutes = 8
    )
    assertThat(retryDao.update(updatedModel)).isEqualTo(1)

    val finalModel = retryDao.forSite(1)
        .single()
    assertThat(finalModel).isEqualTo(updatedModel)
  }

  @Test fun retryPolicy_delete() {
    retryDao.insert(
        RetryPolicy(
            siteId = 1,
            count = 3,
            minutes = 6
        )
    )

    val insertedModel = retryDao.forSite(1)
        .single()
    retryDao.delete(insertedModel)
    assertThat(retryDao.forSite(1)).isEmpty()
  }

  // HeaderDao

  @Test fun headers_insert_and_forSite() {
    val models = listOf(
        Header(
            siteId = 1,
            key = "Name",
            value = "Aidan"
        ),
        Header(
            siteId = 1,
            key = "Born",
            value = "1995"
        )
    )
    val newIds = headerDao.insert(models)
    assertThat(newIds.first()).isEqualTo(1)
    assertThat(newIds.last()).isEqualTo(2)

    val finalModels = headerDao.forSite(1)
    assertThat(finalModels.first()).isEqualTo(models.first().copy(id = 1))
    assertThat(finalModels.last()).isEqualTo(models.last().copy(id = 2))
  }

  @Test fun headers_update() {
    val models = listOf(
        Header(
            siteId = 1,
            key = "Name",
            value = "Aidan"
        ),
        Header(
            siteId = 1,
            key = "Born",
            value = "1995"
        )
    )
    headerDao.insert(models)

    val insertedModel = headerDao.forSite(1)
        .last()
    val updatedModel = insertedModel.copy(
        key = "Test",
        value = "Hello"
    )
    assertThat(headerDao.update(updatedModel)).isEqualTo(1)

    val finalModels = headerDao.forSite(1)
    assertThat(finalModels.first()).isEqualTo(models.first().copy(id = 1))
    assertThat(finalModels.last()).isEqualTo(updatedModel)
  }

  @Test fun headers_delete() {
    val models = listOf(
        Header(
            siteId = 1,
            key = "Name",
            value = "Aidan"
        ),
        Header(
            siteId = 1,
            key = "Born",
            value = "1995"
        )
    )
    headerDao.insert(models)

    val insertedModels = headerDao.forSite(1)
    headerDao.delete(insertedModels)
    assertThat(headerDao.forSite(1)).isEmpty()
  }

  // Extension Methods

  @Test fun extension_put_and_allSites() {
    db.putSite(MOCK_MODEL_1)
    db.putSite(MOCK_MODEL_2)
    db.putSite(MOCK_MODEL_3)

    val allSites = db.allSites()
    assertThat(allSites.size).isEqualTo(3)
    assertThat(allSites[0]).isEqualTo(MOCK_MODEL_1)
    assertThat(allSites[1]).isEqualTo(MOCK_MODEL_2)
    assertThat(allSites[2]).isEqualTo(MOCK_MODEL_3)
  }

  @Test fun extension_put_getSite() {
    db.putSite(MOCK_MODEL_1)
    db.putSite(MOCK_MODEL_2)
    db.putSite(MOCK_MODEL_3)
    val allSites = db.allSites()

    val site = db.getSite(2)
    assertThat(site).isEqualTo(allSites[1])
  }

  @Test fun extension_put_updateSite() {
    db.putSite(MOCK_MODEL_1)
    db.putSite(MOCK_MODEL_2)
    db.putSite(MOCK_MODEL_3)
    val modelToUpdate = db.allSites()[1]

    val updatedSettings = modelToUpdate.settings!!.copy(
        validationIntervalMs = 1,
        validationMode = JAVASCRIPT,
        validationArgs = "throw 'Hello World'",
        disabled = false,
        networkTimeout = 50
    )
    val updatedValidationResult = modelToUpdate.lastResult!!.copy(
        timestampMs = currentTimeMillis() + 10,
        status = ERROR,
        reason = "Oh no"
    )
    val updatedRetryPolicy = modelToUpdate.retryPolicy!!.copy(
        count = 4,
        minutes = 8
    )
    val updatedHeaders = listOf(
        modelToUpdate.headers.first().copy(
            key = "One",
            value = "Hello"
        ),
        modelToUpdate.headers.last().copy(
            key = "Two",
            value = "Hey"
        )
    )
    val updatedModel = modelToUpdate.copy(
        name = "Oijrfouhef",
        url = "https://iojfdfsdk.io",
        settings = updatedSettings,
        lastResult = updatedValidationResult,
        retryPolicy = updatedRetryPolicy,
        headers = updatedHeaders
    )

    db.updateSite(updatedModel)

    val finalSite = db.getSite(modelToUpdate.id)!!
    assertThat(finalSite.settings).isEqualTo(updatedSettings)
    assertThat(finalSite.lastResult).isEqualTo(updatedValidationResult)
    assertThat(finalSite.retryPolicy).isEqualTo(updatedRetryPolicy)
    assertThat(finalSite.headers.first()).isEqualTo(updatedHeaders.first())
    assertThat(finalSite.headers.last()).isEqualTo(updatedHeaders.last())
    assertThat(finalSite).isEqualTo(updatedModel)
  }

  @Test fun extension_put_and_deleteSite() {
    db.putSite(MOCK_MODEL_1)
    db.putSite(MOCK_MODEL_2)
    db.putSite(MOCK_MODEL_3)
    val allSites = db.allSites()

    db.deleteSite(allSites[1])

    val remainingSettings = settingsDao.all()
    assertThat(remainingSettings.size).isEqualTo(2)
    assertThat(remainingSettings[0]).isEqualTo(allSites[0].settings!!)
    assertThat(remainingSettings[1]).isEqualTo(allSites[2].settings!!)

    val remainingResults = resultsDao.all()
    assertThat(remainingResults.size).isEqualTo(2)
    assertThat(remainingResults[0]).isEqualTo(allSites[0].lastResult!!)
    assertThat(remainingResults[1]).isEqualTo(allSites[2].lastResult!!)

    val remainingRetryPolicies = retryDao.all()
    assertThat(remainingRetryPolicies.size).isEqualTo(2)
    assertThat(remainingRetryPolicies[0]).isEqualTo(allSites[0].retryPolicy!!)
    assertThat(remainingRetryPolicies[1]).isEqualTo(allSites[2].retryPolicy!!)

    val remainingHeaders = headerDao.all()
    assertThat(remainingHeaders.size).isEqualTo(4)
    assertThat(remainingHeaders[0]).isEqualTo(allSites[0].headers.first())
    assertThat(remainingHeaders[1]).isEqualTo(allSites[0].headers.last())
    assertThat(remainingHeaders[2]).isEqualTo(allSites[2].headers.first())
    assertThat(remainingHeaders[3]).isEqualTo(allSites[2].headers.last())
  }
}
