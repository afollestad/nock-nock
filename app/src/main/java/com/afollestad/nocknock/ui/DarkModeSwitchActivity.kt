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

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.nocknock.R
import com.afollestad.nocknock.koin.PREF_DARK_MODE
import com.afollestad.nocknock.ui.NightMode.DISABLED
import com.afollestad.nocknock.ui.NightMode.ENABLED
import com.afollestad.nocknock.ui.NightMode.UNKNOWN
import com.afollestad.nocknock.utilities.rx.attachLifecycle
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (afollestad) */
abstract class DarkModeSwitchActivity : AppCompatActivity() {

  private var isDarkModeEnabled: Boolean = false
  private val darkModePref by inject<Pref<Boolean>>(name = PREF_DARK_MODE)

  override fun onCreate(savedInstanceState: Bundle?) {
    isDarkModeEnabled = isDarkMode()
    setTheme(themeRes())
    super.onCreate(savedInstanceState)

    if (getCurrentNightMode() == UNKNOWN) {
      darkModePref.observe()
          .filter { it != isDarkModeEnabled }
          .subscribe {
            log("Theme changed, recreating Activity.")
            recreate()
          }
          .attachLifecycle(this)
    }
  }

  protected fun getCurrentNightMode(): NightMode {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      return UNKNOWN
    }
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_YES -> return ENABLED
      Configuration.UI_MODE_NIGHT_NO -> return DISABLED
      else -> UNKNOWN
    }
  }

  protected fun isDarkMode(): Boolean {
    return when (getCurrentNightMode()) {
      ENABLED -> true
      DISABLED -> false
      else -> darkModePref.get()
    }
  }

  protected fun toggleDarkMode() = setDarkMode(!isDarkMode())

  private fun setDarkMode(darkMode: Boolean) = darkModePref.set(darkMode)

  private fun themeRes() = if (isDarkMode()) {
    R.style.AppTheme_Dark
  } else {
    R.style.AppTheme
  }
}
