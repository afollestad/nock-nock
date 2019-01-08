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

import android.graphics.Color.WHITE
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.nocknock.R
import com.afollestad.nocknock.adapter.TagAdapter.TagViewHolder
import kotlinx.android.synthetic.main.list_item_tag.view.chip

typealias TagsListener = (tags: List<String>) -> Unit

/** @author Aidan Follestad (@afollestad) */
class TagAdapter(
  private val listener: TagsListener
) : RecyclerView.Adapter<TagViewHolder>() {

  private val tags = mutableListOf<String>()
  private val checked = mutableListOf<Int>()

  fun set(tags: List<String>) {
    this.tags.run {
      clear()
      addAll(tags)
    }
    notifyDataSetChanged()
  }

  fun toggleChecked(index: Int) {
    if (checked.contains(index)) {
      checked.remove(index)
    } else {
      checked.add(index)
    }
    notifyItemChanged(index)
    listener.invoke(getCheckedTags())
  }

  private fun getCheckedTags(): List<String> {
    return mutableListOf<String>().apply {
      checked.forEach { index -> add(tags[index]) }
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): TagViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.list_item_tag, parent, false)
    return TagViewHolder(view, this)
  }

  override fun getItemCount() = tags.size

  override fun onBindViewHolder(
    holder: TagViewHolder,
    position: Int
  ) {
    holder.bind(tags[position], checked.contains(position))
  }

  /** @author Aidan Follestad (@afollestad) */
  class TagViewHolder(
    itemView: View,
    private val adapter: TagAdapter
  ) : ViewHolder(itemView), OnClickListener {

    override fun onClick(v: View) = adapter.toggleChecked(adapterPosition)

    init {
      itemView.setOnClickListener(this)
    }

    fun bind(
      name: String,
      checked: Boolean
    ) = itemView.chip.run {
      text = name
      setTextColor(
          if (checked) {
            WHITE
          } else {
            ContextCompat.getColor(itemView.context, R.color.unchecked_chip_text)
          }
      )
      setBackgroundResource(
          if (checked) {
            R.drawable.checked_chip_selector
          } else {
            R.drawable.unchecked_chip_selector
          }
      )
    }
  }
}
