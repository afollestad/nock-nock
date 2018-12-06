package com.afollestad.nocknock.broadcasts

import android.app.Application
import android.content.IntentFilter
import com.afollestad.nocknock.MOCK_MODEL_2
import com.afollestad.nocknock.engine.validation.ValidationJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.engine.validation.ValidationJob.Companion.KEY_UPDATE_MODEL
import com.afollestad.nocknock.fakeIntent
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

/** @author Aidan Follestad (@afollestad) */
class StatusUpdateIntentReceiverTest {

  private val app = mock<Application>()
  private val callback = mock<SiteCallback>()

  private val receiver = StatusUpdateIntentReceiver(app, callback)

  @Test fun onReceive() {
    val badIntent = fakeIntent("Hello World")
    receiver.intentReceiver.onReceive(app, badIntent)

    val goodIntent = fakeIntent(ACTION_STATUS_UPDATE)
    whenever(goodIntent.getSerializableExtra(KEY_UPDATE_MODEL))
        .doReturn(MOCK_MODEL_2)

    receiver.intentReceiver.onReceive(app, goodIntent)
    verify(callback, times(1)).invoke(MOCK_MODEL_2)
  }

  @Test fun onResume() {
    receiver.onResume()

    val filterCaptor = argumentCaptor<IntentFilter>()
    verify(app).registerReceiver(receiver.intentReceiver, filterCaptor.capture())

    val actionIterator = filterCaptor.firstValue.actionsIterator()
    assertThat(actionIterator.hasNext()).isTrue()
    val filterAction = actionIterator.next()
    assertThat(filterAction).isEqualTo(ACTION_STATUS_UPDATE)
    assertThat(actionIterator.hasNext()).isFalse()
  }

  @Test fun onPause() {
    receiver.onPause()
    verify(app).unregisterReceiver(receiver.intentReceiver)
  }
}