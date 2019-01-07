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
package com.afollestad.nocknock.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.nocknock.R
import com.afollestad.nocknock.koin.PREF_DARK_MODE
import com.afollestad.nocknock.utilities.rx.attachLifecycle
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (afollestad) */
abstract class DarkModeSwitchActivity : AppCompatActivity() {

  private var isDarkModeEnabled: Boolean = false
  private val darkModePref by inject<Pref<Boolean>>(name = PREF_DARK_MODE)

  override fun onCreate(savedInstanceState: Bundle?) {
    isDarkModeEnabled = darkModePref.get()
    setTheme(themeRes())
    super.onCreate(savedInstanceState)

    darkModePref.observe()
        .filter { it != isDarkModeEnabled }
        .subscribe {
          log("Theme changed, recreating Activity.")
          recreate()
        }
        .attachLifecycle(this)
  }

  override fun onResume() {
    super.onResume()
  }

  private fun themeRes() = if (darkModePref.get()) {
    R.style.AppTheme_Dark
  } else {
    R.style.AppTheme
  }
}
