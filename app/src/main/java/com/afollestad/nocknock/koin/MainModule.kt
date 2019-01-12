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
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.Database1to2Migration
import com.afollestad.nocknock.data.Database2to3Migration
import com.afollestad.nocknock.data.Database3to4Migration
import com.afollestad.nocknock.data.Database4to5Migration
import com.afollestad.nocknock.notifications.Qualifiers.MAIN_ACTIVITY_CLASS
import com.afollestad.nocknock.ui.main.MainActivity
import com.afollestad.nocknock.utilities.ext.systemService
import okhttp3.OkHttpClient
import org.koin.dsl.module.module

val mainActivityCls = MainActivity::class.java

/** @author Aidan Follestad (@afollestad) */
val mainModule = module {

  single(name = MAIN_ACTIVITY_CLASS) { mainActivityCls }

  single {
    databaseBuilder(get(), AppDatabase::class.java, "NockNock.db")
        .addMigrations(
            Database1to2Migration(),
            Database2to3Migration(),
            Database3to4Migration(),
            Database4to5Migration()
        )
        .build()
  }

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
