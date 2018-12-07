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
class ZipTest {

  @Rule @JvmField val rule = InstantTaskExecutorRule()

  @Test fun test_withDistinct() {
    val data1 = MutableLiveData<String>()
    val data2 = MutableLiveData<Int>()
    val zipped = zip(data1, data2, true)
        .test()

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertValues(Pair("Hello", 24))

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertNoValues()
  }

  @Test fun test_noDistinct() {
    val data1 = MutableLiveData<String>()
    val data2 = MutableLiveData<Int>()
    val zipped = zip(data1, data2, false)
        .test()

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertValues(Pair("Hello", 24))

    data1.postValue("Hi")
    data2.postValue(24)
    zipped.assertValues(Pair("Hi", 24))
  }

  @Test fun test_noDistinct_resetAfterEmission() {
    val data1 = MutableLiveData<String>()
    val data2 = MutableLiveData<Int>()
    val zipped = zip(data1, data2, false, true)
        .test()

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertValues(Pair("Hello", 24))

    data1.postValue("Hi")
    data2.postValue(50)
    zipped.assertValues(Pair("Hi", 50))
  }

  @Test fun test_withDistinct_customZipper() {
    val data1 = MutableLiveData<String>()
    val data2 = MutableLiveData<Int>()
    val zipped = zip(data1, data2, true,
        zipper = { left, right ->
          "$left $right"
        }).test()

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertValues("Hello 24")

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertNoValues()
  }

  @Test fun test_noDistinct_customZipper() {
    val data1 = MutableLiveData<String>()
    val data2 = MutableLiveData<Int>()
    val zipped = zip(data1, data2, false,
        zipper = { left, right ->
          "$left $right"
        }).test()

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertValues("Hello 24")

    data1.postValue("Hi")
    data2.postValue(24)
    zipped.assertValues("Hi 24")
  }

  @Test fun test_noDistinct_customZipper_resetAfterEmission() {
    val data1 = MutableLiveData<String>()
    val data2 = MutableLiveData<Int>()
    val zipped = zip(data1, data2, false, true,
        zipper = { left, right ->
          "$left $right"
        }).test()

    data1.postValue("Hello")
    data2.postValue(24)
    zipped.assertValues("Hello 24")

    data1.postValue("Hi")
    data2.postValue(50)
    zipped.assertValues("Hi 50")
  }
}
