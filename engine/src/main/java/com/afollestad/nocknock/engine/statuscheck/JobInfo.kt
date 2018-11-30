/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine.statuscheck

import android.app.job.JobInfo
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context

typealias JobInfoBuilder = JobInfo.Builder

fun jobInfo(
  context: Context,
  id: Int,
  target: Class<out JobService>,
  exec: JobInfoBuilder.() -> JobInfoBuilder
): JobInfo {
  val component = ComponentName(context, target)
  val builder = JobInfo.Builder(id, component)
  exec(builder)
  return builder.build()
}
