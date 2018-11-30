/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities

/** @author Aidan Follestad (afollestad)*/
interface Injector {

  fun injectInto(target: Any)
}
