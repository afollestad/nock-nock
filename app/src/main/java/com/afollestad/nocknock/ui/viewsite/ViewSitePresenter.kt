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
package com.afollestad.nocknock.ui.viewsite

import android.content.Intent
import androidx.annotation.CheckResult
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.WAITING
import com.afollestad.nocknock.data.ValidationMode
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.engine.statuscheck.CheckStatusManager
import com.afollestad.nocknock.notifications.NockNotificationManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import org.jetbrains.annotations.TestOnly
import javax.inject.Inject

const val KEY_VIEW_MODEL = "site_model"

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
interface ViewSitePresenter {

  fun takeView(
    view: ViewSiteView,
    intent: Intent
  )

  fun onBroadcast(intent: Intent)

  fun onNewIntent(intent: Intent?)

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
    validationContent: String?,
    networkTimeout: Int
  )

  fun checkNow()

  fun disableChecks()

  fun removeSite()

  fun currentModel(): ServerModel

  fun dropView()
}

/** @author Aidan Follestad (@afollestad) */
class RealViewSitePresenter @Inject constructor(
  private val serverModelStore: ServerModelStore,
  private val checkStatusManager: CheckStatusManager,
  private val notificationManager: NockNotificationManager
) : ViewSitePresenter {

  private var view: ViewSiteView? = null
  private var currentModel: ServerModel? = null

  override fun takeView(
    view: ViewSiteView,
    intent: Intent
  ) {
    this.currentModel = intent.getSerializableExtra(KEY_VIEW_MODEL) as ServerModel
    this.view = view.apply {
      displayModel(currentModel!!)
    }
  }

  override fun onBroadcast(intent: Intent) {
    if (intent.action == ACTION_STATUS_UPDATE) {
      val model = intent.getSerializableExtra(KEY_UPDATE_MODEL) as? ServerModel ?: return
      this.currentModel = model
      view?.displayModel(model)
    }
  }

  override fun onNewIntent(intent: Intent?) {
    if (intent != null && intent.hasExtra(KEY_VIEW_MODEL)) {
      currentModel = intent.getSerializableExtra(KEY_VIEW_MODEL) as ServerModel
      view?.displayModel(currentModel!!)
    }
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
    validationContent: String?,
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
    if (validationMode == TERM_SEARCH && validationContent.isNullOrEmpty()) {
      inputErrors.termSearch = R.string.please_enter_search_term
    } else if (validationMode == JAVASCRIPT && validationContent.isNullOrEmpty()) {
      inputErrors.javaScript = R.string.please_enter_javaScript
    }
    if (networkTimeout <= 0) {
      inputErrors.networkTimeout = R.string.please_enter_networkTimeout
    }

    if (inputErrors.any()) {
      view?.setInputErrors(inputErrors)
      return
    }

    val newModel = currentModel!!.copy(
        name = name,
        url = url,
        status = WAITING,
        checkInterval = checkInterval,
        validationMode = validationMode,
        validationContent = validationContent,
        disabled = false,
        networkTimeout = networkTimeout
    )

    with(view!!) {
      scopeWhileAttached(Main) {
        launch(coroutineContext) {
          setLoading()
          async(IO) { serverModelStore.update(newModel) }.await()
          checkStatusManager.scheduleCheck(
              site = newModel,
              rightNow = true,
              cancelPrevious = true
          )
          setDoneLoading()
          view?.finish()
        }
      }
    }
  }

  override fun checkNow() = with(view!!) {
    val checkModel = currentModel!!.copy(
        status = WAITING
    )
    view?.displayModel(checkModel)
    checkStatusManager.scheduleCheck(
        site = checkModel,
        rightNow = true,
        cancelPrevious = true
    )
  }

  override fun disableChecks() {
    val site = currentModel!!
    checkStatusManager.cancelCheck(site)
    notificationManager.cancelStatusNotification(site)

    with(view!!) {
      scopeWhileAttached(Main) {
        launch(coroutineContext) {
          setLoading()
          currentModel = currentModel!!.copy(disabled = true)
          async(IO) { serverModelStore.update(currentModel!!) }.await()
          setDoneLoading()
          view?.displayModel(currentModel!!)
        }
      }
    }
  }

  override fun removeSite() {
    val site = currentModel!!
    checkStatusManager.cancelCheck(site)
    notificationManager.cancelStatusNotification(site)

    with(view!!) {
      scopeWhileAttached(Main) {
        launch(coroutineContext) {
          setLoading()
          async(IO) { serverModelStore.delete(site) }.await()
          setDoneLoading()
          view?.finish()
        }
      }
    }
  }

  override fun currentModel() = this.currentModel!!

  override fun dropView() {
    view = null
    currentModel = null
  }

  @TestOnly fun setModel(model: ServerModel) {
    this.currentModel = model
  }
}
