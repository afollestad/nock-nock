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
package com.afollestad.nocknock.di;

import static androidx.room.Room.databaseBuilder;

import android.app.Application;
import com.afollestad.nocknock.R;
import com.afollestad.nocknock.data.AppDatabase;
import com.afollestad.nocknock.ui.addsite.AddSitePresenter;
import com.afollestad.nocknock.ui.addsite.RealAddSitePresenter;
import com.afollestad.nocknock.ui.main.MainActivity;
import com.afollestad.nocknock.ui.main.MainPresenter;
import com.afollestad.nocknock.ui.main.RealMainPresenter;
import com.afollestad.nocknock.ui.viewsite.RealViewSitePresenter;
import com.afollestad.nocknock.ui.viewsite.ViewSitePresenter;
import com.afollestad.nocknock.utilities.qualifiers.AppIconRes;
import com.afollestad.nocknock.utilities.qualifiers.MainActivityClass;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/** @author Aidan Follestad (@afollestad) */
@Module
abstract class MainModule {
  @SuppressWarnings("FieldCanBeLocal")
  private static String DATABASE_NAME = "NockNock.db";

  @Binds
  @Singleton
  abstract MainPresenter provideMainPresenter(RealMainPresenter presenter);

  @Binds
  @Singleton
  abstract AddSitePresenter provideAddSitePresenter(RealAddSitePresenter presenter);

  @Binds
  @Singleton
  abstract ViewSitePresenter provideViewSitePresenter(RealViewSitePresenter presenter);

  @Provides
  @Singleton
  @AppIconRes
  static int provideAppIconRes() {
    return R.mipmap.ic_launcher;
  }

  @Provides
  @Singleton
  @MainActivityClass
  static Class<?> provideMainActivityClass() {
    return MainActivity.class;
  }

  @Provides
  @Singleton
  static AppDatabase provideAppDatabase(Application app) {
    return databaseBuilder(app, AppDatabase.class, DATABASE_NAME).build();
  }
}
