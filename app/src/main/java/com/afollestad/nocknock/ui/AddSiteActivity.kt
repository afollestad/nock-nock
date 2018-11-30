/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.net.Uri
import android.os.Bundle
import android.util.Patterns.WEB_URL
import android.view.View
import android.view.ViewAnimationUtils.createCircularReveal
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.WAITING
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
import com.afollestad.nocknock.utilities.ext.conceal
import com.afollestad.nocknock.utilities.ext.hide
import com.afollestad.nocknock.utilities.ext.injector
import com.afollestad.nocknock.utilities.ext.onEnd
import com.afollestad.nocknock.utilities.ext.onItemSelected
import com.afollestad.nocknock.utilities.ext.onLayout
import com.afollestad.nocknock.utilities.ext.scopeWhileAttached
import com.afollestad.nocknock.utilities.ext.show
import com.afollestad.nocknock.utilities.ext.showOrHide
import com.afollestad.nocknock.utilities.ext.textAsLong
import com.afollestad.nocknock.utilities.ext.trimmedText
import kotlinx.android.synthetic.main.activity_addsite.checkIntervalInput
import kotlinx.android.synthetic.main.activity_addsite.checkIntervalSpinner
import kotlinx.android.synthetic.main.activity_addsite.content_loading_progress
import kotlinx.android.synthetic.main.activity_addsite.doneBtn
import kotlinx.android.synthetic.main.activity_addsite.inputName
import kotlinx.android.synthetic.main.activity_addsite.inputUrl
import kotlinx.android.synthetic.main.activity_addsite.nameTiLayout
import kotlinx.android.synthetic.main.activity_addsite.responseValidationMode
import kotlinx.android.synthetic.main.activity_addsite.responseValidationScript
import kotlinx.android.synthetic.main.activity_addsite.responseValidationScriptInput
import kotlinx.android.synthetic.main.activity_addsite.responseValidationSearchTerm
import kotlinx.android.synthetic.main.activity_addsite.rootView
import kotlinx.android.synthetic.main.activity_addsite.textUrlWarning
import kotlinx.android.synthetic.main.activity_addsite.toolbar
import kotlinx.android.synthetic.main.activity_addsite.urlTiLayout
import kotlinx.android.synthetic.main.activity_addsite.validationModeDescription
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import kotlin.math.max

private const val KEY_FAB_X = "fab_x"
private const val KEY_FAB_Y = "fab_y"
private const val KEY_FAB_SIZE = "fab_size"

/** @author Aidan Follestad (afollestad) */
fun MainActivity.intentToAdd(
  x: Float,
  y: Float,
  size: Int
) = Intent(this, AddSiteActivity::class.java).apply {
  putExtra(KEY_FAB_X, x)
  putExtra(KEY_FAB_Y, y)
  putExtra(KEY_FAB_SIZE, size)
  addFlags(FLAG_ACTIVITY_NO_ANIMATION)
}

/** @author Aidan Follestad (afollestad) */
class AddSiteActivity : AppCompatActivity(), View.OnClickListener {

  private var isClosing: Boolean = false

  @Inject lateinit var serverModelStore: ServerModelStore
  @Inject lateinit var checkStatusManager: CheckStatusManager

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    injector().injectInto(this)

    setContentView(R.layout.activity_addsite)
    toolbar.setNavigationOnClickListener { closeActivityWithReveal() }

    if (savedInstanceState == null) {
      rootView.conceal()
      rootView.onLayout { circularRevealActivity() }
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
        } else if ("http" != uri.scheme && "https" != uri.scheme) {
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
            else -> throw IllegalStateException("Unknown validation mode position: $pos")
          }
      )
    }

    doneBtn.setOnClickListener(this)
  }

  private fun closeActivityWithReveal() {
    if (isClosing) {
      return
    }

    isClosing = true
    val fabSize = intent.getIntExtra(KEY_FAB_SIZE, toolbar!!.measuredHeight)

    val defaultCx = rootView.measuredWidth / 2f
    val cx =
      intent.getFloatExtra(KEY_FAB_X, defaultCx).toInt() + fabSize / 2

    val defaultCy = rootView.measuredHeight / 2f
    val cy = (intent.getFloatExtra(KEY_FAB_Y, defaultCy).toInt() +
        toolbar!!.measuredHeight +
        fabSize / 2)

    val initialRadius = max(cx, cy).toFloat()
    createCircularReveal(rootView, cx, cy, initialRadius, 0f)
        .apply {
          duration = 300
          interpolator = AccelerateInterpolator()
          onEnd {
            rootView.conceal()
            finish()
            overridePendingTransition(0, 0)
          }
          start()
        }
  }

  private fun circularRevealActivity() {
    val cx = rootView.measuredWidth / 2
    val cy = rootView.measuredHeight / 2
    val finalRadius = Math.max(cx, cy)
        .toFloat()
    val circularReveal = createCircularReveal(rootView, cx, cy, 0f, finalRadius)
        .apply {
          duration = 300
          interpolator = DecelerateInterpolator()
        }
    rootView.show()
    circularReveal.start()
  }

  // Done button
  override fun onClick(view: View) {
    isClosing = true
    var model = ServerModel(
        name = inputName.trimmedText(),
        url = inputUrl.trimmedText(),
        status = WAITING,
        validationMode = STATUS_CODE
    )

    if (model.name.isEmpty()) {
      nameTiLayout.error = getString(R.string.please_enter_name)
      isClosing = false
      return
    } else {
      nameTiLayout.error = null
    }

    if (model.url.isEmpty()) {
      urlTiLayout.error = getString(R.string.please_enter_url)
      isClosing = false
      return
    } else {
      urlTiLayout.error = null
      if (!WEB_URL.matcher(model.url).find()) {
        urlTiLayout.error = getString(R.string.please_enter_valid_url)
        isClosing = false
        return
      } else {
        val uri = Uri.parse(model.url)
        if (uri.scheme == null) {
          model = model.copy(url = "http://${model.url}")
        }
      }
    }

    val intervalValue = checkIntervalInput.textAsLong()

    model = when (checkIntervalSpinner.selectedItemPosition) {
      0 -> model.copy(checkInterval = intervalValue * (60 * 1000))
      1 -> model.copy(checkInterval = intervalValue * (60 * 60 * 1000))
      2 -> model.copy(checkInterval = intervalValue * (60 * 60 * 24 * 1000))
      else -> model.copy(checkInterval = intervalValue * (60 * 60 * 24 * 7 * 1000))
    }
    model = model.copy(lastCheck = currentTimeMillis() - model.checkInterval)

    when (responseValidationMode.selectedItemPosition) {
      0 -> {
        model = model.copy(validationMode = STATUS_CODE, validationContent = null)
      }
      1 -> {
        model = model.copy(
            validationMode = TERM_SEARCH,
            validationContent = responseValidationSearchTerm.trimmedText()
        )
      }
      2 -> {
        model = model.copy(
            validationMode = JAVASCRIPT,
            validationContent = responseValidationScriptInput.trimmedText()
        )
      }
      else -> {
        throw IllegalStateException(
            "Unexpected validation mode index: ${responseValidationMode.selectedItemPosition}"
        )
      }
    }

    rootView.scopeWhileAttached(Main) {
      launch(coroutineContext) {
        content_loading_progress.show()
        val storedModel = async(IO) { serverModelStore.put(model) }.await()
        checkStatusManager.cancelCheck(storedModel)
        checkStatusManager.scheduleCheck(storedModel, rightNow = true)
        content_loading_progress.hide()

        setResult(RESULT_OK)
        finish()
        overridePendingTransition(R.anim.fade_out, R.anim.fade_out)
      }
    }
  }

  override fun onBackPressed() = closeActivityWithReveal()
}
