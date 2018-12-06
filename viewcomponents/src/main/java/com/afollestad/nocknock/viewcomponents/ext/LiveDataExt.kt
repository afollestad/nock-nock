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
package com.afollestad.nocknock.viewcomponents.ext

import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.afollestad.nocknock.utilities.ext.onTextChanged

fun <X, Y> LiveData<X>.map(mapper: (X) -> Y) =
  Transformations.map(this, mapper)!!

fun <X, Y> LiveData<X>.switchMap(mapper: (X) -> LiveData<Y>) =
  Transformations.switchMap(this, mapper)!!

inline fun <reified T> EditText.attachLiveData(
  lifecycleOwner: LifecycleOwner,
  data: MutableLiveData<T>
) {
  // Out
  when {
    T::class == Int::class -> {
      onTextChanged { data.postValue(it.trim().toInt() as T) }
    }
    T::class == Long::class -> {
      onTextChanged { data.postValue(it.trim().toLong() as T) }
    }
    T::class == String::class -> {
      onTextChanged { data.postValue(it.trim() as T) }
    }
    else -> {
      throw IllegalArgumentException("Can't send EditText text changes into ${T::class}")
    }
  }
  // In
  data.observe(lifecycleOwner, Observer {
    when {
      T::class == Int::class -> setText(it as Int)
      T::class == String::class -> setText(it as String)
    }
  })
}

fun <T> Spinner.attachLiveData(
  lifecycleOwner: LifecycleOwner,
  data: MutableLiveData<T>,
  outTransformer: (Int) -> T,
  inTransformer: (T) -> Int
) {
  // Out
  onItemSelected { data.postValue(outTransformer(it)) }
  // In
  data.observe(lifecycleOwner, Observer {
    setSelection(inTransformer(it))
  })
}

fun LiveData<Int?>.toViewError(
  owner: LifecycleOwner,
  view: EditText
) = observe(owner, Observer { error ->
  view.error = if (error != null) {
    view.resources.getString(error)
  } else {
    null
  }
})

inline fun <reified T> LiveData<T>.toViewText(
  owner: LifecycleOwner,
  view: TextView
) = observe(owner, Observer {
  when {
    T::class == Int::class -> view.setText(it as Int)
    T::class == String::class -> view.text = it as String
    else -> throw IllegalStateException("Cannot set ${T::class} as view text.")
  }
})

fun LiveData<Boolean>.toViewVisibility(
  owner: LifecycleOwner,
  view: View
) = observe(owner, Observer { view.showOrHide(it) })

/** @author Aidan Follestad (@afollestad) */
class ZipLiveData<T, K>(
  source1: LiveData<T>,
  source2: LiveData<K>
) : MediatorLiveData<Pair<T, K>>() {

  private var data1: T? = null
  private var data2: K? = null

  init {
    super.addSource(source1) {
      data1 = it
      maybeNotify()
    }
    super.addSource(source2) {
      data2 = it
      maybeNotify()
    }
  }

  private fun maybeNotify() {
    if (data1 != null && data2 != null) {
      value = Pair(data1!!, data2!!)
    }
  }

  override fun <S : Any?> addSource(
    source: LiveData<S>,
    onChanged: Observer<in S>
  ) {
    throw UnsupportedOperationException()
  }

  override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
    throw UnsupportedOperationException()
  }
}

fun <T, K> zip(
  source1: LiveData<T>,
  source2: LiveData<K>
): MediatorLiveData<Pair<T, K>> {
  return ZipLiveData(source1, source2)
}
