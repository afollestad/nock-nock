/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine.db

import android.app.Application
import android.database.Cursor
import com.afollestad.nocknock.data.ServerModel
import com.afollestad.nocknock.data.ServerModel.Companion.COLUMN_ID
import com.afollestad.nocknock.data.ServerModel.Companion.DEFAULT_SORT_ORDER
import com.afollestad.nocknock.data.ServerModel.Companion.TABLE_NAME
import com.afollestad.nocknock.utilities.ext.diffFrom
import org.jetbrains.annotations.TestOnly
import javax.inject.Inject
import timber.log.Timber.d as log
import timber.log.Timber.w as warn

/** @author Aidan Follestad (@afollestad) */
interface ServerModelStore {

  suspend fun get(id: Int? = null): List<ServerModel>

  suspend fun put(model: ServerModel): ServerModel

  suspend fun update(model: ServerModel): Int

  suspend fun delete(model: ServerModel): Int

  suspend fun delete(id: Int): Int

  suspend fun deleteAll(): Int
}

/** @author Aidan Follestad (@afollestad) */
class RealServerModelStore @Inject constructor(app: Application) : ServerModelStore {

  private val dbHelper = ServerModelDbHelper(app)

  override suspend fun get(id: Int?): List<ServerModel> {
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

  override suspend fun put(model: ServerModel): ServerModel {
    check(model.id == 0) { "Cannot put a model that already has an ID." }

    val writer = dbHelper.writableDatabase
    val newId = writer.insert(TABLE_NAME, null, model.toContentValues())

    return model.copy(id = newId.toInt())
        .apply {
          log("Inserted new site model: $this")
        }
  }

  override suspend fun update(model: ServerModel): Int {
    check(model.id != 0) { "Cannot update a model that does not have an ID." }

    val oldModel = get(model.id).single()
    val oldValues = oldModel.toContentValues()

    val writer = dbHelper.writableDatabase
    val newValues = model.toContentValues()
    val valuesDiff = oldValues.diffFrom(newValues)

    if (valuesDiff.size() == 0) {
      warn("Nothing has changed - nothing to update!")
      return 0
    }

    val selection = "$COLUMN_ID = ?"
    val selectionArgs = arrayOf("${model.id}")

    log("Updated model: $model")
    return writer.update(TABLE_NAME, valuesDiff, selection, selectionArgs)
  }

  override suspend fun delete(model: ServerModel) = delete(model.id)

  override suspend fun delete(id: Int): Int {
    check(id != 0) { "Cannot delete a model that doesn't have an ID." }

    val selection = "$COLUMN_ID = ?"
    val selectionArgs = arrayOf("$id")

    log("Deleted model: $id")
    return dbHelper.writableDatabase.delete(TABLE_NAME, selection, selectionArgs)
  }

  override suspend fun deleteAll(): Int {
    log("Deleted all models")
    return dbHelper.writableDatabase.delete(TABLE_NAME, null, null)
  }

  @TestOnly fun db() = dbHelper

  private fun readModels(cursor: Cursor): List<ServerModel> {
    val results = mutableListOf<ServerModel>()
    while (cursor.moveToNext()) {
      results.add(ServerModel.pull(cursor))
    }
    return results
  }
}
