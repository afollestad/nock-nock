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

import androidx.recyclerview.widget.DiffUtil
import com.afollestad.nocknock.data.model.Site

/** @author Aidan Follestad (@afollestad) */
class SiteDiffCallback(
  private val oldItems: List<Site>,
  private val newItems: List<Site>
) : DiffUtil.Callback() {

  override fun getOldListSize() = oldItems.size

  override fun getNewListSize() = newItems.size

  override fun areItemsTheSame(
    oldItemPosition: Int,
    newItemPosition: Int
  ) = oldItems[oldItemPosition].id == newItems[newItemPosition].id

  override fun areContentsTheSame(
    oldItemPosition: Int,
    newItemPosition: Int
  ) = oldItems[oldItemPosition] == newItems[newItemPosition]
}
