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
package com.afollestad.nocknock.ui.addsite

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ValidationMode
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.indexToValidationMode
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import com.afollestad.nocknock.utilities.ext.injector
import com.afollestad.nocknock.utilities.ext.scopeWhileAttached
import com.afollestad.nocknock.viewcomponents.ext.conceal
import com.afollestad.nocknock.viewcomponents.ext.onItemSelected
import com.afollestad.nocknock.viewcomponents.ext.onLayout
import com.afollestad.nocknock.viewcomponents.ext.showOrHide
import com.afollestad.nocknock.viewcomponents.ext.textAsInt
import com.afollestad.nocknock.viewcomponents.ext.trimmedText
import kotlinx.android.synthetic.main.activity_addsite.checkIntervalLayout
import kotlinx.android.synthetic.main.activity_addsite.doneBtn
import kotlinx.android.synthetic.main.activity_addsite.inputName
import kotlinx.android.synthetic.main.activity_addsite.inputUrl
import kotlinx.android.synthetic.main.activity_addsite.loadingProgress
import kotlinx.android.synthetic.main.activity_addsite.responseTimeoutInput
import kotlinx.android.synthetic.main.activity_addsite.responseValidationMode
import kotlinx.android.synthetic.main.activity_addsite.responseValidationSearchTerm
import kotlinx.android.synthetic.main.activity_addsite.rootView
import kotlinx.android.synthetic.main.activity_addsite.scriptInputLayout
import kotlinx.android.synthetic.main.activity_addsite.textUrlWarning
import kotlinx.android.synthetic.main.activity_addsite.toolbar
import kotlinx.android.synthetic.main.activity_addsite.validationModeDescription
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.properties.Delegates.notNull

const val KEY_FAB_X = "fab_x"
const val KEY_FAB_Y = "fab_y"
const val KEY_FAB_SIZE = "fab_size"

/** @author Aidan Follestad (@afollestad) */
class AddSiteActivity : AppCompatActivity(), AddSiteView {

  var isClosing: Boolean = false
  var revealCx by notNull<Int>()
  var revealCy by notNull<Int>()
  var revealRadius by notNull<Float>()

  @Inject lateinit var presenter: AddSitePresenter

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    injector().injectInto(this)
    setContentView(R.layout.activity_addsite)
    presenter.takeView(this)

    toolbar.setNavigationOnClickListener { closeActivityWithReveal() }

    if (savedInstanceState == null) {
      rootView.conceal()
      rootView.onLayout {
        val fabSize = intent.getIntExtra(KEY_FAB_SIZE, 0)
        val fabX = intent.getFloatExtra(KEY_FAB_X, 0f)
            .toInt()
        val fabY = intent.getFloatExtra(KEY_FAB_Y, 0f)
            .toInt()

        revealCx = fabX + fabSize / 2
        revealCy = (fabY + toolbar.measuredHeight + fabSize / 2)
        revealRadius = max(revealCx, revealCy).toFloat()

        circularRevealActivity()
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
      val defaultTimeout = getString(R.string.response_timeout_default).toInt()

      isClosing = true
      presenter.commit(
          name = inputName.trimmedText(),
          url = inputUrl.trimmedText(),
          checkInterval = checkInterval,
          validationMode = validationMode,
          validationContent = validationMode.validationContent(),
          networkTimeout = responseTimeoutInput.textAsInt(defaultValue = defaultTimeout)
      )
    }
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

  override fun setInputErrors(errors: InputErrors) {
    isClosing = false
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
    responseTimeoutInput.error = if (errors.networkTimeout != null) {
      getString(errors.networkTimeout!!)
    } else {
      null
    }
  }

  override fun onSiteAdded() {
    setResult(RESULT_OK)
    finish()
    overridePendingTransition(R.anim.fade_out, R.anim.fade_out)
  }

  override fun scopeWhileAttached(
    context: CoroutineContext,
    exec: ScopeReceiver
  ) = rootView.scopeWhileAttached(context, exec)

  override fun onBackPressed() = closeActivityWithReveal()

  private fun ValidationMode.validationContent() = when (this) {
    STATUS_CODE -> null
    TERM_SEARCH -> responseValidationSearchTerm.trimmedText()
    JAVASCRIPT -> scriptInputLayout.getCode()
  }
}
