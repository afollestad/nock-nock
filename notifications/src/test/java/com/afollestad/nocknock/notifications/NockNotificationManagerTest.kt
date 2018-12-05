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
package com.afollestad.nocknock.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.Bitmap
import com.afollestad.nocknock.data.legacy.ServerModel
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.notifications.Channel.CheckFailures
import com.afollestad.nocknock.utilities.providers.BitmapProvider
import com.afollestad.nocknock.utilities.providers.IntentProvider
import com.afollestad.nocknock.utilities.providers.NotificationChannelProvider
import com.afollestad.nocknock.utilities.providers.NotificationProvider
import com.afollestad.nocknock.utilities.providers.RealIntentProvider.Companion.BASE_NOTIFICATION_REQUEST_CODE
import com.afollestad.nocknock.utilities.providers.StringProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

class NockNotificationManagerTest {

  private val appIconRes = 1024
  private val somethingWentWrong = "something went wrong"

  private val stockManager = mock<NotificationManager>()
  private val appIcon = mock<Bitmap>()
  private val bitmapProvider = mock<BitmapProvider> {
    on { get(appIconRes) } doReturn appIcon
  }
  private val stringProvider = mock<StringProvider> {
    on { get(R.string.something_wrong) } doReturn somethingWentWrong
  }
  private val intentProvider = mock<IntentProvider>()
  private val channelProvider = mock<NotificationChannelProvider>()
  private val notificationProvider = mock<NotificationProvider>()

  private val manager = RealNockNotificationManager(
      appIconRes,
      stockManager,
      bitmapProvider,
      stringProvider,
      intentProvider,
      channelProvider,
      notificationProvider
  )

  @Before fun setup() {
    whenever(channelProvider.create(any(), any(), any(), any())).doAnswer { inv ->
      val id = inv.getArgument<String>(0)
      val title = inv.getArgument<String>(1)
      val description = inv.getArgument<String>(2)
      val important = inv.getArgument<Int>(3)
      return@doAnswer mock<NotificationChannel> {
        on { this.id } doReturn id
        on { this.name } doReturn title
        on { this.description } doReturn description
        on { this.importance } doReturn important
      }
    }
  }

  @Test fun createChannels() {
    whenever(stringProvider.get(any())).doReturn("")
    val createdChannel = mock<NotificationChannel> {
      on { this.id } doReturn CheckFailures.id
    }
    whenever(channelProvider.create(any(), any(), any(), any()))
        .doReturn(createdChannel)
    manager.createChannels()

    val captor = argumentCaptor<NotificationChannel>()
    verify(stockManager, times(1)).createNotificationChannel(captor.capture())

    val channel = captor.allValues.single()
    assertThat(channel.id).isEqualTo(CheckFailures.id)

    verifyNoMoreInteractions(stockManager)
  }

  @Test fun postStatusNotification_appIsOpen() {
    manager.setIsAppOpen(true)
    manager.postStatusNotification(fakeModel())

    verifyNoMoreInteractions(stockManager)
  }

  @Test fun postStatusNotification_appNotOpen() {
    manager.setIsAppOpen(false)
    val model = fakeModel()

    val pendingIntent = mock<PendingIntent>()
    whenever(intentProvider.getPendingIntentForViewSite(model))
        .doReturn(pendingIntent)

    val notification = mock<Notification>()
    whenever(
        notificationProvider.create(
            CheckFailures.id,
            model.name,
            somethingWentWrong,
            pendingIntent,
            R.drawable.ic_notification,
            appIcon
        )
    ).doReturn(notification)

    manager.postStatusNotification(model)

    verify(stockManager).notify(
        model.url,
        BASE_NOTIFICATION_REQUEST_CODE + model.id,
        notification
    )
    verifyNoMoreInteractions(stockManager)
  }

  @Test fun cancelStatusNotification() {
    val model = fakeModel()
    manager.cancelStatusNotification(model)
    verify(stockManager).cancel(BASE_NOTIFICATION_REQUEST_CODE + model.id)
    verifyNoMoreInteractions(stockManager)
  }

  @Test fun cancelStatusNotifications() {
    manager.cancelStatusNotifications()
    verify(stockManager).cancelAll()
    verifyNoMoreInteractions(stockManager)
  }

  private fun fakeModel() = ServerModel(
      id = 1,
      url = "https://hello.com",
      name = "Testing",
      validationMode = STATUS_CODE
  )
}
