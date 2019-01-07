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
package com.afollestad.nocknock

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.HtmlCompat.fromHtml
import com.afollestad.materialdialogs.utils.MDUtil.resolveColor
import com.afollestad.nocknock.utilities.ui.toast

typealias ActivityLifeChange = (activity: Activity, resumed: Boolean) -> Unit

/** @author Aidan Follestad (@afollestad) */
fun Application.onActivityLifeChange(cb: ActivityLifeChange) {
  registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
    override fun onActivitySaveInstanceState(
      activity: Activity?,
      outState: Bundle?
    ) = Unit

    override fun onActivityPaused(activity: Activity) = cb(activity, false)

    override fun onActivityResumed(activity: Activity) = cb(activity, true)

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivityCreated(
      activity: Activity?,
      savedInstanceState: Bundle?
    ) = Unit
  })
}

fun String.toHtml() = fromHtml(this, FROM_HTML_MODE_LEGACY)

fun String.toUri() = Uri.parse(this)!!

fun Activity.viewUrl(url: String) {
  val customTabsIntent = CustomTabsIntent.Builder()
      .apply {
        setToolbarColor(resolveColor(this@viewUrl, attr = R.attr.colorPrimary))
      }
      .build()
  try {
    customTabsIntent.launchUrl(this, url.toUri())
  } catch (_: ActivityNotFoundException) {
    toast(R.string.install_web_browser)
  }
}

fun Activity.viewUrlWithApp(
  url: String,
  pkg: String
) {
  val intent = Intent(Intent.ACTION_VIEW).apply {
    data = url.toUri()
  }
  val resInfo = packageManager.queryIntentActivities(intent, 0)
  for (info in resInfo) {
    if (info.activityInfo.packageName.toLowerCase().contains(pkg) ||
        info.activityInfo.name.toLowerCase().contains(pkg)
    ) {
      startActivity(intent.apply {
        setPackage(info.activityInfo.packageName)
      })
      return
    }
  }
  viewUrl(url)
}
