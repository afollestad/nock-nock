/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.main

import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import kotlin.coroutines.CoroutineContext

/** @author Aidan Follestad (afollestad) */
interface MainView {

  fun setModels(models: List<ServerModel>)

  fun updateModel(model: ServerModel)

  fun onSiteDeleted(model: ServerModel)

  fun scopeWhileAttached(
    context: CoroutineContext,
    exec: ScopeReceiver
  )
}
