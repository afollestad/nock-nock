/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine

import android.app.job.JobInfo
import android.content.ComponentName
import android.os.PersistableBundle
import com.afollestad.nocknock.utilities.providers.BundleProvider
import com.afollestad.nocknock.utilities.providers.IBundle
import com.afollestad.nocknock.utilities.providers.IBundler
import com.afollestad.nocknock.utilities.providers.JobInfoProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

fun testBundleProvider(): BundleProvider {
  val provider = mock<BundleProvider>()
  whenever(provider.createPersistable(any())).doAnswer {
    val realBundle = mock<PersistableBundle>()
    val creator = it.getArgument<IBundler>(0)
    creator(object : IBundle {
      override fun putInt(
        key: String,
        value: Int
      ) {
        whenever(realBundle.getInt(key)).doReturn(value)
      }
    })
    return@doAnswer realBundle
  }
  return provider
}

fun testJobInfoProvider(): JobInfoProvider {
  val provider = mock<JobInfoProvider>()
  whenever(provider.createCheckJob(any(), any(), any(), any(), any())).doAnswer { inv ->
    val jobInfo = mock<JobInfo>()
    val id = inv.getArgument<Int>(0)
    val delay = inv.getArgument<Long>(2)
    val extras = inv.getArgument<PersistableBundle>(3)
    val target = inv.getArgument<Class<*>>(4)
    val component = mock<ComponentName>()
    whenever(component.className).doReturn(target.name)

    whenever(jobInfo.id).doReturn(id)
    whenever(jobInfo.minLatencyMillis).doReturn(delay)
    whenever(jobInfo.extras).doReturn(extras)
    whenever(jobInfo.service).doReturn(component)

    return@doAnswer jobInfo
  }
  return provider
}
