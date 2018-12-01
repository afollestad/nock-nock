/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

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
