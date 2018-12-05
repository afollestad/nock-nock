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
package com.afollestad.nocknock.utilities.providers

import android.os.PersistableBundle
import javax.inject.Inject

interface IBundle {
  fun putLong(
    key: String,
    value: Long
  )
}

typealias IBundler = IBundle.() -> Unit

/** @author Aidan Follestad (@afollestad) */
interface BundleProvider {

  fun createPersistable(bundler: IBundle.() -> Unit): PersistableBundle
}

/** @author Aidan Follestad (@afollestad) */
class RealBundleProvider @Inject constructor() : BundleProvider {

  override fun createPersistable(bundler: IBundler): PersistableBundle {
    val realBundle = PersistableBundle()
    bundler(object : IBundle {
      override fun putLong(
        key: String,
        value: Long
      ) = realBundle.putLong(key, value)
    })
    return realBundle
  }
}
