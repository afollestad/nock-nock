/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.qualifiers

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/** @author Aidan Follestad (@afollestad) */
@Qualifier
@Retention(RUNTIME)
annotation class MainActivityClass
