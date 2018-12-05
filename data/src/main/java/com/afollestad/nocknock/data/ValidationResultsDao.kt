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
import com.afollestad.nocknock.data.model.ValidationResult

/** @author Aidan Follestad (@afollestad) */
@Dao
interface ValidationResultsDao {

  @Query("SELECT * FROM validation_results ORDER BY siteId ASC")
  fun all(): List<ValidationResult>

  @Query("SELECT * FROM validation_results WHERE siteId = :siteId LIMIT 1")
  fun forSite(siteId: Long): List<ValidationResult>

  @Insert(onConflict = FAIL)
  fun insert(siteSetting: ValidationResult): Long

  @Update(onConflict = FAIL)
  fun update(siteSetting: ValidationResult): Int

  @Delete
  fun delete(siteSetting: ValidationResult): Int
}
