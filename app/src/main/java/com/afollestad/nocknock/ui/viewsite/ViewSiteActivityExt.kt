/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.viewsite

import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.isPending
import com.afollestad.nocknock.toHtml
import com.afollestad.nocknock.utilities.ext.animateRotation
import kotlinx.android.synthetic.main.activity_viewsite.toolbar

internal fun ViewSiteActivity.maybeRemoveSite() {
  val model = presenter.currentModel()
  MaterialDialog(this).show {
    title(R.string.remove_site)
    message(text = context.getString(R.string.remove_site_prompt, model.name).toHtml())
    positiveButton(R.string.remove) { presenter.removeSite() }
    negativeButton(android.R.string.cancel)
  }
}

internal fun ViewSiteActivity.maybeDisableChecks() {
  val model = presenter.currentModel()
  MaterialDialog(this).show {
    title(R.string.disable_automatic_checks)
    message(
        text = context.getString(R.string.disable_automatic_checks_prompt, model.name).toHtml()
    )
    positiveButton(R.string.disable) { presenter.disableChecks() }
    negativeButton(android.R.string.cancel)
  }
}

internal fun ViewSiteActivity.invalidateMenuForStatus(model: ServerModel) {
  val refreshIcon = toolbar.menu.findItem(R.id.refresh)
      .actionView as ImageView

  if (model.status.isPending()) {
    refreshIcon.animateRotation()
  } else {
    refreshIcon.run {
      animate().cancel()
      rotation = 0f
    }
  }
}
