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
import com.afollestad.nocknock.data.model.ValidationMode
import com.afollestad.nocknock.viewcomponents.ext.conceal
import com.afollestad.nocknock.viewcomponents.ext.onLayout
import com.afollestad.nocknock.viewcomponents.livedata.attachLiveData
import com.afollestad.nocknock.viewcomponents.livedata.toViewError
import com.afollestad.nocknock.viewcomponents.livedata.toViewText
import com.afollestad.nocknock.viewcomponents.livedata.toViewVisibility
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.max
import kotlin.properties.Delegates.notNull

const val KEY_FAB_X = "fab_x"
const val KEY_FAB_Y = "fab_y"
const val KEY_FAB_SIZE = "fab_size"

/** @author Aidan Follestad (@afollestad) */
class AddSiteActivity : AppCompatActivity() {

  var revealCx by notNull<Int>()
  var revealCy by notNull<Int>()
  var revealRadius by notNull<Float>()

  internal var isClosing = false

  private val viewModel by viewModel<AddSiteViewModel>()

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_addsite)
    setupUi(savedInstanceState)

    lifecycle.addObserver(viewModel)

    // Loading
    loadingProgress.observe(this, viewModel.onIsLoading())

    // Name
    inputName.attachLiveData(this, viewModel.name)
    viewModel.onNameError()
        .toViewError(this, inputName)

    // Url
    inputUrl.attachLiveData(this, viewModel.url)
    viewModel.onUrlError()
        .toViewError(this, inputUrl)
    viewModel.onUrlWarningVisibility()
        .toViewVisibility(this, textUrlWarning)

    // Timeout
    responseTimeoutInput.attachLiveData(this, viewModel.timeout)
    viewModel.onTimeoutError()
        .toViewError(this, responseTimeoutInput)

    // Validation mode
    responseValidationMode.attachLiveData(
        lifecycleOwner = this,
        data = viewModel.validationMode,
        outTransformer = { ValidationMode.fromIndex(it) },
        inTransformer = { it.toIndex() }
    )
    viewModel.onValidationSearchTermError()
        .toViewError(this, responseValidationSearchTerm)
    viewModel.onValidationModeDescription()
        .toViewText(this, validationModeDescription)

    // Validation search term
    responseValidationSearchTerm.attachLiveData(
        lifecycleOwner = this,
        data = viewModel.validationSearchTerm,
        pullInChanges = false
    )
    viewModel.onValidationSearchTermVisibility()
        .toViewVisibility(this, responseValidationSearchTerm)

    // Validation script
    scriptInputLayout.attach(
        codeData = viewModel.validationScript,
        errorData = viewModel.onValidationScriptError(),
        visibility = viewModel.onValidationScriptVisibility()
    )

    // Check interval
    checkIntervalLayout.attach(
        valueData = viewModel.checkIntervalValue,
        multiplierData = viewModel.checkIntervalUnit,
        errorData = viewModel.onCheckIntervalError()
    )

    // Done button
    doneBtn.setOnClickListener {
      viewModel.commit {
        setResult(RESULT_OK)
        finish()
        overridePendingTransition(R.anim.fade_out, R.anim.fade_out)
      }
    }
  }

  private fun setupUi(savedInstanceState: Bundle?) {
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

    val validationOptionsAdapter = ArrayAdapter(
        this,
        R.layout.list_item_spinner,
        resources.getStringArray(R.array.response_validation_options)
    )
    validationOptionsAdapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown)
    responseValidationMode.adapter = validationOptionsAdapter
  }

  override fun onBackPressed() = closeActivityWithReveal()
}
