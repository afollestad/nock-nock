/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.addsite

import androidx.annotation.StringRes
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import kotlin.coroutines.CoroutineContext

/** @author Aidan Follestad (afollestad) */
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
