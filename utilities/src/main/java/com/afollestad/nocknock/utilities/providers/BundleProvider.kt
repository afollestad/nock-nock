/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.providers

import android.os.PersistableBundle
import javax.inject.Inject

interface IBundle {
  fun putInt(
    key: String,
    value: Int
  )
}

typealias IBundler = IBundle.() -> Unit

/** @author Aidan Follestad (@afollestad) */
interface BundleProvider {

  fun createPersistable(builder: IBundle.() -> Unit): PersistableBundle
}

/** @author Aidan Follestad (@afollestad) */
class RealBundleProvider @Inject constructor() : BundleProvider {

  override fun createPersistable(bundler: IBundler): PersistableBundle {
    val realBundle = PersistableBundle()
    bundler(object : IBundle {
      override fun putInt(
        key: String,
        value: Int
      ) = realBundle.putInt(key, value)
    })
    return realBundle
  }
}
