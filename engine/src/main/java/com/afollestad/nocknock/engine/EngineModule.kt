/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine

import com.afollestad.nocknock.engine.db.RealServerModelStore
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
import com.afollestad.nocknock.engine.statuscheck.RealCheckStatusManager
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/** @author Aidan Follestad (afollestad) */
@Module
abstract class EngineModule {

  @Binds
  @Singleton
  abstract fun provideServerModelStore(
    serverModelStore: RealServerModelStore
  ): ServerModelStore

  @Binds
  @Singleton
  abstract fun provideCheckStatusManager(
    checkStatusManager: RealCheckStatusManager
  ): CheckStatusManager
}
