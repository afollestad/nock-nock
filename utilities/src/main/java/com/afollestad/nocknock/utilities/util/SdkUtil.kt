/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.util

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O

fun hasOreo() = SDK_INT >= O
