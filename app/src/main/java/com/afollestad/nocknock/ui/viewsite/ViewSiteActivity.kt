/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.viewsite

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.LAST_CHECK_NONE
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.CHECKING
import com.afollestad.nocknock.data.ServerStatus.WAITING
import com.afollestad.nocknock.data.ValidationMode
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.indexToValidationMode
import com.afollestad.nocknock.data.textRes
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.ui.main.MainActivity
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import com.afollestad.nocknock.utilities.ext.formatDate
import com.afollestad.nocknock.utilities.ext.injector
import com.afollestad.nocknock.utilities.ext.safeRegisterReceiver
import com.afollestad.nocknock.utilities.ext.safeUnregisterReceiver
import com.afollestad.nocknock.utilities.ext.scopeWhileAttached
import com.afollestad.nocknock.viewcomponents.ext.dimenFloat
import com.afollestad.nocknock.viewcomponents.ext.onItemSelected
import com.afollestad.nocknock.viewcomponents.ext.onScroll
import com.afollestad.nocknock.viewcomponents.ext.showOrHide
import com.afollestad.nocknock.viewcomponents.ext.trimmedText
import kotlinx.android.synthetic.main.activity_viewsite.checkIntervalLayout
import kotlinx.android.synthetic.main.activity_viewsite.disableChecksButton
import kotlinx.android.synthetic.main.activity_viewsite.doneBtn
import kotlinx.android.synthetic.main.activity_viewsite.iconStatus
import kotlinx.android.synthetic.main.activity_viewsite.inputName
import kotlinx.android.synthetic.main.activity_viewsite.inputUrl
import kotlinx.android.synthetic.main.activity_viewsite.loadingProgress
import kotlinx.android.synthetic.main.activity_viewsite.responseValidationMode
import kotlinx.android.synthetic.main.activity_viewsite.responseValidationSearchTerm
import kotlinx.android.synthetic.main.activity_viewsite.rootView
import kotlinx.android.synthetic.main.activity_viewsite.scriptInputLayout
import kotlinx.android.synthetic.main.activity_viewsite.scrollView
import kotlinx.android.synthetic.main.activity_viewsite.textLastCheckResult
import kotlinx.android.synthetic.main.activity_viewsite.textNextCheck
import kotlinx.android.synthetic.main.activity_viewsite.textUrlWarning
import kotlinx.android.synthetic.main.activity_viewsite.toolbar
import kotlinx.android.synthetic.main.activity_viewsite.validationModeDescription
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/** @author Aidan Follestad (@afollestad) */
fun MainActivity.intentToView(model: ServerModel) =
  Intent(this, ViewSiteActivity::class.java).apply {
    putExtra(KEY_VIEW_MODEL, model)
  }

/** @author Aidan Follestad (@afollestad) */
class ViewSiteActivity : AppCompatActivity(), ViewSiteView {

  @Inject lateinit var presenter: ViewSitePresenter

  private val intentReceiver = object : BroadcastReceiver() {
    override fun onReceive(
      context: Context,
      intent: Intent
    ) = presenter.onBroadcast(intent)
  }

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    injector().injectInto(this)
    setContentView(R.layout.activity_viewsite)

    toolbar.run {
      setNavigationOnClickListener { finish() }
      inflateMenu(R.menu.menu_viewsite)
      setOnMenuItemClickListener {
        when (it.itemId) {
          R.id.refresh -> presenter.checkNow()
          R.id.remove -> maybeRemoveSite()
        }
        return@setOnMenuItemClickListener true
      }
    }

    scrollView.onScroll {
      toolbar.elevation = if (it > toolbar.height / 4) {
        toolbar.dimenFloat(R.dimen.default_elevation)
      } else {
        0f
      }
    }

    inputUrl.setOnFocusChangeListener { _, hasFocus ->
      presenter.onUrlInputFocusChange(hasFocus, inputUrl.trimmedText())
    }

    val validationOptionsAdapter = ArrayAdapter(
        this,
        R.layout.list_item_spinner,
        resources.getStringArray(R.array.response_validation_options)
    )
    validationOptionsAdapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown)
    responseValidationMode.adapter = validationOptionsAdapter

    responseValidationMode.onItemSelected(presenter::onValidationModeSelected)

    doneBtn.setOnClickListener {
      val checkInterval = checkIntervalLayout.getSelectedCheckInterval()
      val validationMode =
        responseValidationMode.selectedItemPosition.indexToValidationMode()

      presenter.commit(
          name = inputName.trimmedText(),
          url = inputUrl.trimmedText(),
          checkInterval = checkInterval,
          validationMode = validationMode,
          validationContent = validationMode.validationContent()
      )
    }

    disableChecksButton.setOnClickListener { maybeDisableChecks() }

    presenter.takeView(this, intent)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    presenter.onNewIntent(intent)
  }

  override fun onDestroy() {
    presenter.dropView()
    super.onDestroy()
  }

  override fun setLoading() = loadingProgress.setLoading()

  override fun setDoneLoading() = loadingProgress.setDone()

  override fun showOrHideUrlSchemeWarning(show: Boolean) {
    textUrlWarning.showOrHide(show)
    if (show) {
      textUrlWarning.setText(R.string.warning_http_url)
    }
  }

  override fun showOrHideValidationSearchTerm(show: Boolean) =
    responseValidationSearchTerm.showOrHide(show)

  override fun showOrHideScriptInput(show: Boolean) = scriptInputLayout.showOrHide(show)

  override fun setValidationModeDescription(res: Int) = validationModeDescription.setText(res)

  override fun displayModel(model: ServerModel) = with(model) {
    iconStatus.setStatus(this.status)
    inputName.setText(this.name)
    inputUrl.setText(this.url)

    if (this.lastCheck == LAST_CHECK_NONE) {
      textLastCheckResult.setText(R.string.none)
    } else {
      val statusText = this.status.textRes()
      textLastCheckResult.text = if (statusText == 0) {
        this.reason
      } else {
        getString(statusText)
      }
    }

    if (this.disabled) {
      textNextCheck.setText(R.string.auto_checks_disabled)
    } else {
      textNextCheck.text = (this.lastCheck + this.checkInterval).formatDate()
    }
    checkIntervalLayout.set(this.checkInterval)

    responseValidationMode.setSelection(validationMode.value - 1)

    when (this.validationMode) {
      TERM_SEARCH -> responseValidationSearchTerm.setText(this.validationContent ?: "")
      JAVASCRIPT -> scriptInputLayout.setCode(this.validationContent)
      else -> {
        responseValidationSearchTerm.setText("")
        scriptInputLayout.clear()
      }
    }

    disableChecksButton.showOrHide(!this.disabled)
    doneBtn.setText(
        if (this.disabled) R.string.renable_and_save_changes
        else R.string.save_changes
    )

    invalidateMenuForStatus()
  }

  override fun setInputErrors(errors: InputErrors) {
    inputName.error = if (errors.name != null) {
      getString(errors.name!!)
    } else {
      null
    }
    inputUrl.error = if (errors.url != null) {
      getString(errors.url!!)
    } else {
      null
    }
    checkIntervalLayout.setError(
        if (errors.checkInterval != null) {
          getString(errors.checkInterval!!)
        } else {
          null
        }
    )
    responseValidationSearchTerm.error = if (errors.termSearch != null) {
      getString(errors.termSearch!!)
    } else {
      null
    }
    scriptInputLayout.setError(
        if (errors.javaScript != null) {
          getString(errors.javaScript!!)
        } else {
          null
        }
    )
  }

  override fun scopeWhileAttached(
    context: CoroutineContext,
    exec: ScopeReceiver
  ) = rootView.scopeWhileAttached(context, exec)

  override fun onResume() {
    super.onResume()
    val filter = IntentFilter().apply {
      addAction(ACTION_STATUS_UPDATE)
    }
    safeRegisterReceiver(intentReceiver, filter)
  }

  override fun onPause() {
    super.onPause()
    safeUnregisterReceiver(intentReceiver)
  }

  private fun maybeRemoveSite() {
    val model = presenter.currentModel()
    MaterialDialog(this).show {
      title(R.string.remove_site)
      message(
          text = HtmlCompat.fromHtml(
              context.getString(R.string.remove_site_prompt, model.name),
              FROM_HTML_MODE_LEGACY
          )
      )
      positiveButton(R.string.remove) { presenter.removeSite() }
      negativeButton(android.R.string.cancel)
    }
  }

  private fun maybeDisableChecks() {
    val model = presenter.currentModel()
    MaterialDialog(this).show {
      title(R.string.disable_automatic_checks)
      message(
          text = HtmlCompat.fromHtml(
              context.getString(R.string.disable_automatic_checks_prompt, model.name),
              FROM_HTML_MODE_LEGACY
          )
      )
      positiveButton(R.string.disable) { presenter.disableChecks() }
      negativeButton(android.R.string.cancel)
    }
  }

  private fun invalidateMenuForStatus() {
    val model = presenter.currentModel()
    val item = toolbar.menu.findItem(R.id.refresh)
    item.isEnabled = model.status != CHECKING &&
        model.status != WAITING
  }

  private fun ValidationMode.validationContent() = when (this) {
    STATUS_CODE -> null
    TERM_SEARCH -> responseValidationSearchTerm.trimmedText()
    JAVASCRIPT -> scriptInputLayout.getCode()
  }
}
