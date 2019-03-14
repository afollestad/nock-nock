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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.afollestad.nocknock.R
import com.afollestad.nocknock.adapter.SiteAdapter
import com.afollestad.nocknock.adapter.TagAdapter
import com.afollestad.nocknock.broadcasts.StatusUpdateIntentReceiver
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.dialogs.AboutDialog
import com.afollestad.nocknock.notifications.NockNotificationManager
import com.afollestad.nocknock.ui.DarkModeSwitchActivity
import com.afollestad.nocknock.ui.NightMode.UNKNOWN
import com.afollestad.nocknock.utilities.providers.IntentProvider
import com.afollestad.nocknock.utilities.ui.toast
import com.afollestad.nocknock.viewUrl
import com.afollestad.nocknock.viewUrlWithApp
import com.afollestad.nocknock.viewcomponents.livedata.toViewVisibility
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.activity_main.loadingProgress
import kotlinx.android.synthetic.main.include_app_bar.toolbar
import kotlinx.android.synthetic.main.include_empty_view.emptyText
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.activity_main.tags_list as tagsList

/** @author Aidan Follestad (@afollestad) */
class MainActivity : DarkModeSwitchActivity() {

  private val notificationManager by inject<NockNotificationManager>()
  private val intentProvider by inject<IntentProvider>()

  internal val viewModel by viewModel<MainViewModel>()

  private lateinit var siteAdapter: SiteAdapter
  private lateinit var tagAdapter: TagAdapter

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
        .observe(this, Observer { siteAdapter.set(it) })
    viewModel.onEmptyTextVisibility()
        .toViewVisibility(this, emptyText)
    viewModel.onTags()
        .observe(this, Observer { tagAdapter.set(it) })
    viewModel.onTagsListVisibility()
        .toViewVisibility(this, tagsList)
    loadingProgress.observe(this, viewModel.onIsLoading())

    processIntent(intent)
  }

  private fun setupUi() {
    toolbar.run {
      inflateMenu(R.menu.menu_main)
      menu.findItem(R.id.dark_mode)
          .apply {
            if (getCurrentNightMode() == UNKNOWN) {
              isChecked = isDarkMode()
            } else {
              isVisible = false
            }
          }
      setOnMenuItemClickListener { item ->
        when (item.itemId) {
          R.id.about -> AboutDialog.show(this@MainActivity)
          R.id.dark_mode -> toggleDarkMode()
          R.id.support_me -> supportMe()
        }
        return@setOnMenuItemClickListener true
      }
    }

    siteAdapter = SiteAdapter(this::onSiteSelected)
    list.run {
      layoutManager = LinearLayoutManager(this@MainActivity)
      adapter = siteAdapter
      addItemDecoration(DividerItemDecoration(this@MainActivity, VERTICAL))
    }

    tagAdapter = TagAdapter(viewModel::onTagSelection)
    tagsList.run {
      layoutManager = LinearLayoutManager(this@MainActivity, HORIZONTAL, false)
      adapter = tagAdapter
    }

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
            1 -> addSiteForDuplication(model)
            2 -> maybeRemoveSite(model)
          }
        }
      }
    } else {
      viewSite(model)
    }
  }

  private fun supportMe() {
    MaterialDialog(this).show {
      title(R.string.support_me)
      message(R.string.support_me_message, html = true, lineHeightMultiplier = 1.4f)
      listItemsSingleChoice(R.array.donation_options) { _, index, _ ->
        when (index) {
          0 -> viewUrl("https://paypal.me/AidanFollestad")
          1 -> viewUrlWithApp("https://cash.me/\$afollestad", pkg = "com.squareup.cash")
          2 -> viewUrlWithApp("https://venmo.com/afollestad", pkg = "com.venmo")
        }
        toast(R.string.thank_you)
      }
      positiveButton(R.string.next)
    }
  }
}
