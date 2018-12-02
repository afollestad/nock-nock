/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine

import android.app.job.JobInfo
import android.app.job.JobScheduler
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerStatus.ERROR
import com.afollestad.nocknock.data.ServerStatus.OK
import com.afollestad.nocknock.data.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.engine.db.ServerModelStore
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.KEY_SITE_ID
import com.afollestad.nocknock.engine.statuscheck.RealCheckStatusManager
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

class CheckStatusManagerTest {

  private val timeoutError = "Oh no, a timeout"

  private val jobScheduler = mock<JobScheduler>()
  private val okHttpClient = mock<OkHttpClient>()
  private val stringProvider = mock<StringProvider> {
    on { get(R.string.timeout) } doReturn timeoutError
  }
  private val bundleProvider = testBundleProvider()
  private val jobInfoProvider = testJobInfoProvider()
  private val store = mock<ServerModelStore>()

  private val manager = RealCheckStatusManager(
      jobScheduler,
      okHttpClient,
      stringProvider,
      bundleProvider,
      jobInfoProvider,
      store
  )

  @Test fun ensureScheduledChecks_noEnabledSites() = runBlocking {
    val model1 = fakeModel().copy(disabled = true)
    whenever(store.get()).doReturn(listOf(model1))

    manager.ensureScheduledChecks()

    verifyNoMoreInteractions(jobScheduler)
  }

  @Test fun ensureScheduledChecks_sitesAlreadyHaveJobs() = runBlocking<Unit> {
    val model1 = fakeModel()
    val job1 = fakeJob(model1.id)
    whenever(store.get()).doReturn(listOf(model1))
    whenever(jobScheduler.allPendingJobs).doReturn(listOf(job1))

    manager.ensureScheduledChecks()

    verify(jobScheduler, never()).schedule(any())
  }

  @Test fun ensureScheduledChecks() = runBlocking {
    val model1 = fakeModel()
    whenever(store.get()).doReturn(listOf(model1))
    whenever(jobScheduler.allPendingJobs).doReturn(listOf<JobInfo>())

    manager.ensureScheduledChecks()

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getInt(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test fun scheduleCheck_rightNow() {
    val model1 = fakeModel()
    whenever(jobScheduler.allPendingJobs).doReturn(listOf<JobInfo>())

    manager.scheduleCheck(
        site = model1,
        rightNow = true
    )

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    verify(jobScheduler).cancel(model1.id)

    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getInt(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test(expected = IllegalStateException::class)
  fun scheduleCheck_notFromFinishingJob_haveExistingJob() {
    val model1 = fakeModel()
    val job1 = fakeJob(model1.id)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf(job1))

    manager.scheduleCheck(
        site = model1,
        fromFinishingJob = false
    )
  }

  @Test fun scheduleCheck_fromFinishingJob_haveExistingJob() {
    val model1 = fakeModel()
    val job1 = fakeJob(model1.id)
    whenever(jobScheduler.allPendingJobs).doReturn(listOf(job1))

    manager.scheduleCheck(
        site = model1,
        fromFinishingJob = true
    )

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    verify(jobScheduler, never()).cancel(model1.id)

    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getInt(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test fun scheduleCheck() {
    val model1 = fakeModel()
    whenever(jobScheduler.allPendingJobs).doReturn(listOf<JobInfo>())

    manager.scheduleCheck(
        site = model1,
        fromFinishingJob = true
    )

    val jobCaptor = argumentCaptor<JobInfo>()
    verify(jobScheduler).schedule(jobCaptor.capture())
    verify(jobScheduler, never()).cancel(model1.id)

    val jobInfo = jobCaptor.allValues.single()
    assertThat(jobInfo.id).isEqualTo(model1.id)
    assertThat(jobInfo.extras.getInt(KEY_SITE_ID)).isEqualTo(model1.id)
  }

  @Test fun cancelCheck() {
    val model1 = fakeModel()
    manager.cancelCheck(model1)
    verify(jobScheduler).cancel(model1.id)
  }

  @Test fun performCheck_httpNotSuccess() = runBlocking {
    val response = fakeResponse(500, "Internal Server Error", "Hello World")
    val call = mock<Call> {
      on { execute() } doReturn response
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel()
    val result = manager.performCheck(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            status = ERROR,
            reason = "Response 500 - Hello World"
        )
    )
  }

  @Test fun performCheck_socketTimeout() = runBlocking {
    val error = SocketTimeoutException("Oh no!")
    val call = mock<Call> {
      on { execute() } doAnswer { throw error }
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel()
    val result = manager.performCheck(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            status = ERROR,
            reason = timeoutError
        )
    )
  }

  @Test fun performCheck_exception() = runBlocking {
    val error = Exception("Oh no!")
    val call = mock<Call> {
      on { execute() } doAnswer { throw error }
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel()
    val result = manager.performCheck(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            status = ERROR,
            reason = "Oh no!"
        )
    )
  }

  @Test fun performCheck_success() = runBlocking {
    val response = fakeResponse(200, "OK", "Hello World")
    val call = mock<Call> {
      on { execute() } doReturn response
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel()
    val result = manager.performCheck(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            status = OK,
            reason = null
        )
    )
  }

  @Test fun performCheck_401_butStillSuccess() = runBlocking {
    val response = fakeResponse(401, "Unauthorized", "Hello World")
    val call = mock<Call> {
      on { execute() } doReturn response
    }
    whenever(okHttpClient.newCall(any())).doReturn(call)

    val model1 = fakeModel()
    val result = manager.performCheck(model1)

    assertThat(result.model).isEqualTo(
        model1.copy(
            status = OK,
            reason = null
        )
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

  private fun fakeModel() = ServerModel(
      id = 1,
      name = "Wakanda Forever",
      url = "https://www.wakanda.gov",
      validationMode = STATUS_CODE
  )

  private fun fakeJob(id: Int): JobInfo {
    return mock {
      on { this.id } doReturn id
    }
  }
}
