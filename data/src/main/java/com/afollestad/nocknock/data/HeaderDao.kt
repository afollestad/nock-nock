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
package com.afollestad.nocknock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.FAIL
import androidx.room.Query
import androidx.room.Update
import com.afollestad.nocknock.data.model.Header

/** @author Aidan Follestad (@afollestad) */
@Dao
interface HeaderDao {

  @Query("SELECT * FROM headers ORDER BY siteId ASC")
  fun all(): List<Header>

  @Query("SELECT * FROM headers WHERE siteId = :siteId")
  fun forSite(siteId: Long): List<Header>

  @Insert(onConflict = FAIL)
  fun insert(headers: Header): Long

  @Insert(onConflict = FAIL)
  fun insert(headers: List<Header>): List<Long>

  @Update(onConflict = FAIL)
  fun update(header: Header): Int

  @Delete
  fun delete(headers: List<Header>): Int
}
