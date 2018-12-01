/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.main

import android.content.Intent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.toHtml
import com.afollestad.nocknock.ui.addsite.AddSiteActivity
import com.afollestad.nocknock.ui.addsite.KEY_FAB_SIZE
import com.afollestad.nocknock.ui.addsite.KEY_FAB_X
import com.afollestad.nocknock.ui.addsite.KEY_FAB_Y
import com.afollestad.nocknock.ui.viewsite.KEY_VIEW_MODEL
import com.afollestad.nocknock.ui.viewsite.ViewSiteActivity
import com.afollestad.nocknock.utilities.providers.RealIntentProvider.Companion.KEY_VIEW_NOTIFICATION_MODEL
import kotlinx.android.synthetic.main.activity_main.fab

internal const val VIEW_SITE_RQ = 6923
internal const val ADD_SITE_RQ = 6969

internal fun MainActivity.addSite() {
  startActivityForResult(intentToAdd(fab.x, fab.y, fab.measuredWidth), ADD_SITE_RQ)
}

private fun MainActivity.intentToAdd(
  x: Float,
  y: Float,
  size: Int
) = Intent(this, AddSiteActivity::class.java).apply {
  putExtra(KEY_FAB_X, x)
  putExtra(KEY_FAB_Y, y)
  putExtra(KEY_FAB_SIZE, size)
  addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
}

internal fun MainActivity.viewSite(model: ServerModel) {
  startActivityForResult(intentToView(model), VIEW_SITE_RQ)
}

private fun MainActivity.intentToView(model: ServerModel) =
  Intent(this, ViewSiteActivity::class.java).apply {
    putExtra(KEY_VIEW_MODEL, model)
  }

internal fun MainActivity.maybeRemoveSite(model: ServerModel) {
  MaterialDialog(this).show {
    title(R.string.remove_site)
    message(text = context.getString(R.string.remove_site_prompt, model.name).toHtml())
    positiveButton(R.string.remove) { presenter.removeSite(model) }
    negativeButton(android.R.string.cancel)
  }
}

internal fun MainActivity.processIntent(intent: Intent) {
  if (intent.hasExtra(KEY_VIEW_NOTIFICATION_MODEL)) {
    val model = intent.getSerializableExtra(KEY_VIEW_NOTIFICATION_MODEL) as ServerModel
    viewSite(model)
  }
}
