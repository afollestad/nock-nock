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
@file:Suppress("DEPRECATION")

package com.afollestad.nocknock.data.legacy

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import com.afollestad.nocknock.data.legacy.ServerModel.Companion.COLUMN_ID
import com.afollestad.nocknock.data.legacy.ServerModel.Companion.DEFAULT_SORT_ORDER
import com.afollestad.nocknock.data.legacy.ServerModel.Companion.TABLE_NAME

/** @author Aidan Follestad (@afollestad) */
@Deprecated("Deprecated in favor of AppDatabase.")
internal class ServerModelStore(app: Application) {

  private val dbHelper = ServerModelDbHelper(app)

  fun get(id: Int? = null): List<ServerModel> {
    if (id == null) {
      return getAll()
    }

    val reader = dbHelper.readableDatabase
    val selection = "$COLUMN_ID = ?"
    val selectionArgs = arrayOf("$id")
    val cursor = reader.query(
        TABLE_NAME,
        null,
        selection,
        selectionArgs,
        null,
        null,
        DEFAULT_SORT_ORDER,
        "1"
    )
    cursor.use {
      val results = readModels(it)
      check(results.size == 1) { "Should only get one model per ID." }
      return results
    }
  }

  private fun getAll(): List<ServerModel> {
    val reader = dbHelper.readableDatabase
    val cursor = reader.query(
        TABLE_NAME,
        null,
        null,
        null,
        null,
        null,
        DEFAULT_SORT_ORDER,
        null
    )
    cursor.use { return readModels(it) }
  }

  fun put(model: ServerModel): ServerModel {
    check(model.id == 0) { "Cannot put a model that already has an ID." }

    val writer = dbHelper.writableDatabase
    val newId = writer.insert(TABLE_NAME, null, model.toContentValues())

    return model.copy(id = newId.toInt())
  }

  fun update(model: ServerModel): Int {
    check(model.id != 0) { "Cannot update a model that does not have an ID." }

    val oldModel = get(model.id).single()
    val oldValues = oldModel.toContentValues()

    val writer = dbHelper.writableDatabase
    val newValues = model.toContentValues()
    val valuesDiff = oldValues.diffFrom(newValues)

    if (valuesDiff.size() == 0) {
      return 0
    }

    val selection = "$COLUMN_ID = ?"
    val selectionArgs = arrayOf("${model.id}")

    return writer.update(TABLE_NAME, valuesDiff, selection, selectionArgs)
  }

  fun delete(model: ServerModel) = delete(model.id)

  fun delete(id: Int): Int {
    check(id != 0) { "Cannot delete a model that doesn't have an ID." }

    val selection = "$COLUMN_ID = ?"
    val selectionArgs = arrayOf("$id")

    return dbHelper.writableDatabase.delete(TABLE_NAME, selection, selectionArgs)
  }

  fun wipe() = dbHelper.wipe()

  private fun readModels(cursor: Cursor): List<ServerModel> {
    val results = mutableListOf<ServerModel>()
    while (cursor.moveToNext()) {
      results.add(ServerModel.pull(cursor))
    }
    return results
  }

  /**
   * Returns a [ContentValues] instance which contains only values that have changed between
   * the receiver (original) and parameter (new) instances.
   */
  private fun ContentValues.diffFrom(contentValues: ContentValues): ContentValues {
    val diff = ContentValues()
    for ((name, oldValue) in this.valueSet()) {
      val newValue = contentValues.get(name)
      if (newValue != oldValue) {
        diff.putAny(name, newValue)
      }
    }
    return diff
  }

  /**
   * Auto casts an [Any] value and uses the appropriate `put` method to store it
   * in the [ContentValues] instance.
   */
  private fun ContentValues.putAny(
    name: String,
    value: Any?
  ) {
    if (value == null) {
      putNull(name)
      return
    }
    when (value) {
      is String -> put(name, value)
      is Byte -> put(name, value)
      is Short -> put(name, value)
      is Int -> put(name, value)
      is Long -> put(name, value)
      is Float -> put(name, value)
      is Double -> put(name, value)
      is Boolean -> put(name, value)
      is ByteArray -> put(name, value)
      else -> throw IllegalArgumentException("ContentValues can't hold $value")
    }
  }
}
