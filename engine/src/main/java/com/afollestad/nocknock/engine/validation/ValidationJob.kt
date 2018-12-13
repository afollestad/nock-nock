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
package com.afollestad.nocknock.engine.validation

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.getSite
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.Status
import com.afollestad.nocknock.data.model.Status.CHECKING
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.model.isPending
import com.afollestad.nocknock.data.updateSite
import com.afollestad.nocknock.engine.BuildConfig.APPLICATION_ID
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.utilities.js.JavaScript
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.lang.System.currentTimeMillis
import timber.log.Timber.d as log

/**
 * The job which is sent to the system JobScheduler to perform site validation in the background.
 *
 * @author Aidan Follestad (@afollestad)
 */
class ValidationJob : JobService() {

  companion object {
    const val ACTION_STATUS_UPDATE = "$APPLICATION_ID.STATUS_UPDATE"
    const val ACTION_JOB_RUNNING = "$APPLICATION_ID.STATUS_JOB_RUNNING"
    const val KEY_UPDATE_MODEL = "site_model"
    const val KEY_SITE_ID = "site.id"
  }

  private val database by inject<AppDatabase>()
  private val validaitonManager by inject<ValidationManager>()
  private val notificationManager by inject<NockNotificationManager>()

  override fun onStartJob(params: JobParameters): Boolean {
    val siteId = params.extras.getLong(KEY_SITE_ID)

    GlobalScope.launch(Main) {
      val site = async(IO) { database.getSite(siteId) }.await()
      if (site == null) {
        log("Unable to find a site for ID $siteId, this job will not be rescheduled.")
        return@launch jobFinished(params, false)
      }

      val siteSettings = site.settings
      requireNotNull(siteSettings) { "Site settings must be populated." }

      log("Performing status checks on site ${site.id}...")
      sendBroadcast(Intent(ACTION_JOB_RUNNING).apply { putExtra(KEY_SITE_ID, site.id) })

      log("Checking ${site.name} (${site.url})...")

      val jobResult = async(IO) {
        updateStatus(site, CHECKING)
        val checkResult = validaitonManager.performCheck(site)
        val resultModel = checkResult.model
        val resultResponse = checkResult.response
        val result = resultModel.lastResult!!

        if (result.status != OK) {
          log("Got unsuccessful check status back: ${result.reason}")
          return@async updateStatus(site = resultModel)
        } else {
          when (siteSettings.validationMode) {
            TERM_SEARCH -> {
              val body = resultResponse?.body()?.string() ?: ""
              log("Using TERM_SEARCH validation mode on body of length: ${body.length}")

              return@async if (!body.contains(siteSettings.validationArgs ?: "")) {
                updateStatus(
                    resultModel.withStatus(
                        status = ERROR,
                        reason = "Term \"${siteSettings.validationArgs}\" not found in response body."
                    )
                )
              } else {
                updateStatus(site = resultModel)
              }
            }
            JAVASCRIPT -> {
              val body = resultResponse?.body()?.string() ?: ""
              log("Using JAVASCRIPT validation mode on body of length: ${body.length}")
              val reason = JavaScript.eval(siteSettings.validationArgs ?: "", body)
              return@async if (reason != null) {
                updateStatus(resultModel.withStatus(reason = reason), status = ERROR)
              } else {
                resultModel
              }
            }
            STATUS_CODE -> {
              // We already know the status code is successful because we are in this else branch
              log("Using STATUS_CODE validation, which has passed!")
              updateStatus(
                  resultModel.withStatus(
                      status = OK,
                      reason = null
                  )
              )
            }
            else -> {
              throw IllegalArgumentException(
                  "Unknown validation mode: ${siteSettings.validationArgs}"
              )
            }
          }
        }
      }.await()

      if (jobResult.lastResult!!.status == OK) {
        notificationManager.cancelStatusNotification(jobResult)
      } else {
        notificationManager.postStatusNotification(jobResult)
      }

      validaitonManager.scheduleCheck(
          site = jobResult,
          fromFinishingJob = true
      )
    }

    return true
  }

  override fun onStopJob(params: JobParameters): Boolean {
    val siteId = params.extras.getLong(KEY_SITE_ID)
    log("Check job for site $siteId is done")
    return true
  }

  private suspend fun updateStatus(
    site: Site,
    status: Status = site.lastResult?.status ?: WAITING
  ): Site {
    log("Updating ${site.name} (${site.url}) status to $status...")

    val lastCheckTime =
      if (status.isPending()) site.lastResult?.timestampMs ?: -1
      else currentTimeMillis()
    val reason =
      if (status == OK) null
      else site.lastResult?.reason ?: "Unknown"

    val updatedModel = site.withStatus(
        status = status,
        timestamp = lastCheckTime,
        reason = reason
    )
    database.updateSite(updatedModel)

    withContext(Main) {
      sendBroadcast(Intent(ACTION_STATUS_UPDATE).apply {
        putExtra(KEY_UPDATE_MODEL, updatedModel)
      })
    }
    return updatedModel
  }
}
