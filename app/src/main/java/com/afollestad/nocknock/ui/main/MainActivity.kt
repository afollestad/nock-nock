/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.ui.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.HtmlCompat.fromHtml
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.nocknock.R
import com.afollestad.nocknock.adapter.ServerAdapter
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.dialogs.AboutDialog
import com.afollestad.nocknock.engine.statuscheck.CheckStatusJob.Companion.ACTION_STATUS_UPDATE
import com.afollestad.nocknock.ui.addsite.intentToAdd
import com.afollestad.nocknock.ui.viewsite.intentToView
import com.afollestad.nocknock.utilities.ext.ScopeReceiver
import com.afollestad.nocknock.utilities.ext.injector
import com.afollestad.nocknock.utilities.ext.safeRegisterReceiver
import com.afollestad.nocknock.utilities.ext.safeUnregisterReceiver
import com.afollestad.nocknock.utilities.ext.scopeWhileAttached
import com.afollestad.nocknock.viewcomponents.ext.showOrHide
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.activity_main.rootView
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.include_empty_view.emptyText
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/** @author Aidan Follestad (@afollestad) */
class MainActivity : AppCompatActivity(), MainView {

  companion object {
    private const val ADD_SITE_RQ = 6969
    private const val VIEW_SITE_RQ = 6923
  }

  private val intentReceiver = object : BroadcastReceiver() {
    override fun onReceive(
      context: Context,
      intent: Intent
    ) = presenter.onBroadcast(intent)
  }

  @Inject lateinit var presenter: MainPresenter

  private lateinit var adapter: ServerAdapter

  @SuppressLint("CommitPrefEdits")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    injector().injectInto(this)
    setContentView(R.layout.activity_main)
    presenter.takeView(this)

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

    fab.setOnClickListener {
      startActivityForResult(
          intentToAdd(fab.x, fab.y, fab.measuredWidth),
          ADD_SITE_RQ
      )
    }
  }

  override fun onResume() {
    super.onResume()
    val filter = IntentFilter().apply {
      addAction(ACTION_STATUS_UPDATE)
    }
    safeRegisterReceiver(intentReceiver, filter)
    presenter.resume()
  }

  override fun onPause() {
    super.onPause()
    safeUnregisterReceiver(intentReceiver)
  }

  override fun onDestroy() {
    presenter.dropView()
    super.onDestroy()
  }

  override fun setModels(models: List<ServerModel>) {
    list.post {
      adapter.set(models)
      emptyText.showOrHide(models.isEmpty())
    }
  }

  override fun updateModel(model: ServerModel) {
    list.post { adapter.update(model) }
  }

  override fun onSiteDeleted(model: ServerModel) {
    list.post {
      adapter.remove(model)
      emptyText.showOrHide(adapter.itemCount == 0)
    }
  }

  override fun scopeWhileAttached(
    context: CoroutineContext,
    exec: ScopeReceiver
  ) = rootView.scopeWhileAttached(context, exec)

  private fun onSiteSelected(
    model: ServerModel,
    longClick: Boolean
  ) {
    if (longClick) {
      MaterialDialog(this).show {
        title(R.string.options)
        listItems(R.array.site_long_options) { _, i, _ ->
          when (i) {
            0 -> presenter.refreshSite(model)
            1 -> maybeRemoveSite(model)
          }
        }
      }
      return
    }

    startActivityForResult(intentToView(model), VIEW_SITE_RQ)
  }

  private fun maybeRemoveSite(model: ServerModel) {
    MaterialDialog(this).show {
      title(R.string.remove_site)
      message(
          text = fromHtml(
              context.getString(R.string.remove_site_prompt, model.name),
              FROM_HTML_MODE_LEGACY
          )
      )
      positiveButton(R.string.remove) {
        presenter.removeSite(model)
      }
      negativeButton(android.R.string.cancel)
    }
  }
}
