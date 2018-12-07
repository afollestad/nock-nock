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

import com.afollestad.nocknock.ui.addsite.AddSiteViewModel
import com.afollestad.nocknock.ui.main.MainViewModel
import com.afollestad.nocknock.ui.viewsite.ViewSiteViewModel
import com.afollestad.nocknock.utilities.Qualifiers.IO_DISPATCHER
import com.afollestad.nocknock.utilities.Qualifiers.MAIN_DISPATCHER
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/** @author Aidan Follestad (@afollestad) */
val viewModelModule = module {

  viewModel {
    MainViewModel(
        get(),
        get(),
        get(),
        get(name = MAIN_DISPATCHER),
        get(name = IO_DISPATCHER)
    )
  }

  viewModel {
    AddSiteViewModel(
        get(),
        get(),
        get(name = MAIN_DISPATCHER),
        get(name = IO_DISPATCHER)
    )
  }

  viewModel {
    ViewSiteViewModel(
        get(),
        get(),
        get(),
        get(),
        get(name = MAIN_DISPATCHER),
        get(name = IO_DISPATCHER)
    )
  }
}
