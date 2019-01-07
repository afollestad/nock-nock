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

import android.app.job.JobScheduler
import android.app.job.JobScheduler.RESULT_SUCCESS
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.allSites
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.engine.R
import com.afollestad.nocknock.engine.validation.ValidationJob.Companion.KEY_SITE_ID
import com.afollestad.nocknock.utilities.providers.BundleProvider
import com.afollestad.nocknock.utilities.providers.JobInfoProvider
import com.afollestad.nocknock.utilities.providers.StringProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit.MILLISECONDS
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
data class CheckResult(
  val model: Site,
  val response: Response? = null
)

typealias ClientTimeoutChanger = (client: OkHttpClient, timeout: Int) -> OkHttpClient

/** @author Aidan Follestad (@afollestad) */
interface ValidationManager {

  suspend fun ensureScheduledChecks()

  fun scheduleCheck(
    site: Site,
    rightNow: Boolean = false,
    cancelPrevious: Boolean = rightNow,
    fromFinishingJob: Boolean = false,
    overrideDelay: Long = -1
  )

  fun cancelCheck(site: Site)

  suspend fun performCheck(site: Site): CheckResult
}

class RealValidationManager(
  private val jobScheduler: JobScheduler,
  private val okHttpClient: OkHttpClient,
  private val stringProvider: StringProvider,
  private val bundleProvider: BundleProvider,
  private val jobInfoProvider: JobInfoProvider,
  private val database: AppDatabase
) : ValidationManager {

  private var clientTimeoutChanger: ClientTimeoutChanger = { client, timeout ->
    client.newBuilder()
        .callTimeout(timeout.toLong(), MILLISECONDS)
        .build()
  }

  override suspend fun ensureScheduledChecks() {
    val sites = database.allSites()
    if (sites.isEmpty()) {
      return
    }
    log("Ensuring enabled sites have scheduled checks.")
    sites.filter { it.settings?.disabled != true }
        .forEach { site ->
          val existingJob = jobForSite(site)
          if (existingJob == null) {
            log("Site ${site.id} does NOT have a scheduled job, running one now.")
            scheduleCheck(site = site, rightNow = true)
          } else {
            log("Site ${site.id} already has a scheduled job. Nothing to do.")
          }
        }
  }

  override fun scheduleCheck(
    site: Site,
    rightNow: Boolean,
    cancelPrevious: Boolean,
    fromFinishingJob: Boolean,
    overrideDelay: Long
  ) {
    check(site.id != 0L) { "Cannot schedule checks for jobs with no ID." }
    val siteSettings = site.settings
    requireNotNull(siteSettings) { "Site settings must be populated." }

    if (cancelPrevious) {
      cancelCheck(site)
    } else if (!fromFinishingJob) {
      val existingJob = jobForSite(site)
      check(existingJob == null) {
        "Site ${site.id} already has a scheduled job, and cancelPrevious = false."
      }
    }

    log("Requesting a check job for site to be scheduled: $site")
    val extras = bundleProvider.createPersistable {
      putLong(KEY_SITE_ID, site.id)
    }
    val jobInfo = jobInfoProvider.createCheckJob(
        id = site.id.toInt(),
        onlyUnmeteredNetwork = false,
        delayMs = when {
          rightNow -> 1
          overrideDelay > -1 -> overrideDelay
          else -> siteSettings.validationIntervalMs
        },
        extras = extras,
        target = ValidationJob::class.java
    )

    val dispatchResult = jobScheduler.schedule(jobInfo)
    if (dispatchResult != RESULT_SUCCESS) {
      log("Failed to schedule a check job for site: ${site.id}")
    } else {
      log("Check job successfully scheduled for site: ${site.id}")
    }
  }

  override fun cancelCheck(site: Site) {
    check(site.id != 0L) { "Cannot cancel scheduled checks for jobs with no ID." }
    log("Cancelling scheduled checks for site: ${site.id}")
    jobScheduler.cancel(site.id.toInt())
  }

  override suspend fun performCheck(site: Site): CheckResult {
    check(site.id != 0L) { "Cannot schedule checks for jobs with no ID." }
    val siteSettings = site.settings
    requireNotNull(siteSettings) { "Site settings must be populated." }
    check(siteSettings.networkTimeout > 0) { "Network timeout not set for site ${site.id}" }
    log("performCheck(${site.id}) - GET ${site.url}")

    val request = Request.Builder()
        .url(site.url)
        .get()
        .build()

    return try {
      val client = clientTimeoutChanger(okHttpClient, siteSettings.networkTimeout)
      val response = client.newCall(request)
          .execute()

      if (response.isSuccessful || response.code() == 401) {
        log("performCheck(${site.id}) = Successful")
        CheckResult(
            model = site.withStatus(status = OK, reason = null),
            response = response
        )
      } else {
        log("performCheck(${site.id}) = Failure, HTTP code ${response.code()}")
        CheckResult(
            model = site.withStatus(
                status = ERROR,
                reason = "Response ${response.code()} - ${response.body()?.string() ?: "Unknown"}"
            ),
            response = response
        )
      }
    } catch (timeoutEx: SocketTimeoutException) {
      log("performCheck(${site.id}) = Socket Timeout")
      CheckResult(
          model = site.withStatus(
              status = ERROR,
              reason = stringProvider.get(R.string.timeout)
          )
      )
    } catch (ex: Exception) {
      log("performCheck(${site.id}) = Error: ${ex.message}")
      CheckResult(model = site.withStatus(status = ERROR, reason = ex.message))
    }
  }

  private fun jobForSite(site: Site) =
    jobScheduler.allPendingJobs
        .firstOrNull { job -> job.id == site.id.toInt() }

//  @TestOnly fun setClientTimeoutChanger(changer: ClientTimeoutChanger) {
//    this.clientTimeoutChanger = changer
//  }
}
