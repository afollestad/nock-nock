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
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.model.ValidationMode
import com.afollestad.nocknock.ui.DarkModeSwitchActivity
import com.afollestad.nocknock.viewcomponents.ext.dimenFloat
import com.afollestad.nocknock.viewcomponents.ext.onScroll
import com.afollestad.nocknock.viewcomponents.livedata.attachLiveData
import com.afollestad.nocknock.viewcomponents.livedata.toViewError
import com.afollestad.nocknock.viewcomponents.livedata.toViewText
import com.afollestad.nocknock.viewcomponents.livedata.toViewVisibility
import kotlinx.android.synthetic.main.activity_addsite.checkIntervalLayout
import kotlinx.android.synthetic.main.activity_addsite.headersLayout
import kotlinx.android.synthetic.main.activity_addsite.inputName
import kotlinx.android.synthetic.main.activity_addsite.inputTags
import kotlinx.android.synthetic.main.activity_addsite.inputUrl
import kotlinx.android.synthetic.main.activity_addsite.loadingProgress
import kotlinx.android.synthetic.main.activity_addsite.responseTimeoutInput
import kotlinx.android.synthetic.main.activity_addsite.responseValidationMode
import kotlinx.android.synthetic.main.activity_addsite.responseValidationSearchTerm
import kotlinx.android.synthetic.main.activity_addsite.retryPolicyLayout
import kotlinx.android.synthetic.main.activity_addsite.scriptInputLayout
import kotlinx.android.synthetic.main.activity_addsite.scrollView
import kotlinx.android.synthetic.main.activity_addsite.textUrlWarning
import kotlinx.android.synthetic.main.activity_addsite.validationModeDescription
import kotlinx.android.synthetic.main.include_app_bar.toolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.include_app_bar.app_toolbar as appToolbar
import kotlinx.android.synthetic.main.include_app_bar.toolbar_title as toolbarTitle

/** @author Aidan Follestad (@afollestad) */
class AddSiteActivity : DarkModeSwitchActivity() {

  private val viewModel by viewModel<AddSiteViewModel>()

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_addsite)
    setupUi()

    lifecycle.addObserver(viewModel)

    // Loading
    loadingProgress.observe(this, viewModel.onIsLoading())

    // Name
    inputName.attachLiveData(this, viewModel.name)
    viewModel.onNameError()
        .toViewError(this, inputName)

    // Tags
    inputTags.attachLiveData(this, viewModel.tags)

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

    // Retry Policy
    retryPolicyLayout.attach(
        timesData = viewModel.retryPolicyTimes,
        minutesData = viewModel.retryPolicyMinutes
    )

    // Headers
    headersLayout.attach(viewModel.headers)
  }

  private fun setupUi() {
    toolbarTitle.setText(R.string.add_site)
    toolbar.run {
      inflateMenu(R.menu.menu_addsite)
      setOnMenuItemClickListener {
        if (it.itemId == R.id.commit) {
          viewModel.commit {
            setResult(RESULT_OK)
            finish()
          }
        }
        true
      }
      setNavigationIcon(R.drawable.ic_action_close)
      setNavigationOnClickListener { finish() }
    }

    val validationOptionsAdapter = ArrayAdapter(
        this,
        R.layout.list_item_spinner,
        resources.getStringArray(R.array.response_validation_options)
    )
    validationOptionsAdapter.setDropDownViewResource(R.layout.list_item_spinner_dropdown)
    responseValidationMode.adapter = validationOptionsAdapter

    scrollView.onScroll {
      appToolbar.elevation = if (it > appToolbar.measuredHeight / 2) {
        appToolbar.dimenFloat(R.dimen.default_elevation)
      } else {
        0f
      }
    }
  }
}
