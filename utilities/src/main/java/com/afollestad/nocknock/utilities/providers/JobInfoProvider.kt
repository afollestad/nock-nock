/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobInfo.NETWORK_TYPE_UNMETERED
import android.content.ComponentName
import android.os.PersistableBundle
import javax.inject.Inject

interface JobInfoProvider {

  fun createCheckJob(
    id: Int,
    onlyUnmeteredNetwork: Boolean,
    delayMs: Long,
    extras: PersistableBundle,
    target: Class<*>
  ): JobInfo
}

class RealJobInfoProvider @Inject constructor(
  private val app: Application
) : JobInfoProvider {

  // Note: we don't use the periodic feature of JobScheduler because it requires a
  // minimum of 15 minutes between each execution which may not be what's requested by the
  // user of the app.
  override fun createCheckJob(
    id: Int,
    onlyUnmeteredNetwork: Boolean,
    delayMs: Long,
    extras: PersistableBundle,
    target: Class<*>
  ): JobInfo {
    val component = ComponentName(app, target)
    val networkType = if (onlyUnmeteredNetwork) {
      NETWORK_TYPE_UNMETERED
    } else {
      NETWORK_TYPE_ANY
    }
    return JobInfo.Builder(id, component)
        .setRequiredNetworkType(networkType)
        .setMinimumLatency(delayMs)
        .setExtras(extras)
        .build()
  }
}
