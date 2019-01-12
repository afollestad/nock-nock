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

import android.app.job.JobInfo
import android.app.job.JobScheduler
import com.afollestad.nocknock.data.model.Header
import com.afollestad.nocknock.data.model.Status.ERROR
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.engine.ssl.SslManager
import com.afollestad.nocknock.engine.validation.RealValidationExecutor
import com.afollestad.nocknock.engine.validation.ValidationJob.Companion.KEY_SITE_ID
import com.afollestad.nocknock.utilities.providers.StringProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol.HTTP_2
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Test
import java.net.SocketTimeoutException

class ValidationExecutorTest {

  private val timeoutError = "Oh no, a timeout"

  private val jobScheduler = mock<JobScheduler>()
  private val okHttpClient = mock<OkHttpClient>()
  private val stringProvider = mock<StringProvider> {
    on { get(R.string.timeout) } doReturn timeoutError
  }
  private val bundleProvider = testBundleProvider()
  private val jobInfoProvider = testJobInfoProvider()
  private val database = mockDatabase()
  private val sslManager = mock<SslManager> {
    on { clientForCertificate(any(), any(), any()) } doAnswer { inv ->
      inv.getArgument<OkHttpClient>(2)
    }
  }

  private val manager = RealValidationExecutor(
      jobScheduler,
      okHttpClient,
      stringProvider,
      bundleProvider,
      jobInfoProvider,
      database,
      sslManager
  ).apply {
    setClientTimeoutChanger { _, timeout ->
      whenever(okHttpClient.callTimeoutMillis()).doReturn(timeout)
      return@setClientTimeoutChanger okHttpClient
    }
  }

  @Test fun ensureScheduledValidations_noEnabledSites() = runBlocking {
    val model1 = fakeModel(id = 1)
    model1.settings = model1.settings!!.copy(disabled = true)
    database.setAllSites(model1)

    manager.ensureScheduledValidations()

    verifyNoMoreInteractions(jobScheduler)
  }

  @Test fun ensureScheduledValidations_sitesAlreadyHaveJobs() = runBlocking<Unit> {
    val model1 = fakeModel(id = 1)
    val job1 = fakeJob(1)
    database.setAllSites(model1)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf(job1))

    manager.ensureScheduledValidations()

    verify(jobScheduler, never()).schedule(any())
  }

  @Test fun ensureScheduledValidations() = runBlocking {
    val model1 = fakeModel(id = 1)
    database.setAllSites(model1)

    whenever(jobScheduler.allPendingJobs).doReturn(listOf<JobInfo>())

    manager.ensureScheduledValidations()

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getLong(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test fun scheduleValidation_rightNow() {
    val model1 = fakeModel(id = 1)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf<JobInfo>())

    manager.scheduleValidation(
        site = model1,
        rightNow = true
    )

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    verify(jobScheduler).cancel(1)

    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getLong(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test(expected = IllegalStateException::class)
  fun scheduleValidation_notFromFinishingJob_haveExistingJob() {
    val model1 = fakeModel(id = 1)
    val job1 = fakeJob(1)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf(job1))

    manager.scheduleValidation(
        site = model1,
        fromFinishingJob = false
    )
  }

  @Test fun scheduleValidation_fromFinishingJob_haveExistingJob() {
    val model1 = fakeModel(id = 1)
    val job1 = fakeJob(1)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf(job1))

    manager.scheduleValidation(
        site = model1,
        fromFinishingJob = true
    )

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    verify(jobScheduler, never()).cancel(any())

    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getLong(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test fun scheduleValidation() {
    val model1 = fakeModel(id = 1)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf<JobInfo>())

    manager.scheduleValidation(
        site = model1,
        fromFinishingJob = true
    )

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    verify(jobScheduler, never()).cancel(any())

    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getLong(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test fun cancelScheduledValidation() {
    val model1 = fakeModel(id = 1)
    manager.cancelScheduledValidation(model1)
    verify(jobScheduler).cancel(1)
  }

  @Test fun performValidation_httpNotSuccess() = runBlocking {
    val response = fakeResponse(500, "Internal Server Error", "Hello World")
    val call = mock<Call> {
      on { execute() } doReturn response
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel(id = 1)
    val result = manager.performValidation(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            lastResult = model1.lastResult?.copy(
                status = ERROR,
                reason = "Response 500 - Hello World"
            )
        )
    )
  }

  @Test fun performValidation_socketTimeout() = runBlocking {
    val error = SocketTimeoutException("Oh no!")
    val call = mock<Call> {
      on { execute() } doAnswer { throw error }
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel(id = 1)
    val result = manager.performValidation(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            lastResult = model1.lastResult?.copy(
                status = ERROR,
                reason = timeoutError
            )
        )
    )
  }

  @Test fun performValidation_exception() = runBlocking {
    val error = Exception("Oh no!")
    val call = mock<Call> {
      on { execute() } doAnswer { throw error }
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel(id = 1)
    val result = manager.performValidation(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            lastResult = model1.lastResult?.copy(
                status = ERROR,
                reason = "Oh no!"
            )
        )
    )
  }

  @Test fun performValidation_success_withHeaders() = runBlocking {
    val requestCaptor = argumentCaptor<Request>()
    val response = fakeResponse(200, "OK", "Hello World")

    val call = mock<Call> {
      on { execute() } doReturn response
    }
    whenever(okHttpClient.newCall(requestCaptor.capture()))
        .doReturn(call)

    val model1 = fakeModel(id = 1).copy(
        headers = listOf(
            Header(
                key = "X-Test-Header",
                value = "Hello, World!"
            )
        )
    )
    val result = manager.performValidation(model1)
    val httpRequest = requestCaptor.firstValue

    assertThat(result.model).isEqualTo(
        model1.copy(
            lastResult = model1.lastResult?.copy(
                status = OK,
                reason = null
            )
        )
    )
    assertThat(okHttpClient.callTimeoutMillis())
        .isEqualTo(model1.settings!!.networkTimeout)
    assertThat(httpRequest.header("X-Test-Header"))
        .isEqualTo("Hello, World!")
  }

  @Test fun performValidation_success_withCustomSslCert() = runBlocking<Unit> {
    val response = fakeResponse(200, "OK", "Hello World")
    val call = mock<Call> {
      on { execute() } doReturn response
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel(id = 1).copy(
        url = "http://wwww.mysite.com/test.html",
        headers = emptyList()
    )
    model1.settings = model1.settings!!.copy(
        certificate = "file:///sdcard/cert.pem"
    )
    val result = manager.performValidation(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            lastResult = model1.lastResult?.copy(
                status = OK,
                reason = null
            )
        )
    )
    assertThat(okHttpClient.callTimeoutMillis())
        .isEqualTo(model1.settings!!.networkTimeout)

    verify(sslManager).clientForCertificate(
        "file:///sdcard/cert.pem",
        "http://wwww.mysite.com/test.html",
        okHttpClient
    )
  }

  private fun fakeResponse(
    code: Int,
    message: String,
    body: String?
  ): Response {
    val responseBody = if (body != null) {
      ResponseBody.create(null, body)
    } else {
      null
    }
    val request = Request.Builder()
        .url("https://placeholder.com")
        .build()
    return Response.Builder()
        .protocol(HTTP_2)
        .request(request)
        .message(message)
        .code(code)
        .body(responseBody)
        .build()
  }

  private fun fakeJob(id: Int): JobInfo {
    return mock {
      on { this.id } doReturn id
    }
  }
}
