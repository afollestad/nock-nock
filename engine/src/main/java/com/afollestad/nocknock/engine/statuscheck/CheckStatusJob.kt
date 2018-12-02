/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine.statuscheck

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus
import com.afollestad.nocknock.data.ServerStatus.CHECKING
import com.afollestad.nocknock.data.ServerStatus.ERROR
import com.afollestad.nocknock.data.ServerStatus.OK
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.isPending
import com.afollestad.nocknock.engine.BuildConfig.APPLICATION_ID
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.utilities.ext.injector
import com.afollestad.nocknock.utilities.js.JavaScript
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad)*/
class CheckStatusJob : JobService() {

  companion object {
    const val ACTION_STATUS_UPDATE = "$APPLICATION_ID.STATUS_UPDATE"
    const val ACTION_JOB_RUNNING = "$APPLICATION_ID.STATUS_JOB_RUNNING"
    const val KEY_UPDATE_MODEL = "site_model"
    const val KEY_SITE_ID = "site.id"
  }

  @Inject lateinit var modelStore: ServerModelStore
  @Inject lateinit var checkStatusManager: CheckStatusManager
  @Inject lateinit var notificationManager: NockNotificationManager

  override fun onStartJob(params: JobParameters): Boolean {
    injector().injectInto(this)
    val siteId = params.extras.getInt(KEY_SITE_ID)

    GlobalScope.launch(Main) {
      val sites = async(IO) { modelStore.get(id = siteId) }.await()
      if (sites.isEmpty()) {
        log("Unable to find any sites for ID $siteId, this job will not be rescheduled.")
        return@launch jobFinished(params, false)
      }

      val site = sites.single()
      log("Performing status checks on site ${site.id}...")
      sendBroadcast(Intent(ACTION_JOB_RUNNING).apply { putExtra(KEY_SITE_ID, site.id) })

      log("Checking ${site.name} (${site.url})...")

      val result = async(IO) {
        updateStatus(site, CHECKING)
        val checkResult = checkStatusManager.performCheck(site)
        val resultModel = checkResult.model
        val resultResponse = checkResult.response

        if (resultModel.status != OK) {
          log("Got unsuccessful check status back: ${resultModel.reason}")
          return@async updateStatus(site = resultModel)
        } else {
          when (site.validationMode) {
            TERM_SEARCH -> {
              val body = resultResponse?.body()?.string() ?: ""
              log("Using TERM_SEARCH validation mode on body of length: ${body.length}")

              return@async if (!body.contains(site.validationContent ?: "")) {
                updateStatus(
                    resultModel.copy(
                        status = ERROR,
                        reason = "Term \"${site.validationContent}\" not found in response body."
                    )
                )
              } else {
                updateStatus(site = resultModel)
              }
            }
            JAVASCRIPT -> {
              val body = resultResponse?.body()?.string() ?: ""
              log("Using JAVASCRIPT validation mode on body of length: ${body.length}")
              val reason = JavaScript.eval(resultModel.validationContent ?: "", body)
              return@async if (reason != null) {
                updateStatus(resultModel.copy(reason = reason), status = ERROR)
              } else {
                resultModel
              }
            }
            STATUS_CODE -> {
              // We already know the status code is successful because we are in this else branch
              log("Using STATUS_CODE validation, which has passed!")
              updateStatus(
                  resultModel.copy(
                      status = OK,
                      reason = null
                  )
              )
            }
            else -> {
              throw IllegalArgumentException("Unknown validation mode: ${site.validationMode}")
            }
          }
        }
      }.await()

      if (result.status == OK) {
        notificationManager.cancelStatusNotification(result)
      } else {
        notificationManager.postStatusNotification(result)
      }

      checkStatusManager.scheduleCheck(
          site = result,
          fromFinishingJob = true
      )
    }

    return true
  }

  override fun onStopJob(params: JobParameters): Boolean {
    val siteId = params.extras.getInt(KEY_SITE_ID)
    log("Check job for site $siteId is done")
    return true
  }

  private suspend fun updateStatus(
    site: ServerModel,
    status: ServerStatus = site.status
  ): ServerModel {
    log("Updating ${site.name} (${site.url}) status to $status...")

    val lastCheckTime =
      if (status.isPending()) site.lastCheck
      else currentTimeMillis()
    val reason =
      if (status == OK) null
      else site.reason

    val newSiteModel = site.copy(
        status = status,
        lastCheck = lastCheckTime,
        reason = reason
    )
    modelStore.update(newSiteModel)

    withContext(Main) {
      sendBroadcast(Intent(ACTION_STATUS_UPDATE).apply { putExtra(KEY_UPDATE_MODEL, newSiteModel) })
    }
    return newSiteModel
  }
}
