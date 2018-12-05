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

import android.app.Application;
import android.app.NotificationManager;
import android.app.job.JobScheduler;
import com.afollestad.nocknock.NockNockApp;
import com.afollestad.nocknock.engine.EngineModule;
import com.afollestad.nocknock.engine.statuscheck.BootReceiver;
import com.afollestad.nocknock.engine.statuscheck.ValidationJob;
import com.afollestad.nocknock.notifications.NotificationsModule;
import com.afollestad.nocknock.ui.addsite.AddSiteActivity;
import com.afollestad.nocknock.ui.main.MainActivity;
import com.afollestad.nocknock.ui.viewsite.ViewSiteActivity;
import com.afollestad.nocknock.utilities.UtilitiesModule;
import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

/** @author Aidan Follestad (@afollestad) */
@Singleton
@Component(
  modules = {MainModule.class, EngineModule.class, NotificationsModule.class, UtilitiesModule.class}
)
public interface AppComponent {

  void inject(NockNockApp app);

  void inject(MainActivity activity);

  void inject(ViewSiteActivity activity);

  void inject(AddSiteActivity activity);

  void inject(ValidationJob job);

  void inject(BootReceiver bootReceiver);

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder application(Application application);

    @BindsInstance
    Builder okHttpClient(OkHttpClient okHttpClient);

    @BindsInstance
    Builder jobScheduler(JobScheduler jobScheduler);

    @BindsInstance
    Builder notificationManager(NotificationManager notificationManager);

    AppComponent build();
  }
}
