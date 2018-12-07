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
package com.afollestad.nocknock.koin

import android.app.Application
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import androidx.room.Room.databaseBuilder
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.notifications.Qualifiers.APP_ICON_RES
import com.afollestad.nocknock.ui.main.MainActivity
import com.afollestad.nocknock.utilities.Qualifiers.MAIN_ACTIVITY_CLASS
import com.afollestad.nocknock.utilities.ext.systemService
import okhttp3.OkHttpClient
import org.koin.dsl.module.module

const val MAIN_MODULE = "main"

/** @author Aidan Follestad (@afollestad) */
val mainModule = module(MAIN_MODULE) {

  single(name = APP_ICON_RES) { R.mipmap.ic_launcher }

  single(name = MAIN_ACTIVITY_CLASS) { MainActivity::class.java }

  single { databaseBuilder(get(), AppDatabase::class.java, "NockNock.db").build() }

  single {
    OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
          val request = chain.request()
              .newBuilder()
              .addHeader("User-Agent", "com.afollestad.nocknock")
              .build()
          chain.proceed(request)
        }
        .build()
  }

  single<JobScheduler> {
    get<Application>().systemService(JOB_SCHEDULER_SERVICE)
  }

  single<NotificationManager> {
    get<Application>().systemService(NOTIFICATION_SERVICE)
  }
}
