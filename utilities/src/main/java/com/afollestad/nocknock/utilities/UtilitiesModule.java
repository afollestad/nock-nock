/*
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.nocknock.utilities;

import com.afollestad.nocknock.utilities.providers.BitmapProvider;
import com.afollestad.nocknock.utilities.providers.BundleProvider;
import com.afollestad.nocknock.utilities.providers.IntentProvider;
import com.afollestad.nocknock.utilities.providers.JobInfoProvider;
import com.afollestad.nocknock.utilities.providers.NotificationChannelProvider;
import com.afollestad.nocknock.utilities.providers.NotificationProvider;
import com.afollestad.nocknock.utilities.providers.RealBitmapProvider;
import com.afollestad.nocknock.utilities.providers.RealBundleProvider;
import com.afollestad.nocknock.utilities.providers.RealIntentProvider;
import com.afollestad.nocknock.utilities.providers.RealJobInfoProvider;
import com.afollestad.nocknock.utilities.providers.RealNotificationChannelProvider;
import com.afollestad.nocknock.utilities.providers.RealNotificationProvider;
import com.afollestad.nocknock.utilities.providers.RealSdkProvider;
import com.afollestad.nocknock.utilities.providers.RealStringProvider;
import com.afollestad.nocknock.utilities.providers.SdkProvider;
import com.afollestad.nocknock.utilities.providers.StringProvider;
import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

/** @author Aidan Follestad (@afollestad) */
@Module
public abstract class UtilitiesModule {

  @Binds
  @Singleton
  abstract SdkProvider provideSdkProvider(RealSdkProvider sdkProvider);

  @Binds
  @Singleton
  abstract BitmapProvider provideBitmapProvider(RealBitmapProvider bitmapProvider);

  @Binds
  @Singleton
  abstract StringProvider provideStringProvider(RealStringProvider stringProvider);

  @Binds
  @Singleton
  abstract IntentProvider provideIntentProvider(RealIntentProvider intentProvider);

  @Binds
  @Singleton
  abstract NotificationChannelProvider provideChannelProvider(
      RealNotificationChannelProvider channelProvider);

  @Binds
  @Singleton
  abstract NotificationProvider provideNotificationProvider(
      RealNotificationProvider notificationProvider);

  @Binds
  @Singleton
  abstract BundleProvider provideBundleProvider(RealBundleProvider bundleProvider);

  @Binds
  @Singleton
  abstract JobInfoProvider provideJobInfoProvider(RealJobInfoProvider jobInfoProvider);
}
