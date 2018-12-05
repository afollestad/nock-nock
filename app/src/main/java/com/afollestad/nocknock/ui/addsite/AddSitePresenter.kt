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

import androidx.annotation.CheckResult
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.AppDatabase
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.ValidationMode
import com.afollestad.nocknock.data.model.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.model.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.data.putSite
import com.afollestad.nocknock.engine.statuscheck.ValidationManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import javax.inject.Inject

/** @author Aidan Follestad (@afollestad) */
data class InputErrors(
  var name: Int? = null,
  var url: Int? = null,
  var checkInterval: Int? = null,
  var termSearch: Int? = null,
  var javaScript: Int? = null,
  var networkTimeout: Int? = null
) {
  @CheckResult fun any(): Boolean {
    return name != null || url != null || checkInterval != null ||
        termSearch != null || javaScript != null || networkTimeout != null
  }
}

/** @author Aidan Follestad (@afollestad) */
interface AddSitePresenter {

  fun takeView(view: AddSiteView)

  fun onUrlInputFocusChange(
    focused: Boolean,
    content: String
  )

  fun onValidationModeSelected(index: Int)

  fun commit(
    name: String,
    url: String,
    checkInterval: Long,
    validationMode: ValidationMode,
    validationArgs: String?,
    networkTimeout: Int
  )

  fun dropView()
}

/** @author Aidan Follestad (@afollestad) */
class RealAddSitePresenter @Inject constructor(
  private val database: AppDatabase,
  private val checkStatusManager: ValidationManager
) : AddSitePresenter {

  private var view: AddSiteView? = null

  override fun takeView(view: AddSiteView) {
    this.view = view
  }

  override fun onUrlInputFocusChange(
    focused: Boolean,
    content: String
  ) {
    if (content.isEmpty() || focused) {
      return
    }
    val url = HttpUrl.parse(content)
    if (url == null ||
        (url.scheme() != "http" &&
            url.scheme() != "https")
    ) {
      view?.showOrHideUrlSchemeWarning(true)
    } else {
      view?.showOrHideUrlSchemeWarning(false)
    }
  }

  override fun onValidationModeSelected(index: Int) = with(view!!) {
    showOrHideValidationSearchTerm(index == 1)
    showOrHideScriptInput(index == 2)
    setValidationModeDescription(
        when (index) {
          0 -> R.string.validation_mode_status_desc
          1 -> R.string.validation_mode_term_desc
          2 -> R.string.validation_mode_javascript_desc
          else -> throw IllegalStateException("Unknown validation mode position: $index")
        }
    )
  }

  override fun commit(
    name: String,
    url: String,
    checkInterval: Long,
    validationMode: ValidationMode,
    validationArgs: String?,
    networkTimeout: Int
  ) {
    val inputErrors = InputErrors()

    if (name.isEmpty()) {
      inputErrors.name = R.string.please_enter_name
    }
    if (url.isEmpty()) {
      inputErrors.url = R.string.please_enter_url
    } else if (HttpUrl.parse(url) == null) {
      inputErrors.url = R.string.please_enter_valid_url
    }
    if (checkInterval <= 0) {
      inputErrors.checkInterval = R.string.please_enter_check_interval
    }
    if (validationMode == TERM_SEARCH && validationArgs.isNullOrEmpty()) {
      inputErrors.termSearch = R.string.please_enter_search_term
    } else if (validationMode == JAVASCRIPT && validationArgs.isNullOrEmpty()) {
      inputErrors.javaScript = R.string.please_enter_javaScript
    }
    if (networkTimeout <= 0) {
      inputErrors.networkTimeout = R.string.please_enter_networkTimeout
    }

    if (inputErrors.any()) {
      view?.setInputErrors(inputErrors)
      return
    }

    val newSettings = SiteSettings(
        validationIntervalMs = checkInterval,
        validationMode = validationMode,
        validationArgs = validationArgs,
        networkTimeout = networkTimeout,
        disabled = false
    )
    val newModel = Site(
        id = 0,
        name = name,
        url = url,
        settings = newSettings,
        lastResult = null
    )

    with(view!!) {
      scopeWhileAttached(Main) {
        launch(coroutineContext) {
          setLoading()
          val storedModel = async(IO) {
            database.putSite(newModel)
          }.await()

          checkStatusManager.scheduleCheck(
              site = storedModel,
              rightNow = true,
              cancelPrevious = true
          )
          setDoneLoading()
          onSiteAdded()
        }
      }
    }
  }

  override fun dropView() {
    view = null
  }
}
