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
package com.afollestad.nocknock.utilities.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.junit.Rule
import org.junit.Test

/** @author Aidan Follestad (@afollestad) */
class DistinctTest {

  @Rule @JvmField val rule = InstantTaskExecutorRule()

  @Test fun filterLastValues() {
    val data = MutableLiveData<String>()
    val distinct = data.distinct()
        .test()

    data.postValue("Hello")
    data.postValue("Hello")

    data.postValue("Hi")
    data.postValue("Hi")

    data.postValue("Hello")

    distinct.assertValues(
        "Hello",
        "Hi",
        "Hello"
    )
  }
}
