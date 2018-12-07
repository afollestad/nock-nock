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
package com.afollestad.nocknock.utilities.providers

import android.content.Context
import androidx.annotation.StringRes

/** @author Aidan Follestad (@afollestad) */
interface StringProvider {

  fun get(@StringRes res: Int): String
}

/** @author Aidan Follestad (@afollestad) */
class RealStringProvider(
  private val context: Context
) : StringProvider {

  override fun get(res: Int): String {
    return context.resources.getString(res)
  }
}
