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
package com.afollestad.nocknock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.Status.WAITING
import com.afollestad.nocknock.data.model.isPending
import com.afollestad.nocknock.data.model.textRes
import com.afollestad.nocknock.utilities.ui.onDebouncedClick
import kotlinx.android.synthetic.main.list_item_server.view.iconStatus
import kotlinx.android.synthetic.main.list_item_server.view.textInterval
import kotlinx.android.synthetic.main.list_item_server.view.textName
import kotlinx.android.synthetic.main.list_item_server.view.textStatus
import kotlinx.android.synthetic.main.list_item_server.view.textUrl

typealias Listener = (model: Site, longClick: Boolean) -> Unit

/** @author Aidan Follestad (@afollestad) */
class SiteViewHolder constructor(
  itemView: View,
  private val adapter: SiteAdapter
) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {

  init {
    itemView.onDebouncedClick {
      adapter.performClick(adapterPosition, false)
    }
    itemView.setOnLongClickListener(this)
  }

  fun bind(model: Site) {
    requireNotNull(model.settings) { "Settings must be populated." }

    itemView.textName.text = model.name
    itemView.textUrl.text = model.url

    val lastResult = model.lastResult
    if (lastResult != null) {
      itemView.iconStatus.setStatus(lastResult.status)
      val statusText = lastResult.status.textRes()
      if (statusText == 0) {
        itemView.textStatus.text = lastResult.reason
      } else {
        itemView.textStatus.setText(statusText)
      }
    } else {
      itemView.iconStatus.setStatus(WAITING)
      itemView.textStatus.setText(R.string.none)
    }

    val res = itemView.resources
    when {
      model.settings?.disabled == true -> {
        itemView.textInterval.setText(R.string.checks_disabled)
      }
      model.lastResult?.status.isPending() -> {
        itemView.textInterval.text = res.getString(
            R.string.next_check_x,
            res.getString(R.string.now)
        )
      }
      else -> {
        itemView.textInterval.text = res.getString(
            R.string.next_check_x,
            model.intervalText()
        )
      }
    }
  }

  override fun onLongClick(view: View): Boolean {
    adapter.performClick(adapterPosition, true)
    return false
  }
}

/** @author Aidan Follestad (@afollestad) */
class SiteAdapter(private val listener: Listener) : RecyclerView.Adapter<SiteViewHolder>() {

  private var models = mutableListOf<Site>()

  internal fun performClick(
    index: Int,
    longClick: Boolean
  ) = listener.invoke(models[index], longClick)

  fun set(newModels: List<Site>) {
    val formerModels = this.models
    this.models = newModels.toMutableList()
    val diffResult = calculateDiff(SiteDiffCallback(formerModels, this.models))
    diffResult.dispatchUpdatesTo(this)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): SiteViewHolder {
    val v = LayoutInflater.from(parent.context)
        .inflate(R.layout.list_item_server, parent, false)
    return SiteViewHolder(v, this)
  }

  override fun onBindViewHolder(
    holder: SiteViewHolder,
    position: Int
  ) {
    val model = models[position]
    holder.bind(model)
  }

  override fun getItemCount() = models.size
}
