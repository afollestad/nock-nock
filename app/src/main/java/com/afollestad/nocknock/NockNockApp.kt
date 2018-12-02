/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock

import android.app.Application
import com.afollestad.nocknock.di.AppComponent
import com.afollestad.nocknock.di.DaggerAppComponent
import com.afollestad.nocknock.engine.statuscheck.BootReceiver
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.ui.addsite.AddSiteActivity
import com.afollestad.nocknock.ui.main.MainActivity
import com.afollestad.nocknock.ui.viewsite.ViewSiteActivity
import com.afollestad.nocknock.utilities.Injector
import com.afollestad.nocknock.utilities.ext.systemService
import okhttp3.OkHttpClient
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
class NockNockApp : Application(), Injector {

  private lateinit var appComponent: AppComponent
  @Inject lateinit var nockNotificationManager: NockNotificationManager

  private var resumedActivities: Int = 0

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }

    val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
          val request = chain.request()
              .newBuilder()
              .addHeader("User-Agent", "com.afollestad.nocknock")
              .build()
          chain.proceed(request)
        }
        .build()

    appComponent = DaggerAppComponent.builder()
        .application(this)
        .okHttpClient(okHttpClient)
        .jobScheduler(systemService(JOB_SCHEDULER_SERVICE))
        .notificationManager(systemService(NOTIFICATION_SERVICE))
        .build()
    appComponent.inject(this)

    onActivityLifeChange { activity, resumed ->
      if (resumed) {
        resumedActivities++
        log("Activity resumed: $activity, resumedActivities = $resumedActivities")
      } else {
        resumedActivities--
        log("Activity paused: $activity, resumedActivities = $resumedActivities")
      }
      check(resumedActivities >= 0) { "resumedActivities can't go below 0." }
      nockNotificationManager.setIsAppOpen(resumedActivities > 0)
    }
  }

  override fun injectInto(target: Any) = when (target) {
    is MainActivity -> appComponent.inject(target)
    is ViewSiteActivity -> appComponent.inject(target)
    is AddSiteActivity -> appComponent.inject(target)
    is CheckStatusJob -> appComponent.inject(target)
    is BootReceiver -> appComponent.inject(target)
    else -> throw IllegalStateException("Can't inject into $target")
  }
}
