/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.R

/** @author Aidan Follestad (afollestad) */
class AboutDialog : DialogFragment() {
  companion object {
    private const val TAG = "[ABOUT_DIALOG]"

    fun show(context: AppCompatActivity) {
      val dialog = AboutDialog()
      dialog.show(context.supportFragmentManager, TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialDialog(activity!!)
        .title(R.string.about)
        .positiveButton(R.string.dismiss)
        .message(R.string.about_body, html = true, lineHeightMultiplier = 1.4f)
  }
}
