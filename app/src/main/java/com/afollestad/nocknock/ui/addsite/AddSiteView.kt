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
package com.afollestad.nocknock.ui.addsite

import androidx.annotation.StringRes
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import kotlin.coroutines.CoroutineContext

/** @author Aidan Follestad (@afollestad) */
interface AddSiteView {

  fun setLoading()

  fun setDoneLoading()

  fun showOrHideUrlSchemeWarning(show: Boolean)

  fun showOrHideValidationSearchTerm(show: Boolean)

  fun showOrHideScriptInput(show: Boolean)

  fun setValidationModeDescription(@StringRes res: Int)

  fun setInputErrors(errors: InputErrors)

  fun onSiteAdded()

  fun scopeWhileAttached(
    context: CoroutineContext,
    exec: ScopeReceiver
  )
}
