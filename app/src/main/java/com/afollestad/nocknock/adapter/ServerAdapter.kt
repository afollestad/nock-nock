/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.nocknock.R
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.textRes
import com.afollestad.nocknock.utilities.ui.onDebouncedClick
import kotlinx.android.synthetic.main.list_item_server.view.iconStatus
import kotlinx.android.synthetic.main.list_item_server.view.textInterval
import kotlinx.android.synthetic.main.list_item_server.view.textName
import kotlinx.android.synthetic.main.list_item_server.view.textStatus
import kotlinx.android.synthetic.main.list_item_server.view.textUrl

typealias Listener = (model: ServerModel, longClick: Boolean) -> Unit

/** @author Aidan Follestad (afollestad) */
class ServerVH constructor(
  itemView: View,
  private val adapter: ServerAdapter
) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {

  init {
    itemView.onDebouncedClick {
      adapter.performClick(adapterPosition, false)
    }
    itemView.setOnLongClickListener(this)
  }

  fun bind(model: ServerModel) {
    itemView.textName.text = model.name
    itemView.textUrl.text = model.url
    itemView.iconStatus.setStatus(model.status)

    val statusText = model.status.textRes()
    if (statusText == 0) {
      itemView.textStatus.text = model.reason
    } else {
      itemView.textStatus.setText(statusText)
    }

    if (model.disabled) {
      itemView.textInterval.setText(R.string.checks_disabled)
    } else {
      itemView.textInterval.text = itemView.resources.getString(
          R.string.next_check_x,
          model.intervalText()
      )
    }
  }

  override fun onLongClick(view: View): Boolean {
    adapter.performClick(adapterPosition, true)
    return false
  }
}

/** @author Aidan Follestad (afollestad) */
class ServerAdapter(private val listener: Listener) : RecyclerView.Adapter<ServerVH>() {

  private val models = mutableListOf<ServerModel>()

  internal fun performClick(
    index: Int,
    longClick: Boolean
  ) = listener.invoke(models[index], longClick)

  fun add(model: ServerModel) {
    models.add(model)
    notifyItemInserted(models.size - 1)
  }

  fun update(target: ServerModel) {
    for ((i, model) in models.withIndex()) {
      if (model.id == target.id) {
        update(i, target)
        break
      }
    }
  }

  private fun update(
    index: Int,
    model: ServerModel
  ) {
    models[index] = model
    notifyItemChanged(index)
  }

  fun remove(index: Int) {
    models.removeAt(index)
    notifyItemRemoved(index)
  }

  fun remove(target: ServerModel) {
    for ((i, model) in models.withIndex()) {
      if (model.id == target.id) {
        remove(i)
        break
      }
    }
  }

  fun set(newModels: List<ServerModel>) {
    this.models.clear()
    if (newModels.isEmpty()) {
      return
    }
    this.models.addAll(newModels)
    notifyDataSetChanged()
  }

  fun clear() {
    models.clear()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ServerVH {
    val v = LayoutInflater.from(parent.context)
        .inflate(R.layout.list_item_server, parent, false)
    return ServerVH(v, this)
  }

  override fun onBindViewHolder(
    holder: ServerVH,
    position: Int
  ) {
    val model = models[position]
    holder.bind(model)
  }

  override fun getItemCount() = models.size
}
