/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import android.app.job.JobService
import android.content.Context
import com.afollestad.nocknock.utilities.Injector

fun Context.injector() = applicationContext as Injector

fun JobService.injector() = applicationContext as Injector
