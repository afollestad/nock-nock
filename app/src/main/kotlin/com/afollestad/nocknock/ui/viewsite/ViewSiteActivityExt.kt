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
package com.afollestad.nocknock.ui.viewsite

import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.model.Status
import com.afollestad.nocknock.data.model.isPending
import com.afollestad.nocknock.toHtml
import com.afollestad.nocknock.utilities.ext.animateRotation
import kotlinx.android.synthetic.main.activity_viewsite.toolbar

const val KEY_SITE = "site_model"

internal fun ViewSiteActivity.maybeRemoveSite() {
  val model = viewModel.site
  MaterialDialog(this).show {
    title(R.string.remove_site)
    message(text = context.getString(R.string.remove_site_prompt, model.name).toHtml())
    positiveButton(R.string.remove) {
      viewModel.removeSite { finish() }
    }
    negativeButton(android.R.string.cancel)
  }
}

internal fun ViewSiteActivity.maybeDisableChecks() {
  val model = viewModel.site
  MaterialDialog(this).show {
    title(R.string.disable_automatic_checks)
    message(
        text = context.getString(R.string.disable_automatic_checks_prompt, model.name).toHtml()
    )
    positiveButton(R.string.disable) { viewModel.disableSite() }
    negativeButton(android.R.string.cancel)
  }
}

internal fun ViewSiteActivity.invalidateMenuForStatus(status: Status) {
  val refreshIcon = toolbar.menu.findItem(R.id.refresh)
      .actionView as ImageView
  if (status.isPending()) {
    refreshIcon.animateRotation()
  } else {
    refreshIcon.run {
      animate().cancel()
      rotation = 0f
    }
  }
}
