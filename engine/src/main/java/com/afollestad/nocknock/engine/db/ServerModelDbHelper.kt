/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.engine.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.afollestad.nocknock.data.ServerModel

private const val SQL_CREATE_ENTRIES =
  "CREATE TABLE ${ServerModel.TABLE_NAME} (" +
      "${ServerModel.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
      "${ServerModel.COLUMN_NAME} TEXT," +
      "${ServerModel.COLUMN_URL} TEXT," +
      "${ServerModel.COLUMN_STATUS} INTEGER," +
      "${ServerModel.COLUMN_CHECK_INTERVAL} INTEGER," +
      "${ServerModel.COLUMN_LAST_CHECK} INTEGER," +
      "${ServerModel.COLUMN_REASON} TEXT," +
      "${ServerModel.COLUMN_VALIDATION_MODE} INTEGER," +
      "${ServerModel.COLUMN_VALIDATION_CONTENT} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ServerModel.TABLE_NAME}"

/** @author Aidan Follestad (afollestad) */
class ServerModelDbHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
  companion object {
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "ServerModels.db"
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(SQL_CREATE_ENTRIES)
  }

  override fun onUpgrade(
    db: SQLiteDatabase,
    oldVersion: Int,
    newVersion: Int
  ) {
    db.execSQL(SQL_DELETE_ENTRIES)
    onCreate(db)
  }

  override fun onDowngrade(
    db: SQLiteDatabase,
    oldVersion: Int,
    newVersion: Int
  ) = onUpgrade(db, oldVersion, newVersion)
}
