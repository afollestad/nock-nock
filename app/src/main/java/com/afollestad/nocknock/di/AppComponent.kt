/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.di

import android.app.Application
import android.app.NotificationManager
import android.app.job.JobScheduler
import com.afollestad.nocknock.engine.EngineModule
import com.afollestad.nocknock.engine.statuscheck.BootReceiver
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob
import com.afollestad.nocknock.notifications.NotificationsModule
import com.afollestad.nocknock.ui.AddSiteActivity
import com.afollestad.nocknock.ui.MainActivity
import com.afollestad.nocknock.ui.ViewSiteActivity
import com.afollestad.nocknock.utilities.UtilitiesModule
import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** @author Aidan Follestad (afollestad) */
@Singleton
@Component(
    modules = [
      MainModule::class,
      EngineModule::class,
      NotificationsModule::class,
      UtilitiesModule::class
    ]
)
interface AppComponent {

  fun inject(activity: MainActivity)

  fun inject(activity: ViewSiteActivity)

  fun inject(activity: AddSiteActivity)

  fun inject(job: CheckStatusJob)

  fun inject(bootReceiver: BootReceiver)

  @Component.Builder
  interface Builder {

    @BindsInstance fun application(application: Application): Builder

    @BindsInstance fun okHttpClient(okHttpClient: OkHttpClient): Builder

    @BindsInstance fun jobScheduler(jobScheduler: JobScheduler): Builder

    @BindsInstance fun notificationManager(notificationManager: NotificationManager): Builder

    fun build(): AppComponent
  }
}
