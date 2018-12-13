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
package com.afollestad.nocknock.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.nocknock.R
import com.afollestad.nocknock.adapter.ServerAdapter
import com.afollestad.nocknock.broadcasts.StatusUpdateIntentReceiver
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.dialogs.AboutDialog
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.utilities.providers.IntentProvider
import com.afollestad.nocknock.viewcomponents.ext.showOrHide
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.activity_main.loadingProgress
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.include_empty_view.emptyText
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/** @author Aidan Follestad (@afollestad) */
class MainActivity : AppCompatActivity() {

  private val notificationManager by inject<NockNotificationManager>()
  private val intentProvider by inject<IntentProvider>()

  internal val viewModel by viewModel<MainViewModel>()

  private lateinit var adapter: ServerAdapter

  private val statusUpdateReceiver by lazy {
    StatusUpdateIntentReceiver(application, intentProvider) {
      viewModel.postSiteUpdate(it)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupUi()

    notificationManager.createChannels()

    lifecycle.run {
      addObserver(viewModel)
      addObserver(statusUpdateReceiver)
    }

    viewModel.onSites()
        .observe(this, Observer {
          adapter.set(it)
          emptyText.showOrHide(it.isEmpty())
        })
    loadingProgress.observe(this, viewModel.onIsLoading())

    processIntent(intent)
  }

  private fun setupUi() {
    toolbar.inflateMenu(R.menu.menu_main)
    toolbar.setOnMenuItemClickListener { item ->
      if (item.itemId == R.id.about) {
        AboutDialog.show(this)
      }
      return@setOnMenuItemClickListener true
    }

    adapter = ServerAdapter(this::onSiteSelected)

    list.layoutManager = LinearLayoutManager(this)
    list.adapter = adapter
    list.addItemDecoration(DividerItemDecoration(this, VERTICAL))

    fab.setOnClickListener { addSite() }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.let(::processIntent)
  }

  private fun onSiteSelected(
    model: Site,
    longClick: Boolean
  ) {
    if (longClick) {
      MaterialDialog(this).show {
        title(R.string.options)
        listItems(R.array.site_long_options) { _, i, _ ->
          when (i) {
            0 -> viewModel.refreshSite(model)
            1 -> maybeRemoveSite(model)
          }
        }
      }
    } else {
      viewSite(model)
    }
  }
}
