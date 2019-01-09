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
package com.afollestad.nocknock.ui.main

import android.content.Intent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.toHtml
import com.afollestad.nocknock.ui.addsite.AddSiteActivity
import com.afollestad.nocknock.ui.viewsite.KEY_SITE
import com.afollestad.nocknock.ui.viewsite.ViewSiteActivity
import com.afollestad.nocknock.utilities.providers.RealIntentProvider.Companion.KEY_VIEW_NOTIFICATION_MODEL

internal const val VIEW_SITE_RQ = 6923
internal const val ADD_SITE_RQ = 6969

// ADD

internal fun MainActivity.addSite() {
  startActivityForResult(intentToAdd(), ADD_SITE_RQ)
}

internal fun MainActivity.addSiteForDuplication(site: Site) {
  startActivityForResult(intentToAdd(site), ADD_SITE_RQ)
}

private fun MainActivity.intentToAdd(model: Site? = null) =
  Intent(this, AddSiteActivity::class.java).apply {
    model?.let { putExtra(KEY_SITE, it) }
  }

// VIEW

internal fun MainActivity.viewSite(model: Site) {
  startActivityForResult(intentToView(model), VIEW_SITE_RQ)
}

private fun MainActivity.intentToView(model: Site) =
  Intent(this, ViewSiteActivity::class.java).apply {
    putExtra(KEY_SITE, model)
  }

// MISC

internal fun MainActivity.maybeRemoveSite(model: Site) {
  MaterialDialog(this).show {
    title(R.string.remove_site)
    message(text = context.getString(R.string.remove_site_prompt, model.name).toHtml())
    positiveButton(R.string.remove) { viewModel.removeSite(model) }
    negativeButton(android.R.string.cancel)
  }
}

internal fun MainActivity.processIntent(intent: Intent) {
  if (intent.hasExtra(KEY_VIEW_NOTIFICATION_MODEL)) {
    val model = intent.getSerializableExtra(KEY_VIEW_NOTIFICATION_MODEL) as Site
    viewSite(model)
  }
}
