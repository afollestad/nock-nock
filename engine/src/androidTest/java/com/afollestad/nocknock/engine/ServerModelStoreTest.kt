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
package com.afollestad.nocknock.engine

import android.app.Application
import android.support.test.runner.AndroidJUnit4
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.CHECKING
import com.afollestad.nocknock.data.ServerStatus.ERROR
import com.afollestad.nocknock.data.ValidationMode.JAVASCRIPT
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.ValidationMode.TERM_SEARCH
import com.afollestad.nocknock.engine.db.RealServerModelStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.InstrumentationRegistry.getTargetContext as context

@RunWith(AndroidJUnit4::class)
class ServerModelStoreTest {

  private lateinit var store: RealServerModelStore

  @Before fun setup() {
    store = RealServerModelStore(context().applicationContext as Application)
    store.db()
        .wipe()
  }

  @Test fun get() = runBlocking {
    // Put some fake data to retrieve
    store.put(fakeModel(1))
    val model2 = store.put(fakeModel(2))

    val model = store.get(2)
        .single()
    assertThat(model).isEqualTo(model2.copy(id = 2))
  }

  @Test fun getAll() = runBlocking {
    // Put some fake data to retrieve
    val model1 = store.put(fakeModel(1))
    val model2 = store.put(fakeModel(2))

    val models = store.get()
    assertThat(models.size).isEqualTo(2)
    assertThat(models[0]).isEqualTo(model1.copy(id = 1))
    assertThat(models[1]).isEqualTo(model2.copy(id = 2))
  }

  @Test fun update() = runBlocking {
    store.put(
        ServerModel(
            name = "Wakanda Forever",
            url = "https://www.wakanda.gov",
            status = ERROR,
            checkInterval = 5,
            lastCheck = 10,
            reason = "Body doesn't contain your term.",
            validationMode = TERM_SEARCH,
            validationContent = "Vibranium",
            disabled = false
        )
    )
    store.put(fakeModel(2))

    val originalModel1 = store.get(id = 1)
        .single()

    val defaultJs = "var responseObj = JSON.parse(response);\\nreturn responseObj.success === true;"
    val newModel1 = originalModel1.copy(
        name = "HYDRA",
        url = "https://www.hyrda.dict",
        status = CHECKING,
        checkInterval = 10,
        lastCheck = 20,
        reason = "Evaluation failed.",
        validationMode = JAVASCRIPT,
        validationContent = defaultJs,
        disabled = true
    )
    assertThat(store.update(newModel1)).isEqualTo(1)

    val newModels = store.get()
    assertThat(newModels.size).isEqualTo(2)
    assertThat(newModels.first()).isEqualTo(newModel1)
  }

  @Test fun delete() = runBlocking {
    // Put some fake data to delete
    val model1 = store.put(fakeModel(1))
    val model2 = store.put(fakeModel(2))

    assertThat(store.delete(model1)).isEqualTo(1)

    val newModels = store.get()
    assertThat(newModels.single()).isEqualTo(model2)
  }

  @Test fun deleteAll() = runBlocking {
    // Put some fake data to delete
    store.put(fakeModel(1))
    store.put(fakeModel(2))

    store.deleteAll()
    assertThat(store.get()).isEmpty()
  }

  private fun fakeModel(index: Int) = ServerModel(
      name = "Model $index",
      url = "https://hello.com/$index",
      validationMode = STATUS_CODE
  )
}
