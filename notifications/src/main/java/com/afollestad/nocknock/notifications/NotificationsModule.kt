/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.notifications

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/** @author Aidan Follestad (@afollestad) */
@Module
abstract class NotificationsModule {

  @Binds
  @Singleton
  abstract fun provideNockNotificationManager(
    notificationManager: RealNockNotificationManager
  ): NockNotificationManager
}
