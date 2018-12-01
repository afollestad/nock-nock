/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.di

import com.afollestad.nocknock.R
import com.afollestad.nocknock.utilities.qualifiers.AppIconRes
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** @author Aidan Follestad (@afollestad) */
@Module
open class MainModule {

  @Provides
  @Singleton
  @AppIconRes
  fun provideAppIconRes(): Int = R.mipmap.ic_launcher
}
