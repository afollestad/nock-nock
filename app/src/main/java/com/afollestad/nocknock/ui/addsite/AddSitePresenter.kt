/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.addsite

import androidx.annotation.CheckResult
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.WAITING
import com.afollestad.nocknock.data.ValidationMode
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
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
  var javaScript: Int? = null
) {
  @CheckResult fun any(): Boolean {
    return name != null || url != null || checkInterval != null ||
        termSearch != null || javaScript != null
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
    validationContent: String?
  )

  fun dropView()
}

/** @author Aidan Follestad (@afollestad) */
class RealAddSitePresenter @Inject constructor(
  private val serverModelStore: ServerModelStore,
  private val checkStatusManager: CheckStatusManager
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
    validationContent: String?
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
    if (validationMode == TERM_SEARCH && validationContent.isNullOrEmpty()) {
      inputErrors.termSearch = R.string.please_enter_search_term
    } else if (validationMode == JAVASCRIPT && validationContent.isNullOrEmpty()) {
      inputErrors.javaScript = R.string.please_enter_javaScript
    }

    if (inputErrors.any()) {
      view?.setInputErrors(inputErrors)
      return
    }

    val newModel = ServerModel(
        name = name,
        url = url,
        status = WAITING,
        checkInterval = checkInterval,
        validationMode = validationMode,
        validationContent = validationContent
    )

    with(view!!) {
      scopeWhileAttached(Main) {
        launch(coroutineContext) {
          setLoading()
          val storedModel = async(IO) {
            serverModelStore.put(newModel)
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
