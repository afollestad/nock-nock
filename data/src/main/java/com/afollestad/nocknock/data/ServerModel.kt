/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.data

import android.content.ContentValues
import android.database.Cursor
import com.afollestad.nocknock.data.ServerStatus.OK
import com.afollestad.nocknock.utilities.ext.timeString
import java.io.Serializable
import java.lang.System.currentTimeMillis
import kotlin.math.max

const val CHECK_INTERVAL_UNSET = -1L
const val LAST_CHECK_NONE = -1L

/** @author Aidan Follestad (afollestad)*/
data class ServerModel(
  var id: Int = 0,
  val name: String,
  val url: String,
  val status: ServerStatus = OK,
  val checkInterval: Long = CHECK_INTERVAL_UNSET,
  val lastCheck: Long = LAST_CHECK_NONE,
  val reason: String? = null,
  val validationMode: ValidationMode,
  val validationContent: String? = null,
  val disabled: Boolean = false
) : Serializable {

  companion object {
    const val TABLE_NAME = "server_models"
    const val COLUMN_ID = "_id"
    const val COLUMN_NAME = "name"
    const val COLUMN_URL = "url"
    const val COLUMN_STATUS = "status"
    const val COLUMN_CHECK_INTERVAL = "check_interval"
    const val COLUMN_LAST_CHECK = "last_check"
    const val COLUMN_REASON = "reason"
    const val COLUMN_VALIDATION_MODE = "validation_mode"
    const val COLUMN_VALIDATION_CONTENT = "validation_content"
    const val COLUMN_DISABLED = "disabled"

    const val DEFAULT_SORT_ORDER = "$COLUMN_NAME ASC, $COLUMN_DISABLED DESC"

    fun pull(cursor: Cursor): ServerModel {
      return ServerModel(
          id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
          name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
          url = cursor.getString(cursor.getColumnIndex(COLUMN_URL)),
          status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)).toServerStatus(),
          checkInterval = cursor.getLong(cursor.getColumnIndex(COLUMN_CHECK_INTERVAL)),
          lastCheck = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_CHECK)),
          reason = cursor.getString(cursor.getColumnIndex(COLUMN_REASON)),
          validationMode = cursor.getInt(
              cursor.getColumnIndex(COLUMN_VALIDATION_MODE)
          ).toValidationMode(),
          validationContent = cursor.getString(cursor.getColumnIndex(COLUMN_VALIDATION_CONTENT)),
          disabled = cursor.getInt(cursor.getColumnIndex(COLUMN_DISABLED)) == 1
      )
    }
  }

  fun intervalText(): String {
    val now = currentTimeMillis()
    val nextCheck = max(lastCheck, 0) + checkInterval
    return (nextCheck - now).timeString()
  }

  fun toContentValues() = ContentValues().apply {
    put(COLUMN_NAME, name)
    put(COLUMN_URL, url)
    put(COLUMN_STATUS, status.value)
    put(COLUMN_CHECK_INTERVAL, checkInterval)
    put(COLUMN_LAST_CHECK, lastCheck)
    put(COLUMN_REASON, reason)
    put(COLUMN_VALIDATION_MODE, validationMode.value)
    put(COLUMN_VALIDATION_CONTENT, validationContent)
    put(COLUMN_DISABLED, disabled)
  }
}
