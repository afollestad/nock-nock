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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrates the database from version 1 to 2.
 *
 * @author Aidan Follestad (@afollestad)
 */
class Database1to2Migration : Migration(1, 2) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `retry_policies` (siteId INTEGER PRIMARY KEY NOT NULL, count INTEGER NOT NULL, minutes INTEGER NOT NULL, lastTryTimestamp INTEGER NOT NULL, triesLeft INTEGER NOT NULL)"
    )
  }
}

/**
 * Migrates the database from version 2 to 3.
 *
 * @author Aidan Follestad (@afollestad)
 */
class Database2to3Migration : Migration(2, 3) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("ALTER TABLE `sites` ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
  }
}

/**
 * Migrates the database from version 3 to 4.
 *
 * @author Aidan Follestad (@afollestad)
 */
class Database3to4Migration : Migration(3, 4) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `headers` (id INTEGER PRIMARY KEY NOT NULL, siteId INTEGER NOT NULL, `key` TEXT NOT NULL, value TEXT NOT NULL)"
    )
  }
}

/**
 * Migrates the database from version 4 to 5.
 *
 * @author Aidan Follestad (@afollestad)
 */
class Database4to5Migration : Migration(4, 5) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("ALTER TABLE `site_settings` ADD COLUMN certificate TEXT")
  }
}
