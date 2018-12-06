package com.afollestad.nocknock

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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
    assertWithMessage("Expected: $assertList, but got: $receivedValues").that(receivedValues)
        .isEqualTo(assertList)
    receivedValues.clear()
  }

  @CheckResult fun values(): List<T> = receivedValues
}

@CheckResult fun <T> LiveData<T>.test() = TestLiveData(this)