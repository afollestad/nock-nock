/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine.statuscheck

import android.app.job.JobScheduler
import android.app.job.JobScheduler.RESULT_SUCCESS
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.ERROR
import com.afollestad.nocknock.data.ServerStatus.OK
import com.afollestad.nocknock.engine.R
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.KEY_SITE_ID
import com.afollestad.nocknock.utilities.providers.BundleProvider
import com.afollestad.nocknock.utilities.providers.JobInfoProvider
import com.afollestad.nocknock.utilities.providers.StringProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.SocketTimeoutException
import javax.inject.Inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
data class CheckResult(
  val model: ServerModel,
  val response: Response? = null
)

/** @author Aidan Follestad (@afollestad) */
interface CheckStatusManager {

  suspend fun ensureScheduledChecks()

  fun scheduleCheck(
    site: ServerModel,
    rightNow: Boolean = false,
    cancelPrevious: Boolean = rightNow,
    fromFinishingJob: Boolean = false
  )

  fun cancelCheck(site: ServerModel)

  suspend fun performCheck(site: ServerModel): CheckResult
}

class RealCheckStatusManager @Inject constructor(
  private val jobScheduler: JobScheduler,
  private val okHttpClient: OkHttpClient,
  private val stringProvider: StringProvider,
  private val bundleProvider: BundleProvider,
  private val jobInfoProvider: JobInfoProvider,
  private val siteStore: ServerModelStore
) : CheckStatusManager {

  override suspend fun ensureScheduledChecks() {
    val sites = siteStore.get()
    if (sites.isEmpty()) {
      return
    }
    log("Ensuring enabled sites have scheduled checks.")
    sites.filter { !it.disabled }
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
    site: ServerModel,
    rightNow: Boolean,
    cancelPrevious: Boolean,
    fromFinishingJob: Boolean
  ) {
    check(site.id != 0) { "Cannot schedule checks for jobs with no ID." }
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
      putInt(KEY_SITE_ID, site.id)
    }
    val jobInfo = jobInfoProvider.createCheckJob(
        id = site.id,
        onlyUnmeteredNetwork = false,
        delayMs = if (rightNow) {
          1
        } else {
          site.checkInterval
        },
        extras = extras,
        target = CheckStatusJob::class.java
    )

    val dispatchResult = jobScheduler.schedule(jobInfo)
    if (dispatchResult != RESULT_SUCCESS) {
      log("Failed to schedule a check job for site: ${site.id}")
    } else {
      log("Check job successfully scheduled for site: ${site.id}")
    }
  }

  override fun cancelCheck(site: ServerModel) {
    check(site.id != 0) { "Cannot cancel scheduled checks for jobs with no ID." }
    log("Cancelling scheduled checks for site: ${site.id}")
    jobScheduler.cancel(site.id)
  }

  override suspend fun performCheck(site: ServerModel): CheckResult {
    check(site.id != 0) { "Cannot schedule checks for jobs with no ID." }
    log("performCheck(${site.id}) - GET ${site.url}")

    val request = Request.Builder()
        .url(site.url)
        .get()
        .build()

    return try {
      val response = okHttpClient.newCall(request)
          .execute()
      if (response.isSuccessful || response.code() == 401) {
        log("performCheck(${site.id}) = Successful")
        CheckResult(
            model = site.copy(status = OK, reason = null),
            response = response
        )
      } else {
        log("performCheck(${site.id}) = Failure, HTTP code ${response.code()}")
        CheckResult(
            model = site.copy(
                status = ERROR,
                reason = "Response ${response.code()} - ${response.body()?.string() ?: "Unknown"}"
            ),
            response = response
        )
      }
    } catch (timeoutEx: SocketTimeoutException) {
      log("performCheck(${site.id}) = Socket Timeout")
      CheckResult(
          model = site.copy(
              status = ERROR,
              reason = stringProvider.get(R.string.timeout)
          )
      )
    } catch (ex: Exception) {
      log("performCheck(${site.id}) = Error: ${ex.message}")
      CheckResult(model = site.copy(status = ERROR, reason = ex.message))
    }
  }

  private fun jobForSite(site: ServerModel) =
    jobScheduler.allPendingJobs
        .firstOrNull { job -> job.id == site.id }
}
