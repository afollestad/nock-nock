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

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.allSites
import com.afollestad.nocknock.data.deleteSite
import com.afollestad.nocknock.data.legacy.DbMigrator
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.engine.statuscheck.ValidationJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.ValidationJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.engine.statuscheck.ValidationManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
interface MainPresenter {

  fun takeView(view: MainView)

  fun onBroadcast(intent: Intent)

  fun resume()

  fun refreshSite(site: Site)

  fun removeSite(site: Site)

  fun dropView()
}

/** @author Aidan Follestad (@afollestad) */
class RealMainPresenter @Inject constructor(
  private val app: Application,
  private val database: AppDatabase,
  private val notificationManager: NockNotificationManager,
  private val checkStatusManager: ValidationManager
) : MainPresenter {

  private var view: MainView? = null

  override fun takeView(view: MainView) {
    this.view = view
    notificationManager.createChannels()
    ensureCheckJobs()
  }

  override fun onBroadcast(intent: Intent) {
    if (intent.action == ACTION_STATUS_UPDATE) {
      val model = intent.getSerializableExtra(KEY_UPDATE_MODEL) as? Site
          ?: return
      view?.updateModel(model)
    }
  }

  override fun resume() {
    notificationManager.cancelStatusNotifications()

    view!!.run {
      setModels(listOf())
      setLoading()

      scopeWhileAttached(Main) {
        launch(coroutineContext) {
          doMigrationIfNeeded()

          val models = async(IO) { database.allSites() }.await()

          setModels(models)
          setDoneLoading()
        }
      }
    }
  }

  override fun refreshSite(site: Site) =
    checkStatusManager.scheduleCheck(
        site = site,
        rightNow = true,
        cancelPrevious = true
    )

  override fun removeSite(site: Site) {
    checkStatusManager.cancelCheck(site)
    notificationManager.cancelStatusNotification(site)

    view!!.scopeWhileAttached(Main) {
      launch(coroutineContext) {
        async(IO) { database.deleteSite(site) }.await()
        view?.onSiteDeleted(site)
      }
    }
  }

  override fun dropView() {
    view = null
  }

  private suspend fun CoroutineScope.doMigrationIfNeeded() {
    if (needDbMigration()) {
      log("Doing database migration...")
      val migratedCount = async(IO) {
        DbMigrator(app, database).migrateAll()
      }.await()
      didDbMigration()
      log("Database migration done! Migrated $migratedCount models.")
      ensureCheckJobs()
    }
  }

  private fun needDbMigration(): Boolean =
    !app.getSharedPreferences("settings", MODE_PRIVATE)
        .getBoolean("did_db_migration", false)

  private fun didDbMigration() =
    app.getSharedPreferences("settings", MODE_PRIVATE)
        .edit()
        .putBoolean("did_db_migration", true)
        .apply()

  private fun ensureCheckJobs() {
    view!!.scopeWhileAttached(IO) {
      launch(coroutineContext) {
        checkStatusManager.ensureScheduledChecks()
      }
    }
  }
}
