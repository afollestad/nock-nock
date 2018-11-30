/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.ext

import android.net.Uri

fun Uri.isHttpOrHttps() = scheme == "http" || scheme == "https"
