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

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage

/** @author Aidan Follestad (@afollestad) */
class TestLiveData<T>(data: LiveData<T>) {

  private val receivedValues = mutableListOf<T>()
  private val observer = Observer<T> { receivedValues.add(it) }

  init {
    data.observeForever(observer)
  }

  fun assertNoValues() {
    assertWithMessage("Expected no values, but got: $receivedValues").that(receivedValues)
        .isEmpty()
  }

  fun assertValues(vararg assertValues: T) {
    val assertList = assertValues.toList()
    assertThat(receivedValues).isEqualTo(assertList)
    receivedValues.clear()
  }

  @CheckResult fun values(): List<T> = receivedValues
}

@CheckResult fun <T> LiveData<T>.test() = TestLiveData(this)
