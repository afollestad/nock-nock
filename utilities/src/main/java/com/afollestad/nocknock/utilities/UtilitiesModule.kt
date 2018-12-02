/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities

import com.afollestad.nocknock.utilities.providers.BitmapProvider
import com.afollestad.nocknock.utilities.providers.IntentProvider
import com.afollestad.nocknock.utilities.providers.NotificationChannelProvider
import com.afollestad.nocknock.utilities.providers.NotificationProvider
import com.afollestad.nocknock.utilities.providers.RealBitmapProvider
import com.afollestad.nocknock.utilities.providers.RealIntentProvider
import com.afollestad.nocknock.utilities.providers.RealNotificationChannelProvider
import com.afollestad.nocknock.utilities.providers.RealNotificationProvider
import com.afollestad.nocknock.utilities.providers.RealSdkProvider
import com.afollestad.nocknock.utilities.providers.RealStringProvider
import com.afollestad.nocknock.utilities.providers.SdkProvider
import com.afollestad.nocknock.utilities.providers.StringProvider
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/** @author Aidan Follestad (@afollestad) */
@Module
abstract class UtilitiesModule {

  @Binds
  @Singleton
  abstract fun provideSdkProvider(
    sdkProvider: RealSdkProvider
  ): SdkProvider

  @Binds
  @Singleton
  abstract fun provideBitmapProvider(
    bitmapProvider: RealBitmapProvider
  ): BitmapProvider

  @Binds
  @Singleton
  abstract fun provideStringProvider(
    stringProvider: RealStringProvider
  ): StringProvider

  @Binds
  @Singleton
  abstract fun provideIntentProvider(
    intentProvider: RealIntentProvider
  ): IntentProvider

  @Binds
  @Singleton
  abstract fun provideChannelProvider(
    channelProvider: RealNotificationChannelProvider
  ): NotificationChannelProvider

  @Binds
  @Singleton
  abstract fun provideNotificationProvider(
    notificationProvider: RealNotificationProvider
  ): NotificationProvider
}
