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
import android.os.Bundle
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.HtmlCompat.fromHtml

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
