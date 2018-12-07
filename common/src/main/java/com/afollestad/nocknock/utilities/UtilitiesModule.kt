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
package com.afollestad.nocknock.utilities

import com.afollestad.nocknock.utilities.Qualifiers.IO_DISPATCHER
import com.afollestad.nocknock.utilities.Qualifiers.MAIN_ACTIVITY_CLASS
import com.afollestad.nocknock.utilities.Qualifiers.MAIN_DISPATCHER
import com.afollestad.nocknock.utilities.providers.BitmapProvider
import com.afollestad.nocknock.utilities.providers.BundleProvider
import com.afollestad.nocknock.utilities.providers.IntentProvider
import com.afollestad.nocknock.utilities.providers.JobInfoProvider
import com.afollestad.nocknock.utilities.providers.NotificationChannelProvider
import com.afollestad.nocknock.utilities.providers.NotificationProvider
import com.afollestad.nocknock.utilities.providers.RealBitmapProvider
import com.afollestad.nocknock.utilities.providers.RealBundleProvider
import com.afollestad.nocknock.utilities.providers.RealIntentProvider
import com.afollestad.nocknock.utilities.providers.RealJobInfoProvider
import com.afollestad.nocknock.utilities.providers.RealNotificationChannelProvider
import com.afollestad.nocknock.utilities.providers.RealNotificationProvider
import com.afollestad.nocknock.utilities.providers.RealSdkProvider
import com.afollestad.nocknock.utilities.providers.RealStringProvider
import com.afollestad.nocknock.utilities.providers.SdkProvider
import com.afollestad.nocknock.utilities.providers.StringProvider
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module.module

const val UTILITIES_MODULE = "utilities"

object Qualifiers {
  const val MAIN_ACTIVITY_CLASS = "main.main_activity_class"
  const val MAIN_DISPATCHER = "main.main_dispatcher"
  const val IO_DISPATCHER = "main.io_dispatcher"
}

/** @author Aidan Follestad (@afollestad) */
val utilitiesModule = module(UTILITIES_MODULE) {

  factory(name = MAIN_DISPATCHER) { Dispatchers.Main }

  factory(name = IO_DISPATCHER) { Dispatchers.IO }

  factory { RealSdkProvider() } bind SdkProvider::class

  factory { RealBitmapProvider(get()) } bind BitmapProvider::class

  factory { RealStringProvider(get()) } bind StringProvider::class

  factory {
    RealIntentProvider(get(), get(name = MAIN_ACTIVITY_CLASS))
  } bind IntentProvider::class

  factory {
    RealNotificationChannelProvider(get())
  } bind NotificationChannelProvider::class

  factory { RealNotificationProvider(get()) } bind NotificationProvider::class

  factory { RealBundleProvider() } bind BundleProvider::class

  factory { RealJobInfoProvider(get()) } bind JobInfoProvider::class
}
