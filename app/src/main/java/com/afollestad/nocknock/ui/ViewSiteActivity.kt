/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns.WEB_URL
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.nocknock.BuildConfig
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.CHECKING
import com.afollestad.nocknock.data.ServerStatus.WAITING
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.textRes
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.utilities.ext.DAY
import com.afollestad.nocknock.utilities.ext.HOUR
import com.afollestad.nocknock.utilities.ext.MINUTE
import com.afollestad.nocknock.utilities.ext.WEEK
import com.afollestad.nocknock.utilities.ext.formatDate
import com.afollestad.nocknock.utilities.ext.hide
import com.afollestad.nocknock.utilities.ext.injector
import com.afollestad.nocknock.utilities.ext.isHttpOrHttps
import com.afollestad.nocknock.utilities.ext.onItemSelected
import com.afollestad.nocknock.utilities.ext.safeRegisterReceiver
import com.afollestad.nocknock.utilities.ext.safeUnregisterReceiver
import com.afollestad.nocknock.utilities.ext.scopeWhileAttached
import com.afollestad.nocknock.utilities.ext.show
import com.afollestad.nocknock.utilities.ext.showOrHide
import com.afollestad.nocknock.utilities.ext.textAsLong
import com.afollestad.nocknock.utilities.ext.trimmedText
import kotlinx.android.synthetic.main.activity_viewsite.checkIntervalInput
import kotlinx.android.synthetic.main.activity_viewsite.checkIntervalSpinner
import kotlinx.android.synthetic.main.activity_viewsite.content_loading_progress
import kotlinx.android.synthetic.main.activity_viewsite.doneBtn
import kotlinx.android.synthetic.main.activity_viewsite.iconStatus
import kotlinx.android.synthetic.main.activity_viewsite.inputName
import kotlinx.android.synthetic.main.activity_viewsite.inputUrl
import kotlinx.android.synthetic.main.activity_viewsite.responseValidationMode
import kotlinx.android.synthetic.main.activity_viewsite.responseValidationSearchTerm
import kotlinx.android.synthetic.main.activity_viewsite.rootView
import kotlinx.android.synthetic.main.activity_viewsite.textLastCheckResult
import kotlinx.android.synthetic.main.activity_viewsite.textNextCheck
import kotlinx.android.synthetic.main.activity_viewsite.textUrlWarning
import kotlinx.android.synthetic.main.activity_viewsite.toolbar
import kotlinx.android.synthetic.main.activity_viewsite.validationModeDescription
import kotlinx.android.synthetic.main.include_script_input.responseValidationScript
import kotlinx.android.synthetic.main.include_script_input.responseValidationScriptInput
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import kotlin.math.ceil

private const val KEY_VIEW_MODEL = "site_model"

/** @author Aidan Follestad (afViewSiteActivityollestad) */
fun MainActivity.intentToView(model: ServerModel) =
  Intent(this, ViewSiteActivity::class.java).apply {
    putExtra(KEY_VIEW_MODEL, model)
  }

/** @author Aidan Follestad (afollestad) */
class ViewSiteActivity : AppCompatActivity(),
    View.OnClickListener,
    Toolbar.OnMenuItemClickListener {
  companion object {
    private fun log(message: String) {
      if (BuildConfig.DEBUG) {
        Log.d("ViewSiteActivity", message)
      }
    }
  }

  private lateinit var currentModel: ServerModel

  @Inject lateinit var serverModelStore: ServerModelStore
  @Inject lateinit var notificationManager: NockNotificationManager
  @Inject lateinit var checkStatusManager: CheckStatusManager

  private val intentReceiver = object : BroadcastReceiver() {
    override fun onReceive(
      context: Context,
      intent: Intent
    ) {
      log("Received broadcast ${intent.action}")
      val model = intent.getSerializableExtra(KEY_VIEW_MODEL) as? ServerModel
      if (model != null) {
        this@ViewSiteActivity.currentModel = model
        log("Received model update: $currentModel")
        displayCurrentModel()
      }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    injector().injectInto(this)
    setContentView(R.layout.activity_viewsite)

    toolbar.run {
      setNavigationOnClickListener { finish() }
      inflateMenu(R.menu.menu_viewsite)
      setOnMenuItemClickListener(this@ViewSiteActivity)
    }

    val intervalOptionsAdapter = ArrayAdapter(
        this,
        R.layout.list_item_spinner,
        resources.getStringArray(R.array.interval_options)
    )
    intervalOptionsAdapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown)
    checkIntervalSpinner.adapter = intervalOptionsAdapter

    inputUrl.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        val inputStr = inputUrl.text
            .toString()
            .trim()
        if (inputStr.isEmpty()) {
          return@setOnFocusChangeListener
        }

        val uri = Uri.parse(inputStr)
        if (uri.scheme == null) {
          inputUrl.setText("http://$inputStr")
          textUrlWarning.hide()
        } else if (!uri.isHttpOrHttps()) {
          textUrlWarning.show()
          textUrlWarning.setText(R.string.warning_http_url)
        } else {
          textUrlWarning.hide()
        }
      }
    }

    val validationOptionsAdapter = ArrayAdapter(
        this,
        R.layout.list_item_spinner,
        resources.getStringArray(R.array.response_validation_options)
    )
    validationOptionsAdapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown)
    responseValidationMode.adapter = validationOptionsAdapter

    responseValidationMode.onItemSelected { pos ->
      responseValidationSearchTerm.showOrHide(pos == 1)
      responseValidationScript.showOrHide(pos == 2)

      validationModeDescription.setText(
          when (pos) {
            0 -> R.string.validation_mode_status_desc
            1 -> R.string.validation_mode_term_desc
            2 -> R.string.validation_mode_javascript_desc
            else -> throw IllegalStateException("Unexpected position: $pos")
          }
      )
    }

    currentModel = intent.getSerializableExtra(KEY_VIEW_MODEL) as ServerModel
    displayCurrentModel()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    if (intent != null && intent.hasExtra(KEY_VIEW_MODEL)) {
      currentModel = intent.getSerializableExtra(KEY_VIEW_MODEL) as ServerModel
      displayCurrentModel()
    }
  }

  @SuppressLint("SetTextI18n")
  private fun displayCurrentModel() = with(currentModel) {
    iconStatus.setStatus(this.status)
    inputName.setText(this.name)
    inputUrl.setText(this.url)

    if (this.lastCheck == 0L) {
      textLastCheckResult.setText(R.string.none)
    } else {
      val statusText = this.status.textRes()
      textLastCheckResult.text = if (statusText == 0) {
        this.reason
      } else {
        getString(statusText)
      }
    }

    if (this.checkInterval == 0L) {
      textNextCheck.setText(R.string.none_turned_off)
      checkIntervalInput.setText("")
      checkIntervalSpinner.setSelection(0)
    } else {
      var lastCheck = this.lastCheck
      if (lastCheck == 0L) {
        lastCheck = currentTimeMillis()
      }
      textNextCheck.text = (lastCheck + this.checkInterval).formatDate()

      when {
        this.checkInterval >= WEEK -> {
          checkIntervalInput.setText(
              ceil((this.checkInterval.toFloat() / WEEK).toDouble()).toInt().toString()
          )
          checkIntervalSpinner.setSelection(3)
        }
        this.checkInterval >= DAY -> {
          checkIntervalInput.setText(
              ceil((this.checkInterval.toFloat() / DAY.toFloat()).toDouble()).toInt().toString()
          )
          checkIntervalSpinner.setSelection(2)
        }
        this.checkInterval >= HOUR -> {
          checkIntervalInput.setText(
              ceil((this.checkInterval.toFloat() / HOUR.toFloat()).toDouble()).toInt().toString()
          )
          checkIntervalSpinner.setSelection(1)
        }
        this.checkInterval >= MINUTE -> {
          checkIntervalInput.setText(
              ceil((this.checkInterval.toFloat() / MINUTE.toFloat()).toDouble()).toInt().toString()
          )
          checkIntervalSpinner.setSelection(0)
        }
        else -> {
          checkIntervalInput.setText("0")
          checkIntervalSpinner.setSelection(0)
        }
      }
    }

    responseValidationMode.setSelection(validationMode.value - 1)

    when (this.validationMode) {
      TERM_SEARCH -> responseValidationSearchTerm.setText(this.validationContent ?: "")
      JAVASCRIPT -> {
        responseValidationScriptInput.setText(
            this.validationContent ?: getString(R.string.default_js)
        )
      }
      else -> {
        responseValidationSearchTerm.setText("")
        responseValidationScriptInput.setText("")
      }
    }

    doneBtn.setOnClickListener(this@ViewSiteActivity)
    invalidateMenuForStatus()
  }

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

  private fun updateModelFromInput(withValidation: Boolean) {
    currentModel = currentModel.copy(
        name = inputName.trimmedText(),
        url = inputUrl.trimmedText(),
        status = WAITING
    )

    if (withValidation && currentModel.name.isEmpty()) {
      inputName.error = getString(R.string.please_enter_name)
      return
    } else {
      inputName.error = null
    }

    if (withValidation && currentModel.url.isEmpty()) {
      inputUrl.error = getString(R.string.please_enter_url)
      return
    } else {
      inputUrl.error = null
      if (withValidation && !WEB_URL.matcher(currentModel.url).find()) {
        inputUrl.error = getString(R.string.please_enter_valid_url)
        return
      } else {
        val uri = Uri.parse(currentModel.url)
        if (uri.scheme == null) {
          currentModel = currentModel.copy(url = "http://${currentModel.url}")
        }
      }
    }

    val parsedCheckInterval = getParsedCheckInterval()
    val selectedValidationMode = getSelectedValidationMode()
    val selectedValidationContent = getSelectedValidationContent()

    currentModel = currentModel.copy(
        checkInterval = parsedCheckInterval,
        lastCheck = currentTimeMillis() - parsedCheckInterval,
        validationMode = selectedValidationMode,
        validationContent = selectedValidationContent
    )
  }

  // Save button
  override fun onClick(view: View) {
    rootView.scopeWhileAttached(Main) {
      launch(coroutineContext) {
        content_loading_progress.show()
        updateModelFromInput(true)

        async(IO) { serverModelStore.update(currentModel) }.await()
        checkStatusManager.cancelCheck(currentModel)
        checkStatusManager.scheduleCheck(currentModel, rightNow = true)

        content_loading_progress.hide()
        setResult(RESULT_OK)
        finish()
      }
    }
  }

  override fun onMenuItemClick(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.refresh -> {
        rootView.scopeWhileAttached(Main) {
          launch(coroutineContext) {
            content_loading_progress.show()
            updateModelFromInput(false)
            currentModel = currentModel.copy(status = WAITING)
            displayCurrentModel()

            async(IO) { serverModelStore.update(currentModel) }.await()

            checkStatusManager.cancelCheck(currentModel)
            checkStatusManager.scheduleCheck(currentModel, rightNow = true)
            content_loading_progress.hide()
          }
        }
        return true
      }
      R.id.remove -> {
        maybeRemoveSite(currentModel)
        return true
      }
    }
    return false
  }

  private fun maybeRemoveSite(model: ServerModel) {
    MaterialDialog(this).show {
      title(R.string.remove_site)
      message(
          text = HtmlCompat.fromHtml(
              context.getString(R.string.remove_site_prompt, model.name),
              HtmlCompat.FROM_HTML_MODE_LEGACY
          )
      )
      positiveButton(R.string.remove) {
        checkStatusManager.cancelCheck(model)
        notificationManager.cancelStatusNotifications()
        performRemoveSite(model)
      }
      negativeButton(android.R.string.cancel)
    }
  }

  private fun performRemoveSite(model: ServerModel) {
    rootView.scopeWhileAttached(Main) {
      launch(coroutineContext) {
        content_loading_progress.show()
        async(IO) { serverModelStore.delete(model) }.await()
        content_loading_progress.hide()
        finish()
      }
    }
  }

  private fun invalidateMenuForStatus() {
    val item = toolbar.menu.findItem(R.id.refresh)
    item.isEnabled = currentModel.status != CHECKING && currentModel.status != WAITING
  }

  private fun getParsedCheckInterval(): Long {
    val intervalInput = checkIntervalInput.textAsLong()
    return when (checkIntervalSpinner.selectedItemPosition) {
      0 -> intervalInput * (60 * 1000)
      1 -> intervalInput * (60 * 60 * 1000)
      2 -> intervalInput * (60 * 60 * 24 * 1000)
      else -> intervalInput * (60 * 60 * 24 * 7 * 1000)
    }
  }

  private fun getSelectedValidationMode() = when (responseValidationMode.selectedItemPosition) {
    0 -> STATUS_CODE
    1 -> TERM_SEARCH
    2 -> JAVASCRIPT
    else -> {
      throw IllegalStateException(
          "Unexpected validation mode index: ${responseValidationMode.selectedItemPosition}"
      )
    }
  }

  private fun getSelectedValidationContent() = when (responseValidationMode.selectedItemPosition) {
    0 -> null
    1 -> responseValidationSearchTerm.trimmedText()
    2 -> responseValidationScriptInput.trimmedText()
    else -> {
      throw IllegalStateException(
          "Unexpected validation mode index: ${responseValidationMode.selectedItemPosition}"
      )
    }
  }
}
