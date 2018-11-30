/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock

import android.app.Application
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import com.afollestad.nocknock.di.AppComponent
import com.afollestad.nocknock.di.DaggerAppComponent
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob
import com.afollestad.nocknock.ui.AddSiteActivity
import com.afollestad.nocknock.ui.MainActivity
import com.afollestad.nocknock.ui.ViewSiteActivity
import com.afollestad.nocknock.utilities.Injector
import okhttp3.OkHttpClient

/** @author Aidan Follestad (afollestad) */
class App : Application(), Injector {

  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()

    val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
          val request = chain.request()
              .newBuilder()
              .addHeader("User-Agent", "com.afollestad.nocknock")
              .build()
          chain.proceed(request)
        }
        .build()
    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    appComponent = DaggerAppComponent.builder()
        .application(this)
        .okHttpClient(okHttpClient)
        .jobScheduler(jobScheduler)
        .notificationManager(notificationManager)
        .build()
  }

  override fun injectInto(target: Any) = when (target) {
    is MainActivity -> appComponent.inject(target)
    is ViewSiteActivity -> appComponent.inject(target)
    is AddSiteActivity -> appComponent.inject(target)
    is CheckStatusJob -> appComponent.inject(target)
    else -> throw IllegalStateException("Can't inject into $target")
  }
}
