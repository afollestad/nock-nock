/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.di

import com.afollestad.nocknock.ui.addsite.AddSitePresenter
import com.afollestad.nocknock.ui.addsite.RealAddSitePresenter
import com.afollestad.nocknock.ui.main.MainPresenter
import com.afollestad.nocknock.ui.main.RealMainPresenter
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/** @author Aidan Follestad (@afollestad) */
@Module
abstract class MainBindModule {

  @Binds
  @Singleton
  abstract fun provideMainPresenter(
    presenter: RealMainPresenter
  ): MainPresenter

  @Binds
  @Singleton
  abstract fun provideAddSitePresenter(
    presenter: RealAddSitePresenter
  ): AddSitePresenter
}
